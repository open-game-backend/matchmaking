package de.opengamebackend.matchmaking.controller;

import com.google.common.base.Strings;
import de.opengamebackend.matchmaking.model.MatchmakingStatus;
import de.opengamebackend.matchmaking.model.PlayerStatus;
import de.opengamebackend.matchmaking.model.ServerStatus;
import de.opengamebackend.matchmaking.model.entities.GameServer;
import de.opengamebackend.matchmaking.model.entities.Player;
import de.opengamebackend.matchmaking.model.repositories.GameServerRepository;
import de.opengamebackend.matchmaking.model.repositories.PlayerRepository;
import de.opengamebackend.matchmaking.model.requests.*;
import de.opengamebackend.matchmaking.model.responses.*;
import de.opengamebackend.net.ApiException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RestController
public class MatchmakingController {
    private static final long SERVER_HEARTBEAT_TIMEOUT_SECONDS = 120;
    private static final long CLIENT_JOIN_TIMEOUT_SECONDS = 120;

    @Autowired
    GameServerRepository gameServerRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/servers")
    public ResponseEntity<GetServersResponse> getServers() {
        GetServersResponse response = new GetServersResponse();

        ArrayList<GetServersResponseServer> servers = new ArrayList<>();

        for (GameServer server : gameServerRepository.findAll()) {
            GetServersResponseServer responseServer = modelMapper.map(server, GetServersResponseServer.class);

            ArrayList<String> playerIds = new ArrayList<>();

            for (Player player : server.getPlayers()) {
                playerIds.add(player.getPlayerId());
            }

            responseServer.setPlayerIds(playerIds);

            servers.add(responseServer);
        }

        response.setServers(servers);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/queue")
    public ResponseEntity<GetQueueResponse> getQueue() {
        GetQueueResponse response = new GetQueueResponse();

        ArrayList<GetQueueResponsePlayer> players = new ArrayList<>();

        for (Player player : playerRepository.findAll()) {
            GetQueueResponsePlayer responsePlayer = modelMapper.map(player, GetQueueResponsePlayer.class);
            responsePlayer.setServerId(player.getGameServer() != null ? player.getGameServer().getId() : null);
            players.add(responsePlayer);
        }

        response.setPlayers(players);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/register")
    public ResponseEntity<ServerRegisterResponse> register(@RequestBody ServerRegisterRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getGameMode())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_GAME_MODE);
        }

