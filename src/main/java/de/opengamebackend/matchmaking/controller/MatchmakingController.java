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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RestController
public class MatchmakingController {
    private static final ErrorResponse ERROR_MISSING_GAME_MODE =
            new ErrorResponse(100, "Missing game mode.");
    private static final ErrorResponse ERROR_MISSING_IPV4_ADDRESS =
            new ErrorResponse(101, "Missing IPv4 address.");
    private static final ErrorResponse ERROR_MISSING_REGION =
            new ErrorResponse(103, "Missing region.");
    private static final ErrorResponse ERROR_MISSING_VERSION =
            new ErrorResponse(104, "Missing version.");
    private static final ErrorResponse ERROR_MISSING_GAME_SERVER_ID =
            new ErrorResponse(105, "Missing game server id.");
    private static final ErrorResponse ERROR_GAME_SERVER_NOT_FOUND =
            new ErrorResponse(106, "Game server not found.");
    private static final ErrorResponse ERROR_MISSING_PLAYER_ID =
            new ErrorResponse(107, "Missing player id.");
    private static final ErrorResponse ERROR_PLAYER_NOT_FOUND =
            new ErrorResponse(108, "Player not found.");
    private static final ErrorResponse ERROR_PLAYER_NOT_FOUND_FOR_SERVER =
            new ErrorResponse(109, "Player not found for server.");

    private static final long SERVER_HEARTBEAT_TIMEOUT_SECONDS = 120;
    private static final long CLIENT_JOIN_TIMEOUT_SECONDS = 120;

