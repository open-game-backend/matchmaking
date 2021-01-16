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
import de.opengamebackend.net.ApiErrors;
import de.opengamebackend.net.ApiException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class MatchmakingService {
    public static final long SERVER_HEARTBEAT_TIMEOUT_SECONDS = 120;
    public static final long CLIENT_JOIN_TIMEOUT_SECONDS = 120;

    private GameServerRepository gameServerRepository;
    private PlayerRepository playerRepository;
    private ModelMapper modelMapper;

    @Autowired
    public MatchmakingService(GameServerRepository gameServerRepository, PlayerRepository playerRepository, ModelMapper modelMapper) {
        this.gameServerRepository = gameServerRepository;
        this.playerRepository = playerRepository;
        this.modelMapper = modelMapper;
    }

    public GetServersResponse getServers() {
        GetServersResponse response = new GetServersResponse();

        ArrayList<GetServersResponseServer> servers = new ArrayList<>();

        for (GameServer server : gameServerRepository.findAll()) {
            GetServersResponseServer responseServer = modelMapper.map(server, GetServersResponseServer.class);

            ArrayList<String> playerIds = new ArrayList<>();

            for (Player player : server.getPlayers()) {
                playerIds.add(player.getId());
            }

            responseServer.setPlayerIds(playerIds);

            servers.add(responseServer);
        }

        response.setServers(servers);
        return response;
    }

    public GetQueueResponse getQueue() {
        GetQueueResponse response = new GetQueueResponse();

        ArrayList<GetQueueResponsePlayer> players = new ArrayList<>();

        for (Player player : playerRepository.findAll()) {
            GetQueueResponsePlayer responsePlayer = modelMapper.map(player, GetQueueResponsePlayer.class);
            responsePlayer.setServerId(player.getGameServer() != null ? player.getGameServer().getId() : null);
            players.add(responsePlayer);
        }

        response.setPlayers(players);
        return response;
    }

    public ServerRegisterResponse register(ServerRegisterRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getGameMode())) {
            throw new ApiException(ApiErrors.MISSING_GAME_MODE_CODE, ApiErrors.MISSING_GAME_MODE_MESSAGE);
        }

        if (Strings.isNullOrEmpty(request.getIpV4Address())) {
            throw new ApiException(ApiErrors.MISSING_IPV4_ADDRESS_CODE, ApiErrors.MISSING_IPV4_ADDRESS_MESSAGE);
        }

        if (Strings.isNullOrEmpty(request.getRegion())) {
            throw new ApiException(ApiErrors.MISSING_REGION_CODE, ApiErrors.MISSING_REGION_MESSAGE);
        }

        if (Strings.isNullOrEmpty(request.getVersion())) {
            throw new ApiException(ApiErrors.MISSING_VERSION_CODE, ApiErrors.MISSING_VERSION_MESSAGE);
        }

        // Check if already exists.
        Iterable<GameServer> allServers = gameServerRepository.findAll();
        Stream<GameServer> allServersStream = StreamSupport.stream(allServers.spliterator(), false);

        GameServer gameServer = allServersStream.filter(s ->
                s.getIpV4Address().equals(request.getIpV4Address()) &&
                        s.getPort() == request.getPort())
                .findFirst().orElse(new GameServer());

        modelMapper.map(request, gameServer);

        if (gameServer.getId() == null) {
            gameServer.setId(UUID.randomUUID().toString());
        }

        gameServer.getPlayers().clear();
        gameServer.setLastHeartbeat(OffsetDateTime.now());
        gameServer.setStatus(ServerStatus.OPEN);

        gameServerRepository.save(gameServer);

        return new ServerRegisterResponse(gameServer.getId());
    }

    public ServerDeregisterResponse deregister( ServerDeregisterRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getId())) {
            throw new ApiException(ApiErrors.MISSING_GAME_SERVER_ID_CODE, ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE);
        }

        Optional<GameServer> gameServer = gameServerRepository.findById(request.getId());

        if (!gameServer.isPresent()) {
            throw new ApiException(ApiErrors.GAME_SERVER_NOT_FOUND_CODE, ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE);
        }

        gameServerRepository.delete(gameServer.get());

        return new ServerDeregisterResponse(request.getId());
    }

    public ServerSendHeartbeatResponse sendHeartbeat(ServerSendHeartbeatRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getId())) {
            throw new ApiException(ApiErrors.MISSING_GAME_SERVER_ID_CODE, ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getId());

        if (!optionalGameServer.isPresent()) {
            throw new ApiException(ApiErrors.GAME_SERVER_NOT_FOUND_CODE, ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE);
        }

        GameServer gameServer = optionalGameServer.get();
        gameServer.setLastHeartbeat(OffsetDateTime.now());
        gameServerRepository.save(gameServer);

        return new ServerSendHeartbeatResponse(request.getId());
    }

    public ClientEnqueueResponse enqueue(ClientEnqueueRequest request, String playerId)
            throws ApiException {
        if (Strings.isNullOrEmpty(playerId)) {
            throw new ApiException(ApiErrors.MISSING_PLAYER_ID_CODE, ApiErrors.MISSING_PLAYER_ID_MESSAGE);
        }

        if (Strings.isNullOrEmpty(request.getGameMode())) {
            throw new ApiException(ApiErrors.MISSING_GAME_MODE_CODE, ApiErrors.MISSING_GAME_MODE_MESSAGE);
        }

        if (Strings.isNullOrEmpty(request.getRegion())) {
            throw new ApiException(ApiErrors.MISSING_REGION_CODE, ApiErrors.MISSING_REGION_MESSAGE);
        }

        if (Strings.isNullOrEmpty(request.getVersion())) {
            throw new ApiException(ApiErrors.MISSING_VERSION_CODE, ApiErrors.MISSING_VERSION_MESSAGE);
        }

        // Check if already exists.
        Player player = playerRepository.findById(playerId).orElse(new Player());

        // Update data.
        modelMapper.map(request, player);
        player.setId(playerId);
        player.setStatus(PlayerStatus.QUEUED);

        // Remove from any servers.
        if (player.getGameServer() != null) {
            player.getGameServer().getPlayers().remove(player);
            gameServerRepository.save(player.getGameServer());
        }

        player.setGameServer(null);
        player.setMatchedTime(null);
        player.setJoinedTime(null);

        playerRepository.save(player);

        return new ClientEnqueueResponse(playerId, player.getStatus());
    }

    public ClientDequeueResponse dequeue(String playerId)
            throws ApiException {
        if (Strings.isNullOrEmpty(playerId)) {
            throw new ApiException(ApiErrors.MISSING_PLAYER_ID_CODE, ApiErrors.MISSING_PLAYER_ID_MESSAGE);
        }

        Optional<Player> player = playerRepository.findById(playerId);

        if (!player.isPresent()) {
            throw new ApiException(ApiErrors.PLAYER_NOT_FOUND_CODE, ApiErrors.PLAYER_NOT_FOUND_MESSAGE);
        }

        playerRepository.delete(player.get());

        return new ClientDequeueResponse(playerId);
    }

    public ClientPollMatchmakingResponse pollMatchmaking(String playerId)
            throws ApiException {
        if (Strings.isNullOrEmpty(playerId)) {
            throw new ApiException(ApiErrors.MISSING_PLAYER_ID_CODE, ApiErrors.MISSING_PLAYER_ID_MESSAGE);
        }

        Optional<Player> optionalPlayer = playerRepository.findById(playerId);

        if (!optionalPlayer.isPresent()) {
            throw new ApiException(ApiErrors.PLAYER_NOT_FOUND_CODE, ApiErrors.PLAYER_NOT_FOUND_MESSAGE);
        }

        Player player = optionalPlayer.get();
        GameServer gameServer = player.getGameServer();

        // Check if already matched.
        if (gameServer != null) {
            ClientPollMatchmakingResponse response = new ClientPollMatchmakingResponse();
            response.setPlayerId(playerId);
            response.setServerId(gameServer.getId());
            response.setIpV4Address(gameServer.getIpV4Address());
            response.setPort(gameServer.getPort());
            response.setStatus(gameServer.isFull() ? MatchmakingStatus.MATCH_FOUND : MatchmakingStatus.WAITING_FOR_PLAYERS);

            return response;
        }

        // Clean up game servers.
        Iterable<GameServer> allServers = gameServerRepository.findAll();
        Stream<GameServer> allServersStream = StreamSupport.stream(allServers.spliterator(), false);

        Stream<GameServer> expiredServersStream = allServersStream.filter
                (s -> s.getLastHeartbeat().plusSeconds(SERVER_HEARTBEAT_TIMEOUT_SECONDS).isBefore(OffsetDateTime.now()));
        List<GameServer> expiredServers = expiredServersStream.collect(Collectors.toList());

        gameServerRepository.deleteAll(expiredServers);

        // Clean up players.
        Iterable<Player> allPlayers = playerRepository.findAll();
        Stream<Player> allPlayersStream = StreamSupport.stream(allPlayers.spliterator(), false);

        Stream<Player> expiredPlayersStream = allPlayersStream.filter
                (p -> p.getStatus() == PlayerStatus.MATCHED &&
                        p.getMatchedTime().plusSeconds(CLIENT_JOIN_TIMEOUT_SECONDS).isBefore(OffsetDateTime.now()));
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
            response.setPlayerId(playerId);
            response.setStatus(MatchmakingStatus.SERVERS_FULL);

            return response;
        }

        // Allocate player to server.
        player.setStatus(PlayerStatus.MATCHED);
        player.setGameServer(openServer);
        player.setMatchedTime(OffsetDateTime.now());

        openServer.getPlayers().add(player);

        playerRepository.save(player);
        gameServerRepository.save(openServer);

        // Send response.
        ClientPollMatchmakingResponse response = new ClientPollMatchmakingResponse();
        response.setPlayerId(playerId);
        response.setServerId(openServer.getId());
        response.setIpV4Address(openServer.getIpV4Address());
        response.setPort(openServer.getPort());
        response.setStatus(openServer.isFull() ? MatchmakingStatus.MATCH_FOUND : MatchmakingStatus.WAITING_FOR_PLAYERS);

        return response;
    }

    public ServerNotifyPlayerJoinedResponse notifyPlayerJoined(ServerNotifyPlayerJoinedRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getServerId())) {
            throw new ApiException(ApiErrors.MISSING_GAME_SERVER_ID_CODE, ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE);
        }

        if (Strings.isNullOrEmpty(request.getPlayerId())) {
            throw new ApiException(ApiErrors.MISSING_PLAYER_ID_CODE, ApiErrors.MISSING_PLAYER_ID_MESSAGE);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getServerId());

        if (!optionalGameServer.isPresent()) {
            throw new ApiException(ApiErrors.GAME_SERVER_NOT_FOUND_CODE, ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE);
        }

        GameServer gameServer = optionalGameServer.get();

        // Find player.
        Player player = gameServer.getPlayers().stream()
                .filter(p -> p.getId().equals(request.getPlayerId()))
                .findFirst().orElse(null);

        if (player != null)
        {
            if (player.getStatus() != PlayerStatus.JOINED)
            {
                player.setStatus(PlayerStatus.JOINED);
                player.setJoinedTime(OffsetDateTime.now());

                playerRepository.save(player);
            }

            return new ServerNotifyPlayerJoinedResponse(request.getServerId(), request.getPlayerId());
        }
        else
        {
            throw new ApiException(ApiErrors.PLAYER_NOT_FOUND_FOR_SERVER_CODE, ApiErrors.PLAYER_NOT_FOUND_FOR_SERVER_MESSAGE);
        }
    }

    public ServerNotifyPlayerLeftResponse notifyPlayerLeft(ServerNotifyPlayerLeftRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getServerId())) {
            throw new ApiException(ApiErrors.MISSING_GAME_SERVER_ID_CODE, ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE);
        }

        if (Strings.isNullOrEmpty(request.getPlayerId())) {
            throw new ApiException(ApiErrors.MISSING_PLAYER_ID_CODE, ApiErrors.MISSING_PLAYER_ID_MESSAGE);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getServerId());

        if (!optionalGameServer.isPresent()) {
            throw new ApiException(ApiErrors.GAME_SERVER_NOT_FOUND_CODE, ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE);
        }

        GameServer gameServer = optionalGameServer.get();

        // Find player.
        Player player = gameServer.getPlayers().stream()
                .filter(p -> p.getId().equals(request.getPlayerId()))
                .findFirst().orElse(null);

        if (player != null)
        {
            removePlayer(player);

            return new ServerNotifyPlayerLeftResponse(request.getServerId(), request.getPlayerId());
        }
        else
        {
            throw new ApiException(ApiErrors.PLAYER_NOT_FOUND_FOR_SERVER_CODE, ApiErrors.PLAYER_NOT_FOUND_FOR_SERVER_MESSAGE);
        }
    }

    public ServerSetStatusResponse setStatus( ServerSetStatusRequest request)
            throws ApiException {
        if (Strings.isNullOrEmpty(request.getId())) {
            throw new ApiException(ApiErrors.MISSING_GAME_SERVER_ID_CODE, ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getId());

        if (!optionalGameServer.isPresent()) {
            throw new ApiException(ApiErrors.GAME_SERVER_NOT_FOUND_CODE, ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE);
        }

        GameServer gameServer = optionalGameServer.get();
        gameServer.setStatus(request.getStatus());
        gameServerRepository.save(gameServer);

        return new ServerSetStatusResponse(request.getId(), request.getStatus());
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