        if (Strings.isNullOrEmpty(request.getIpV4Address())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_IPV4_ADDRESS);
        }

        if (Strings.isNullOrEmpty(request.getRegion())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_REGION);
        }

        if (Strings.isNullOrEmpty(request.getVersion())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_VERSION);
        }

        // Check if already exists.
        Iterable<GameServer> allServers = gameServerRepository.findAll();
        Stream<GameServer> allServersStream = StreamSupport.stream(allServers.spliterator(), false);

        GameServer gameServer = allServersStream.filter(s ->
                s.getIpV4Address().equals(request.getIpV4Address()) &&
                s.getPort() == request.getPort())
                .findFirst().orElse(null);

        if (gameServer == null) {
            gameServer = modelMapper.map(request, GameServer.class);
            gameServer.setId(UUID.randomUUID().toString());
        } else {
            gameServer.setVersion(request.getVersion());
            gameServer.setGameMode(request.getGameMode());
            gameServer.setRegion(request.getRegion());
            gameServer.setMaxPlayers(request.getMaxPlayers());

            gameServer.getPlayers().clear();
        }

        gameServer.setLastHeartbeat(LocalDateTime.now());
        gameServer.setStatus(ServerStatus.OPEN);

        gameServerRepository.save(gameServer);

        ServerRegisterResponse response = new ServerRegisterResponse(gameServer.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/deregister")
    public ResponseEntity<ServerDeregisterResponse> deregister(@RequestBody ServerDeregisterRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getId())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_GAME_SERVER_ID);
        }

        Optional<GameServer> gameServer = gameServerRepository.findById(request.getId());

        if (!gameServer.isPresent()) {
            throw new ApiException(ApiErrors.ERROR_GAME_SERVER_NOT_FOUND);
        }

        gameServerRepository.deleteById(request.getId());

        ServerDeregisterResponse response = new ServerDeregisterResponse(request.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/sendHeartbeat")
    public ResponseEntity<ServerSendHeartbeatResponse> sendHeartbeat(@RequestBody ServerSendHeartbeatRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getId())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_GAME_SERVER_ID);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getId());

        if (!optionalGameServer.isPresent()) {
            throw new ApiException(ApiErrors.ERROR_GAME_SERVER_NOT_FOUND);
        }

        GameServer gameServer = optionalGameServer.get();
        gameServer.setLastHeartbeat(LocalDateTime.now());
        gameServerRepository.save(gameServer);

        ServerSendHeartbeatResponse response = new ServerSendHeartbeatResponse(request.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/client/enqueue")
    public ResponseEntity<ClientEnqueueResponse> enqueue(@RequestBody ClientEnqueueRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getPlayerId())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_PLAYER_ID);
        }

        if (Strings.isNullOrEmpty(request.getGameMode())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_GAME_MODE);
        }

        if (Strings.isNullOrEmpty(request.getRegion())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_REGION);
        }

        if (Strings.isNullOrEmpty(request.getVersion())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_VERSION);
        }

        // Check if already exists.
        Optional<Player> optionalPlayer = playerRepository.findById(request.getPlayerId());

        Player player = optionalPlayer.orElseGet(() -> modelMapper.map(request, Player.class));
        player.setStatus(PlayerStatus.QUEUED);

        if (player.getGameServer() != null) {
            player.getGameServer().getPlayers().remove(player);
            gameServerRepository.save(player.getGameServer());
        }

        player.setGameServer(null);
        player.setMatchedTime(null);
        player.setJoinedTime(null);

        playerRepository.save(player);

        ClientEnqueueResponse response = new ClientEnqueueResponse(request.getPlayerId(), player.getStatus());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/client/dequeue")
    public ResponseEntity<ClientDequeueResponse> dequeue(@RequestBody ClientDequeueRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getPlayerId())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_PLAYER_ID);
        }

        Optional<Player> player = playerRepository.findById(request.getPlayerId());

        if (!player.isPresent()) {
            throw new ApiException(ApiErrors.ERROR_PLAYER_NOT_FOUND);
        }

        playerRepository.deleteById(request.getPlayerId());

        ClientDequeueResponse response = new ClientDequeueResponse(request.getPlayerId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/client/pollMatchmaking")
    public ResponseEntity<ClientPollMatchmakingResponse> pollMatchmaking(@RequestBody ClientPollMatchmakingRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getPlayerId())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_PLAYER_ID);
        }

        Optional<Player> optionalPlayer = playerRepository.findById(request.getPlayerId());

        if (!optionalPlayer.isPresent()) {
            throw new ApiException(ApiErrors.ERROR_PLAYER_NOT_FOUND);
        }

        Player player = optionalPlayer.get();
        GameServer gameServer = player.getGameServer();

        // Check if already matched.
        if (gameServer != null) {
            ClientPollMatchmakingResponse response = new ClientPollMatchmakingResponse();
            response.setServerId(gameServer.getId());
            response.setIpV4Address(gameServer.getIpV4Address());
            response.setPort(gameServer.getPort());
            response.setStatus(gameServer.isFull() ? MatchmakingStatus.MATCH_FOUND : MatchmakingStatus.WAITING_FOR_PLAYERS);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        // Clean up game servers.
        Iterable<GameServer> allServers = gameServerRepository.findAll();
        Stream<GameServer> allServersStream = StreamSupport.stream(allServers.spliterator(), false);

        Stream<GameServer> expiredServersStream = allServersStream.filter
                (s -> s.getLastHeartbeat().plusSeconds(SERVER_HEARTBEAT_TIMEOUT_SECONDS).isBefore(LocalDateTime.now()));
        Iterable<GameServer> expiredServers = expiredServersStream::iterator;

        gameServerRepository.deleteAll(expiredServers);

        // Clean up players.
        Iterable<Player> allPlayers = playerRepository.findAll();
        Stream<Player> allPlayersStream = StreamSupport.stream(allPlayers.spliterator(), false);

        Stream<Player> expiredPlayersStream = allPlayersStream.filter
                (p -> p.getStatus() == PlayerStatus.MATCHED &&
                        p.getMatchedTime().plusSeconds(CLIENT_JOIN_TIMEOUT_SECONDS).isBefore(LocalDateTime.now()));
        expiredPlayersStream.forEach(this::removePlayer);

        // Get open servers.
        allServers = gameServerRepository.findAll();
        allServersStream = StreamSupport.stream(allServers.spliterator(), false);

        Stream<GameServer> openServers = allServersStream.filter
                (s -> s.getStatus() == ServerStatus.OPEN &&
                        s.getPlayers().size() < s.getMaxPlayers() &&
                        s.getVersion().equals(player.getVersion()) &&
                        s.getGameMode().equals(player.getGameMode()) &&
                        s.getRegion().equals(player.getRegion()));

        // Fill up servers as quickly as possible.
        openServers = openServers.sorted(Comparator.comparingInt(s -> s.getPlayers().size()));

        // Find server.
        GameServer openServer = openServers.findFirst().orElse(null);

        if (openServer == null) {
            ClientPollMatchmakingResponse response = new ClientPollMatchmakingResponse();
            response.setStatus(MatchmakingStatus.SERVERS_FULL);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        // Allocate player to server.
        player.setStatus(PlayerStatus.MATCHED);
        player.setGameServer(openServer);
        player.setMatchedTime(LocalDateTime.now());

        openServer.getPlayers().add(player);

        playerRepository.save(player);
        gameServerRepository.save(openServer);

        // Send response.
        ClientPollMatchmakingResponse response = new ClientPollMatchmakingResponse();
        response.setServerId(openServer.getId());
        response.setIpV4Address(openServer.getIpV4Address());
        response.setPort(openServer.getPort());
        response.setStatus(openServer.isFull() ? MatchmakingStatus.MATCH_FOUND : MatchmakingStatus.WAITING_FOR_PLAYERS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/notifyPlayerJoined")
    public ResponseEntity<ServerNotifyPlayerJoinedResponse> notifyPlayerJoined(@RequestBody ServerNotifyPlayerJoinedRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getServerId())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_GAME_SERVER_ID);
        }

        if (Strings.isNullOrEmpty(request.getPlayerId())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_PLAYER_ID);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getServerId());

        if (!optionalGameServer.isPresent()) {
            throw new ApiException(ApiErrors.ERROR_GAME_SERVER_NOT_FOUND);
        }

        GameServer gameServer = optionalGameServer.get();

        // Find player.
        Player player = gameServer.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(request.getPlayerId()))
                .findFirst().orElse(null);

        if (player != null)
        {
            if (player.getStatus() != PlayerStatus.JOINED)
            {
                player.setStatus(PlayerStatus.JOINED);
                player.setJoinedTime(LocalDateTime.now());

                playerRepository.save(player);
            }

            ServerNotifyPlayerJoinedResponse response = new ServerNotifyPlayerJoinedResponse(request.getPlayerId(), request.getServerId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        else
        {
            throw new ApiException(ApiErrors.ERROR_PLAYER_NOT_FOUND_FOR_SERVER);
        }
    }

    @PostMapping("/server/notifyPlayerLeft")
    public ResponseEntity<ServerNotifyPlayerLeftResponse> notifyPlayerLeft(@RequestBody ServerNotifyPlayerLeftRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getServerId())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_GAME_SERVER_ID);
        }

        if (Strings.isNullOrEmpty(request.getPlayerId())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_PLAYER_ID);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getServerId());

        if (!optionalGameServer.isPresent()) {
            throw new ApiException(ApiErrors.ERROR_GAME_SERVER_NOT_FOUND);
        }

        GameServer gameServer = optionalGameServer.get();

        // Find player.
        Player player = gameServer.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(request.getPlayerId()))
                .findFirst().orElse(null);

        if (player != null)
        {
            removePlayer(player);

            ServerNotifyPlayerLeftResponse response = new ServerNotifyPlayerLeftResponse(request.getPlayerId(), request.getServerId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        else
        {
            throw new ApiException(ApiErrors.ERROR_PLAYER_NOT_FOUND_FOR_SERVER);
        }
    }

    @PostMapping("/server/setStatus")
    public ResponseEntity<ServerSetStatusResponse> setStatus(@RequestBody ServerSetStatusRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getId())) {
            throw new ApiException(ApiErrors.ERROR_MISSING_GAME_SERVER_ID);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getId());

        if (!optionalGameServer.isPresent()) {
            throw new ApiException(ApiErrors.ERROR_GAME_SERVER_NOT_FOUND);
        }

        GameServer gameServer = optionalGameServer.get();
        gameServer.setStatus(request.getStatus());
        gameServerRepository.save(gameServer);

        ServerSetStatusResponse response = new ServerSetStatusResponse(request.getId(), request.getStatus());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void removePlayer(Player player) {
        if (player == null) {
            return;
        }

        if (player.getGameServer() != null) {
            player.getGameServer().getPlayers().remove(player);
            gameServerRepository.save(player.getGameServer());
        }

        playerRepository.delete(player);
    }
}