    @Autowired
    GameServerRepository gameServerRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/servers")
    public ResponseEntity getServers() {
        GetServersResponse response = new GetServersResponse(modelMapper, gameServerRepository.findAll());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @GetMapping("/queue")
    public ResponseEntity getQueue() {
        GetQueueResponse response = new GetQueueResponse(modelMapper, playerRepository.findAll());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/server/register")
    public ResponseEntity register(@RequestBody ServerRegisterRequest request) {
        if (Strings.isNullOrEmpty(request.getGameMode())) {
            return new ResponseEntity(ERROR_MISSING_GAME_MODE, HttpStatus.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(request.getIpV4Address())) {
            return new ResponseEntity(ERROR_MISSING_IPV4_ADDRESS, HttpStatus.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(request.getRegion())) {
            return new ResponseEntity(ERROR_MISSING_REGION, HttpStatus.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(request.getVersion())) {
            return new ResponseEntity(ERROR_MISSING_VERSION, HttpStatus.BAD_REQUEST);
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
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/server/deregister")
    public ResponseEntity deregister(@RequestBody ServerDeregisterRequest request) {
        if (Strings.isNullOrEmpty(request.getId())) {
            return new ResponseEntity(ERROR_MISSING_GAME_SERVER_ID, HttpStatus.BAD_REQUEST);
        }

        Optional<GameServer> gameServer = gameServerRepository.findById(request.getId());

        if (!gameServer.isPresent()) {
            return new ResponseEntity(ERROR_GAME_SERVER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        gameServerRepository.deleteById(request.getId());

        ServerDeregisterResponse response = new ServerDeregisterResponse(request.getId());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/server/sendHeartbeat")
    public ResponseEntity sendHeartbeat(@RequestBody ServerSendHeartbeatRequest request) {
        if (Strings.isNullOrEmpty(request.getId())) {
            return new ResponseEntity(ERROR_MISSING_GAME_SERVER_ID, HttpStatus.BAD_REQUEST);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getId());

        if (!optionalGameServer.isPresent()) {
            return new ResponseEntity(ERROR_GAME_SERVER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        GameServer gameServer = optionalGameServer.get();
        gameServer.setLastHeartbeat(LocalDateTime.now());
        gameServerRepository.save(gameServer);

        ServerSendHeartbeatResponse response = new ServerSendHeartbeatResponse(request.getId());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/client/enqueue")
    public ResponseEntity enqueue(@RequestBody ClientEnqueueRequest request) {
        if (Strings.isNullOrEmpty(request.getPlayerId())) {
            return new ResponseEntity(ERROR_MISSING_PLAYER_ID, HttpStatus.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(request.getGameMode())) {
            return new ResponseEntity(ERROR_MISSING_GAME_MODE, HttpStatus.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(request.getRegion())) {
            return new ResponseEntity(ERROR_MISSING_REGION, HttpStatus.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(request.getVersion())) {
            return new ResponseEntity(ERROR_MISSING_VERSION, HttpStatus.BAD_REQUEST);
        }

        // Check if already exists.
        Optional<Player> optionalPlayer = playerRepository.findById(request.getPlayerId());

        Player player = null;

        if (optionalPlayer.isPresent()) {
            player = optionalPlayer.get();
        } else {
            player = modelMapper.map(request, Player.class);
        }

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
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/client/dequeue")
    public ResponseEntity dequeue(@RequestBody ClientDequeueRequest request) {
        if (Strings.isNullOrEmpty(request.getPlayerId())) {
            return new ResponseEntity(ERROR_MISSING_PLAYER_ID, HttpStatus.BAD_REQUEST);
        }

        Optional<Player> player = playerRepository.findById(request.getPlayerId());

        if (!player.isPresent()) {
            return new ResponseEntity(ERROR_PLAYER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        playerRepository.deleteById(request.getPlayerId());

        ClientDequeueResponse response = new ClientDequeueResponse(request.getPlayerId());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/client/pollMatchmaking")
    public ResponseEntity pollMatchmaking(@RequestBody ClientPollMatchmakingRequest request) {
        if (Strings.isNullOrEmpty(request.getPlayerId())) {
            return new ResponseEntity(ERROR_MISSING_PLAYER_ID, HttpStatus.BAD_REQUEST);
        }

        Optional<Player> optionalPlayer = playerRepository.findById(request.getPlayerId());

        if (!optionalPlayer.isPresent()) {
            return new ResponseEntity(ERROR_PLAYER_NOT_FOUND, HttpStatus.BAD_REQUEST);
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

            return new ResponseEntity(response, HttpStatus.OK);
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

            return new ResponseEntity(response, HttpStatus.OK);
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

        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/server/notifyPlayerJoined")
    public ResponseEntity notifyPlayerJoined(@RequestBody ServerNotifyPlayerJoinedRequest request) {
        if (Strings.isNullOrEmpty(request.getServerId())) {
            return new ResponseEntity(ERROR_MISSING_GAME_SERVER_ID, HttpStatus.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(request.getPlayerId())) {
            return new ResponseEntity(ERROR_MISSING_PLAYER_ID, HttpStatus.BAD_REQUEST);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getServerId());

        if (!optionalGameServer.isPresent()) {
            return new ResponseEntity(ERROR_GAME_SERVER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        GameServer gameServer = optionalGameServer.get();

        // Find player.
        Player player = StreamSupport.stream(gameServer.getPlayers().spliterator(), false)
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
            return new ResponseEntity(response, HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity(ERROR_PLAYER_NOT_FOUND_FOR_SERVER, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/server/notifyPlayerLeft")
    public ResponseEntity notifyPlayerLeft(@RequestBody ServerNotifyPlayerLeftRequest request) {
        if (Strings.isNullOrEmpty(request.getServerId())) {
            return new ResponseEntity(ERROR_MISSING_GAME_SERVER_ID, HttpStatus.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(request.getPlayerId())) {
            return new ResponseEntity(ERROR_MISSING_PLAYER_ID, HttpStatus.BAD_REQUEST);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getServerId());

        if (!optionalGameServer.isPresent()) {
            return new ResponseEntity(ERROR_GAME_SERVER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        GameServer gameServer = optionalGameServer.get();

        // Find player.
        Player player = StreamSupport.stream(gameServer.getPlayers().spliterator(), false)
                .filter(p -> p.getPlayerId().equals(request.getPlayerId()))
                .findFirst().orElse(null);

        if (player != null)
        {
            removePlayer(player);

            ServerNotifyPlayerLeftResponse response = new ServerNotifyPlayerLeftResponse(request.getPlayerId(), request.getServerId());
            return new ResponseEntity(response, HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity(ERROR_PLAYER_NOT_FOUND_FOR_SERVER, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/server/setStatus")
    public ResponseEntity setStatus(@RequestBody ServerSetStatusRequest request) {
        if (Strings.isNullOrEmpty(request.getId())) {
            return new ResponseEntity(ERROR_MISSING_GAME_SERVER_ID, HttpStatus.BAD_REQUEST);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getId());

        if (!optionalGameServer.isPresent()) {
            return new ResponseEntity(ERROR_GAME_SERVER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        GameServer gameServer = optionalGameServer.get();
        gameServer.setStatus(request.getStatus());
        gameServerRepository.save(gameServer);

        ServerSetStatusResponse response = new ServerSetStatusResponse(request.getId(), request.getStatus());
        return new ResponseEntity(response, HttpStatus.OK);
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
