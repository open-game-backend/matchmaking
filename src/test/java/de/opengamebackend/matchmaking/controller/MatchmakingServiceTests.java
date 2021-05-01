package de.opengamebackend.matchmaking.controller;

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
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class MatchmakingServiceTests {
    private GameServerRepository gameServerRepository;
    private PlayerRepository playerRepository;

    private MatchmakingService matchmakingService;

    @BeforeEach
    public void setUp() {
        gameServerRepository = mock(GameServerRepository.class);
        playerRepository = mock(PlayerRepository.class);

        matchmakingService = new MatchmakingService(gameServerRepository, playerRepository, new ModelMapper());
    }

    @Test
    public void givenServers_whenGetServers_thenReturnServers() {
        // GIVEN
        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);

        when(p1.getId()).thenReturn("TestPlayer1");
        when(p2.getId()).thenReturn("TestPlayer2");

        GameServer gameServer = mock(GameServer.class);

        when(gameServer.getId()).thenReturn("testId");
        when(gameServer.getGameMode()).thenReturn("GM");
        when(gameServer.getIpV4Address()).thenReturn("127.0.0.1");
        when(gameServer.getRegion()).thenReturn("EU");
        when(gameServer.getVersion()).thenReturn("1.0");
        when(gameServer.getPort()).thenReturn(1234);
        when(gameServer.getMaxPlayers()).thenReturn(2);
        when(gameServer.getLastHeartbeat()).thenReturn(OffsetDateTime.now());
        when(gameServer.getStatus()).thenReturn(ServerStatus.OPEN);
        when(gameServer.getPlayers()).thenReturn(Lists.newArrayList(p1, p2));

        when(gameServerRepository.findAll()).thenReturn(Lists.newArrayList(gameServer));

        // WHEN
        GetServersResponse response = matchmakingService.getServers();

        // THEN
        assertThat(response.getServers()).isNotNull();
        assertThat(response.getServers()).hasSize(1);
        assertThat(response.getServers().get(0).getId()).isEqualTo(gameServer.getId());
        assertThat(response.getServers().get(0).getGameMode()).isEqualTo(gameServer.getGameMode());
        assertThat(response.getServers().get(0).getIpV4Address()).isEqualTo(gameServer.getIpV4Address());
        assertThat(response.getServers().get(0).getRegion()).isEqualTo(gameServer.getRegion());
        assertThat(response.getServers().get(0).getVersion()).isEqualTo(gameServer.getVersion());
        assertThat(response.getServers().get(0).getPort()).isEqualTo(gameServer.getPort());
        assertThat(response.getServers().get(0).getMaxPlayers()).isEqualTo(gameServer.getMaxPlayers());
        assertThat(response.getServers().get(0).getLastHeartbeat()).isEqualTo(gameServer.getLastHeartbeat());
        assertThat(response.getServers().get(0).getStatus()).isEqualTo(gameServer.getStatus());
        assertThat(response.getServers().get(0).getPlayerIds()).isNotNull();
        assertThat(response.getServers().get(0).getPlayerIds()).containsExactly(p1.getId(), p2.getId());
    }

    @Test
    public void givenPlayers_whenGetQueue_thenReturnPlayers() {
        // GIVEN
        GameServer gameServer = mock(GameServer.class);
        when(gameServer.getId()).thenReturn("testServerId");

        Player player = mock(Player.class);
        when(player.getId()).thenReturn("testPlayerId");
        when(player.getGameMode()).thenReturn("GM");
        when(player.getRegion()).thenReturn("EU");
        when(player.getVersion()).thenReturn("1.0");
        when(player.getStatus()).thenReturn(PlayerStatus.QUEUED);
        when(player.getGameServer()).thenReturn(gameServer);

        when(playerRepository.findAll()).thenReturn(Lists.newArrayList(player));

        // WHEN
        GetQueueResponse response = matchmakingService.getQueue();

        // THEN
        assertThat(response.getPlayers()).isNotNull();
        assertThat(response.getPlayers()).hasSize(1);
        assertThat(response.getPlayers().get(0).getPlayerId()).isEqualTo(player.getId());
        assertThat(response.getPlayers().get(0).getGameMode()).isEqualTo(player.getGameMode());
        assertThat(response.getPlayers().get(0).getRegion()).isEqualTo(player.getRegion());
        assertThat(response.getPlayers().get(0).getVersion()).isEqualTo(player.getVersion());
        assertThat(response.getPlayers().get(0).getStatus()).isEqualTo(player.getStatus());
        assertThat(response.getPlayers().get(0).getServerId()).isEqualTo(gameServer.getId());
    }

    @Test
    public void givenMissingGameMode_whenRegister_thenThrowException() {
        // GIVEN
        ServerRegisterRequest request = new ServerRegisterRequest("1.0", "", "EU", "127.0.0.1", 1234, 2);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.register(request))
                .withMessage(ApiErrors.MISSING_GAME_MODE_MESSAGE);
    }

    @Test
    public void givenMissingIpAddress_whenRegister_thenThrowException() {
        // GIVEN
        ServerRegisterRequest request = new ServerRegisterRequest("1.0", "GM", "EU", "", 1234, 2);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.register(request))
                .withMessage(ApiErrors.MISSING_IPV4_ADDRESS_MESSAGE);
    }

    @Test
    public void givenMissingRegion_whenRegister_thenThrowException() {
        // GIVEN
        ServerRegisterRequest request = new ServerRegisterRequest("1.0", "GM", "", "127.0.0.1", 1234, 2);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.register(request))
                .withMessage(ApiErrors.MISSING_REGION_MESSAGE);
    }

    @Test
    public void givenMissingVersion_whenRegister_thenThrowException() {
        // GIVEN
        ServerRegisterRequest request = new ServerRegisterRequest("", "GM", "EU", "127.0.0.1", 1234, 2);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.register(request))
                .withMessage(ApiErrors.MISSING_VERSION_MESSAGE);
    }

    @Test
    public void givenServer_whenRegister_thenServerIsSaved() throws ApiException {
        // GIVEN
        ServerRegisterRequest request = new ServerRegisterRequest("1.0", "GM", "EU", "127.0.0.1", 1234, 2);

        // WHEN
        matchmakingService.register(request);

        // THEN
        ArgumentCaptor<GameServer> argument = ArgumentCaptor.forClass(GameServer.class);
        verify(gameServerRepository).save(argument.capture());

        GameServer gameServer = argument.getValue();

        assertThat(gameServer.getId()).isNotEmpty();
        assertThat(gameServer.getGameMode()).isEqualTo(request.getGameMode());
        assertThat(gameServer.getIpV4Address()).isEqualTo(request.getIpV4Address());
        assertThat(gameServer.getLastHeartbeat()).isNotNull();
        assertThat(gameServer.getMaxPlayers()).isEqualTo(request.getMaxPlayers());
        assertThat(gameServer.getPort()).isEqualTo(request.getPort());
        assertThat(gameServer.getRegion()).isEqualTo(request.getRegion());
        assertThat(gameServer.getStatus()).isEqualTo(ServerStatus.OPEN);
        assertThat(gameServer.getVersion()).isEqualTo(request.getVersion());
        assertThat(gameServer.getPlayers()).isEmpty();
    }

    @Test
    public void givenExistingServer_whenRegister_thenServerIsUpdated() throws ApiException {
        // GIVEN
        ServerRegisterRequest request = new ServerRegisterRequest("1.0", "newGameMode", "EU", "127.0.0.1", 1234, 2);

        GameServer existingServer = mock(GameServer.class);
        when(existingServer.getIpV4Address()).thenReturn(request.getIpV4Address());
        when(existingServer.getPort()).thenReturn(request.getPort());
        when(existingServer.getGameMode()).thenReturn("oldGameMode");
        when(gameServerRepository.findAll()).thenReturn(Lists.list(existingServer));

        // WHEN
        matchmakingService.register(request);

        // THEN
        verify(existingServer).setGameMode(request.getGameMode());
        verify(existingServer).setLastHeartbeat(any());
        verify(existingServer).setMaxPlayers(request.getMaxPlayers());
        verify(existingServer).setRegion(request.getRegion());
        verify(existingServer).setStatus(ServerStatus.OPEN);
        verify(existingServer).setVersion(request.getVersion());

        verify(gameServerRepository).save(existingServer);
    }

    @Test
    public void givenServer_whenRegister_thenIdIsReturned() throws ApiException {
        // GIVEN
        ServerRegisterRequest request = new ServerRegisterRequest("1.0", "GM", "EU", "127.0.0.1", 1234, 2);

        // WHEN
        ServerRegisterResponse response = matchmakingService.register(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotEmpty();
    }

    @Test
    public void givenMissingId_whenDeregister_thenThrowException() {
        // GIVEN
        ServerDeregisterRequest request = mock(ServerDeregisterRequest.class);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.deregister(request))
                .withMessage(ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE);
    }

    @Test
    public void givenInvalidId_whenDeregister_thenThrowException() {
        // GIVEN
        ServerDeregisterRequest request = mock(ServerDeregisterRequest.class);
        when(request.getId()).thenReturn("testId");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.deregister(request))
                .withMessage(ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE);
    }

    @Test
    public void givenValidId_whenDeregister_thenDeleteServer() throws ApiException {
        // GIVEN
        ServerDeregisterRequest request = mock(ServerDeregisterRequest.class);
        when(request.getId()).thenReturn("testId");

        GameServer gameServer = mock(GameServer.class);
        when(gameServerRepository.findById(request.getId())).thenReturn(Optional.of(gameServer));

        // WHEN
        matchmakingService.deregister(request);

        // THEN
        verify(gameServerRepository).delete(gameServer);
    }

    @Test
    public void givenValidId_whenDeregister_thenReturnId() throws ApiException {
        // GIVEN
        ServerDeregisterRequest request = mock(ServerDeregisterRequest.class);
        when(request.getId()).thenReturn("testId");

        GameServer gameServer = mock(GameServer.class);
        when(gameServerRepository.findById(request.getId())).thenReturn(Optional.of(gameServer));

        // WHEN
        ServerDeregisterResponse response = matchmakingService.deregister(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getRemovedId()).isEqualTo(request.getId());
    }

    @Test
    public void givenMissingId_whenSendHeartbeat_thenThrowException() {
        // GIVEN
        ServerSendHeartbeatRequest request = mock(ServerSendHeartbeatRequest.class);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.sendHeartbeat(request))
                .withMessage(ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE);
    }

    @Test
    public void givenInvalidId_whenSendHeartbeat_thenThrowException() {
        // GIVEN
        ServerSendHeartbeatRequest request = mock(ServerSendHeartbeatRequest.class);
        when(request.getId()).thenReturn("testId");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.sendHeartbeat(request))
                .withMessage(ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE);
    }

    @Test
    public void givenValidId_whenSendHeartbeat_thenUpdateLastHeartbeat() throws ApiException {
        // GIVEN
        ServerSendHeartbeatRequest request = mock(ServerSendHeartbeatRequest.class);
        when(request.getId()).thenReturn("testId");

        GameServer gameServer = mock(GameServer.class);
        when(gameServerRepository.findById(request.getId())).thenReturn(Optional.of(gameServer));

        // WHEN
        matchmakingService.sendHeartbeat(request);

        // THEN
        verify(gameServer).setLastHeartbeat(any());
        verify(gameServerRepository).save(gameServer);
    }

    @Test
    public void givenValidId_whenSendHeartbeat_thenReturnId() throws ApiException {
        // GIVEN
        ServerSendHeartbeatRequest request = mock(ServerSendHeartbeatRequest.class);
        when(request.getId()).thenReturn("testId");

        GameServer gameServer = mock(GameServer.class);
        when(gameServerRepository.findById(request.getId())).thenReturn(Optional.of(gameServer));

        // WHEN
        ServerSendHeartbeatResponse response = matchmakingService.sendHeartbeat(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getUpdatedId()).isEqualTo(request.getId());
    }

    @Test
    public void givenMissingGameMode_whenEnqueue_thenThrowException() {
        // GIVEN
        ClientEnqueueRequest request = new ClientEnqueueRequest("1.0", "", "EU");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.enqueue(request, "testId"))
                .withMessage(ApiErrors.MISSING_GAME_MODE_MESSAGE);
    }

    @Test
    public void givenMissingPlayerId_whenEnqueue_thenThrowException() {
        // GIVEN
        ClientEnqueueRequest request = new ClientEnqueueRequest("1.0", "GM", "EU");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.enqueue(request, ""))
                .withMessage(ApiErrors.MISSING_PLAYER_ID_MESSAGE);
    }

    @Test
    public void givenMissingRegion_whenEnqueue_thenThrowException() {
        // GIVEN
        ClientEnqueueRequest request = new ClientEnqueueRequest("1.0", "GM", "");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.enqueue(request, "testId"))
                .withMessage(ApiErrors.MISSING_REGION_MESSAGE);
    }

    @Test
    public void givenMissingVersion_whenEnqueue_thenThrowException() {
        // GIVEN
        ClientEnqueueRequest request = new ClientEnqueueRequest("", "GM", "EU");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.enqueue(request, "testId"))
                .withMessage(ApiErrors.MISSING_VERSION_MESSAGE);
    }

    @Test
    public void givenPlayer_whenEnqueue_thenPlayerIsSaved() throws ApiException {
        // GIVEN
        String playerId = "testId";
        ClientEnqueueRequest request = new ClientEnqueueRequest("1.0", "GM", "EU");

        // WHEN
        matchmakingService.enqueue(request, playerId);

        // THEN
        ArgumentCaptor<Player> argument = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(argument.capture());

        Player player = argument.getValue();

        assertThat(player.getId()).isEqualTo(playerId);
        assertThat(player.getGameMode()).isEqualTo(request.getGameMode());
        assertThat(player.getRegion()).isEqualTo(request.getRegion());
        assertThat(player.getVersion()).isEqualTo(request.getVersion());
        assertThat(player.getStatus()).isEqualTo(PlayerStatus.QUEUED);
        assertThat(player.getGameServer()).isNull();
        assertThat(player.getMatchedTime()).isNull();
        assertThat(player.getJoinedTime()).isNull();
    }

    @Test
    public void givenExistingServer_whenEnqueue_thenPlayerIsRemovedFromExistingServer() throws ApiException {
        // GIVEN
        String playerId = "testId";
        ClientEnqueueRequest request = new ClientEnqueueRequest("1.0", "GM", "EU");

        Player player = mock(Player.class);
        GameServer gameServer = mock(GameServer.class);

        when(player.getGameServer()).thenReturn(gameServer);

        ArrayList<Player> players = Lists.newArrayList(player);
        when(gameServer.getPlayers()).thenReturn(players);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // WHEN
        matchmakingService.enqueue(request, playerId);

        // THEN
        assertThat(players).doesNotContain(player);
        verify(gameServerRepository).save(gameServer);
    }

    @Test
    public void givenPlayer_whenEnqueue_thenReturnId() throws ApiException {
        // GIVEN
        String playerId = "testId";
        ClientEnqueueRequest request = new ClientEnqueueRequest("1.0", "GM", "EU");

        // WHEN
        ClientEnqueueResponse response = matchmakingService.enqueue(request, playerId);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getPlayerId()).isEqualTo(playerId);
    }

    @Test
    public void givenMissingId_whenDequeue_thenThrowException() {
        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.dequeue(null))
                .withMessage(ApiErrors.MISSING_PLAYER_ID_MESSAGE);
    }

    @Test
    public void givenInvalidId_whenDequeue_thenThrowException() {
        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.dequeue("testId"))
                .withMessage(ApiErrors.PLAYER_NOT_FOUND_MESSAGE);
    }

    @Test
    public void givenValidId_whenDequeue_thenDeletePlayer() throws ApiException {
        // GIVEN
        String playerId = "testId";

        Player player = mock(Player.class);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // WHEN
        matchmakingService.dequeue(playerId);

        // THEN
        verify(playerRepository).delete(player);
    }

    @Test
    public void givenValidId_whenDequeue_thenReturnId() throws ApiException {
        // GIVEN
        String playerId = "testId";

        Player player = mock(Player.class);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // WHEN
        ClientDequeueResponse response = matchmakingService.dequeue(playerId);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getDequeuedPlayerId()).isEqualTo(playerId);
    }

    @Test
    public void givenMissingId_whenPollMatchmaking_thenThrowException() {
        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.pollMatchmaking(null))
                .withMessage(ApiErrors.MISSING_PLAYER_ID_MESSAGE);
    }

    @Test
    public void givenInvalidId_whenPollMatchmaking_thenThrowException() {
        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.pollMatchmaking("testId"))
                .withMessage(ApiErrors.PLAYER_NOT_FOUND_MESSAGE);
    }

    @Test
    public void givenMatchedPlayer_whenPollMatchmaking_thenReturnMatch() throws ApiException {
        // GIVEN
        String playerId = "testId";

        GameServer gameServer = mock(GameServer.class);
        when(gameServer.getId()).thenReturn("testServerId");
        when(gameServer.getIpV4Address()).thenReturn("127.0.0.1");
        when(gameServer.getPort()).thenReturn(1234);

        Player player = mock(Player.class);
        when(player.getGameServer()).thenReturn(gameServer);
        when(player.getTicket()).thenReturn("testTicket");

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // WHEN
        ClientPollMatchmakingResponse response = matchmakingService.pollMatchmaking(playerId);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getTicket()).isEqualTo(player.getTicket());
        assertThat(response.getServerId()).isEqualTo(gameServer.getId());
        assertThat(response.getIpV4Address()).isEqualTo(gameServer.getIpV4Address());
        assertThat(response.getPort()).isEqualTo(gameServer.getPort());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenExpiredServer_whenPollMatchmaking_thenRemoveServer() throws ApiException {
        // GIVEN
        String playerId = "testId";

        Player player = mock(Player.class);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        GameServer gameServer = mock(GameServer.class);
        when(gameServer.getLastHeartbeat()).thenReturn(OffsetDateTime.now().minusSeconds(MatchmakingService.SERVER_HEARTBEAT_TIMEOUT_SECONDS + 1));
        when(gameServerRepository.findAll()).thenReturn(Lists.newArrayList(gameServer));

        // WHEN
        matchmakingService.pollMatchmaking(playerId);

        // THEN
        ArgumentCaptor<List<GameServer>> argument = ArgumentCaptor.forClass(List.class);
        verify(gameServerRepository).deleteAll(argument.capture());
        List<GameServer> deletedServers = argument.getValue();

        assertThat(deletedServers).isNotNull();
        assertThat(deletedServers).contains(gameServer);
    }

    @Test
    public void givenExpiredMatchedPlayer_whenPollMatchmaking_thenRemovePlayer() throws ApiException {
        // GIVEN
        String playerId = "testId";

        Player player = mock(Player.class);
        when(player.getStatus()).thenReturn(PlayerStatus.MATCHED);
        when(player.getMatchedTime()).thenReturn(OffsetDateTime.now().minusSeconds(MatchmakingService.CLIENT_JOIN_TIMEOUT_SECONDS + 1));

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerRepository.findAll()).thenReturn(Lists.newArrayList(player));

        // WHEN
        matchmakingService.pollMatchmaking(playerId);

        // THEN
        verify(playerRepository).delete(player);
    }

    @Test
    public void givenFullServers_whenPollMatchmaking_thenReturnServersFull() throws ApiException {
        // GIVEN
        String playerId = "testId";

        Player player = mock(Player.class);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // WHEN
        ClientPollMatchmakingResponse response = matchmakingService.pollMatchmaking(playerId);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(MatchmakingStatus.SERVERS_FULL);
    }

    @Test
    public void givenMatchingOpenServer_whenPollMatchmaking_thenAllocatePlayerToServer() throws ApiException {
        // GIVEN
        String playerId = "testId";

        String gameMode = "GM";
        String region = "EU";
        String version = "1.0";

        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(gameMode);
        when(player.getRegion()).thenReturn(region);
        when(player.getVersion()).thenReturn(version);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        GameServer gameServer = mock(GameServer.class);
        ArrayList<Player> allocatedPlayers = new ArrayList<>();
        when(gameServer.getStatus()).thenReturn(ServerStatus.OPEN);
        when(gameServer.getPlayers()).thenReturn(allocatedPlayers);
        when(gameServer.getMaxPlayers()).thenReturn(2);
        when(gameServer.getGameMode()).thenReturn(gameMode);
        when(gameServer.getRegion()).thenReturn(region);
        when(gameServer.getVersion()).thenReturn(version);
        when(gameServer.getLastHeartbeat()).thenReturn(OffsetDateTime.now());

        when(gameServerRepository.findAll()).thenReturn(Lists.newArrayList(gameServer));

        // WHEN
        matchmakingService.pollMatchmaking(playerId);

        // THEN
        verify(player).setTicket(anyString());
        verify(player).setStatus(PlayerStatus.MATCHED);
        verify(player).setGameServer(gameServer);
        verify(player).setMatchedTime(any(OffsetDateTime.class));

        assertThat(allocatedPlayers).containsExactly(player);

        verify(gameServerRepository).save(gameServer);
        verify(playerRepository).save(player);
    }

    @Test
    public void givenMatchingOpenServer_whenPollMatchmaking_thenReturnServer() throws ApiException {
        // GIVEN
        String playerId = "testId";

        String gameMode = "GM";
        String region = "EU";
        String version = "1.0";

        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(gameMode);
        when(player.getRegion()).thenReturn(region);
        when(player.getVersion()).thenReturn(version);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        GameServer gameServer = mock(GameServer.class);
        ArrayList<Player> allocatedPlayers = new ArrayList<>();
        when(gameServer.getId()).thenReturn("testServerId");
        when(gameServer.getStatus()).thenReturn(ServerStatus.OPEN);
        when(gameServer.getPlayers()).thenReturn(allocatedPlayers);
        when(gameServer.getMaxPlayers()).thenReturn(2);
        when(gameServer.getGameMode()).thenReturn(gameMode);
        when(gameServer.getRegion()).thenReturn(region);
        when(gameServer.getVersion()).thenReturn(version);
        when(gameServer.getLastHeartbeat()).thenReturn(OffsetDateTime.now());
        when(gameServer.getIpV4Address()).thenReturn("127.0.0.1");
        when(gameServer.getPort()).thenReturn(1234);

        when(gameServerRepository.findAll()).thenReturn(Lists.newArrayList(gameServer));

        // WHEN
        ClientPollMatchmakingResponse response = matchmakingService.pollMatchmaking(playerId);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getServerId()).isEqualTo(gameServer.getId());
        assertThat(response.getIpV4Address()).isEqualTo(gameServer.getIpV4Address());
        assertThat(response.getPort()).isEqualTo(gameServer.getPort());
        assertThat(response.getStatus()).isEqualTo(MatchmakingStatus.WAITING_FOR_PLAYERS);
    }

    @Test
    public void givenMissingServerId_whenNotifyPlayerJoined_thenThrowException() {
        // GIVEN
        ServerNotifyPlayerJoinedRequest request = mock(ServerNotifyPlayerJoinedRequest.class);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.notifyPlayerJoined(request))
                .withMessage(ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE);
    }

    @Test
    public void givenMissingTicket_whenNotifyPlayerJoined_thenThrowException() {
        // GIVEN
        ServerNotifyPlayerJoinedRequest request = mock(ServerNotifyPlayerJoinedRequest.class);
        when(request.getServerId()).thenReturn("testId");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.notifyPlayerJoined(request))
                .withMessage(ApiErrors.MISSING_TICKET_MESSAGE);
    }

    @Test
    public void givenInvalidServerId_whenNotifyPlayerJoined_thenThrowException() {
        // GIVEN
        ServerNotifyPlayerJoinedRequest request = mock(ServerNotifyPlayerJoinedRequest.class);
        when(request.getServerId()).thenReturn("testServerId");
        when(request.getTicket()).thenReturn("testTicket");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.notifyPlayerJoined(request))
                .withMessage(ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE);
    }

    @Test
    public void givenInvalidPlayerId_whenNotifyPlayerJoined_thenThrowException() {
        // GIVEN
        ServerNotifyPlayerJoinedRequest request = mock(ServerNotifyPlayerJoinedRequest.class);
        when(request.getServerId()).thenReturn("testServerId");
        when(request.getTicket()).thenReturn("testTicket");

        GameServer gameServer = mock(GameServer.class);
        when(gameServerRepository.findById(request.getServerId())).thenReturn(Optional.of(gameServer));

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.notifyPlayerJoined(request))
                .withMessage(ApiErrors.PLAYER_NOT_FOUND_FOR_SERVER_MESSAGE);
    }

    @Test
    public void givenValidPlayerAndServer_whenNotifyPlayerJoined_thenSetPlayerStatus() throws ApiException {
        // GIVEN
        String ticket = "testTicket";
        String serverId = "testServerId";

        ServerNotifyPlayerJoinedRequest request = mock(ServerNotifyPlayerJoinedRequest.class);
        when(request.getTicket()).thenReturn(ticket);
        when(request.getServerId()).thenReturn(serverId);

        GameServer gameServer = mock(GameServer.class);
        Player player = mock(Player.class);
        ArrayList<Player> waitingPlayers = Lists.newArrayList(player);

        when(gameServer.getPlayers()).thenReturn(waitingPlayers);
        when(gameServerRepository.findById(request.getServerId())).thenReturn(Optional.of(gameServer));

        when(player.getTicket()).thenReturn(ticket);

        // WHEN
        matchmakingService.notifyPlayerJoined(request);

        // THEN
        verify(player).setStatus(PlayerStatus.JOINED);
        verify(player).setJoinedTime(any(OffsetDateTime.class));

        verify(playerRepository).save(player);
    }

    @Test
    public void givenValidPlayerAndServer_whenNotifyPlayerJoined_thenReturnResponse() throws ApiException {
        // GIVEN
        String ticket = "testTicket";
        String serverId = "testServerId";

        ServerNotifyPlayerJoinedRequest request = mock(ServerNotifyPlayerJoinedRequest.class);
        when(request.getTicket()).thenReturn(ticket);
        when(request.getServerId()).thenReturn(serverId);

        GameServer gameServer = mock(GameServer.class);
        Player player = mock(Player.class);
        ArrayList<Player> waitingPlayers = Lists.newArrayList(player);

        when(gameServer.getPlayers()).thenReturn(waitingPlayers);
        when(gameServerRepository.findById(request.getServerId())).thenReturn(Optional.of(gameServer));

        when(player.getId()).thenReturn("testPlayer");
        when(player.getTicket()).thenReturn(ticket);

        // WHEN
        ServerNotifyPlayerJoinedResponse response = matchmakingService.notifyPlayerJoined(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getPlayerId()).isEqualTo(player.getId());
        assertThat(response.getServerId()).isEqualTo(serverId);
    }

    @Test
    public void givenMissingServerId_whenNotifyPlayerLeft_thenThrowException() {
        // GIVEN
        ServerNotifyPlayerLeftRequest request = mock(ServerNotifyPlayerLeftRequest.class);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.notifyPlayerLeft(request))
                .withMessage(ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE);
    }

    @Test
    public void givenMissingPlayerId_whenNotifyPlayerLeft_thenThrowException() {
        // GIVEN
        ServerNotifyPlayerLeftRequest request = mock(ServerNotifyPlayerLeftRequest.class);
        when(request.getServerId()).thenReturn("testId");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.notifyPlayerLeft(request))
                .withMessage(ApiErrors.MISSING_PLAYER_ID_MESSAGE);
    }

    @Test
    public void givenInvalidServerId_whenNotifyPlayerLeft_thenThrowException() {
        // GIVEN
        ServerNotifyPlayerLeftRequest request = mock(ServerNotifyPlayerLeftRequest.class);
        when(request.getServerId()).thenReturn("testServerId");
        when(request.getPlayerId()).thenReturn("testPlayerId");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.notifyPlayerLeft(request))
                .withMessage(ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE);
    }

    @Test
    public void givenInvalidPlayerId_whenNotifyPlayerLeft_thenThrowException() {
        // GIVEN
        ServerNotifyPlayerLeftRequest request = mock(ServerNotifyPlayerLeftRequest.class);
        when(request.getServerId()).thenReturn("testServerId");
        when(request.getPlayerId()).thenReturn("testPlayerId");

        GameServer gameServer = mock(GameServer.class);
        when(gameServerRepository.findById(request.getServerId())).thenReturn(Optional.of(gameServer));

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.notifyPlayerLeft(request))
                .withMessage(ApiErrors.PLAYER_NOT_FOUND_FOR_SERVER_MESSAGE);
    }

    @Test
    public void givenValidPlayerAndServer_whenNotifyPlayerLeft_thenRemovePlayer() throws ApiException {
        // GIVEN
        String playerId = "testPlayerId";
        String serverId = "testServerId";

        ServerNotifyPlayerLeftRequest request = mock(ServerNotifyPlayerLeftRequest.class);
        when(request.getPlayerId()).thenReturn(playerId);
        when(request.getServerId()).thenReturn(serverId);

        GameServer gameServer = mock(GameServer.class);
        Player player = mock(Player.class);
        ArrayList<Player> activePlayers = Lists.newArrayList(player);

        when(gameServer.getPlayers()).thenReturn(activePlayers);
        when(gameServerRepository.findById(request.getServerId())).thenReturn(Optional.of(gameServer));

        when(player.getId()).thenReturn(playerId);
        when(player.getGameServer()).thenReturn(gameServer);

        // WHEN
        matchmakingService.notifyPlayerLeft(request);

        // THEN
        assertThat(activePlayers).doesNotContain(player);

        verify(gameServerRepository).save(gameServer);
        verify(playerRepository).delete(player);
    }

    @Test
    public void givenValidPlayerAndServer_whenNotifyPlayerLeft_thenReturnResponse() throws ApiException {
        // GIVEN
        String playerId = "testPlayerId";
        String serverId = "testServerId";

        ServerNotifyPlayerLeftRequest request = mock(ServerNotifyPlayerLeftRequest.class);
        when(request.getPlayerId()).thenReturn(playerId);
        when(request.getServerId()).thenReturn(serverId);

        GameServer gameServer = mock(GameServer.class);
        Player player = mock(Player.class);
        ArrayList<Player> activePlayers = Lists.newArrayList(player);

        when(gameServer.getPlayers()).thenReturn(activePlayers);
        when(gameServerRepository.findById(request.getServerId())).thenReturn(Optional.of(gameServer));

        when(player.getId()).thenReturn(playerId);
        when(player.getGameServer()).thenReturn(gameServer);

        // WHEN
        ServerNotifyPlayerLeftResponse response = matchmakingService.notifyPlayerLeft(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getPlayerId()).isEqualTo(playerId);
        assertThat(response.getServerId()).isEqualTo(serverId);
    }

    @Test
    public void givenMissingServerId_whenSetStatus_thenThrowException() {
        // GIVEN
        ServerSetStatusRequest request = mock(ServerSetStatusRequest.class);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.setStatus(request))
                .withMessage(ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE);
    }

    @Test
    public void givenInvalidServerId_whenSetStatus_thenThrowException() {
        // GIVEN
        ServerSetStatusRequest request = mock(ServerSetStatusRequest.class);
        when(request.getId()).thenReturn("testId");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> matchmakingService.setStatus(request))
                .withMessage(ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE);
    }

    @Test
    public void givenValidServerId_whenSetStatus_thenSetStatus() throws ApiException {
        // GIVEN
        String serverId = "testId";
        ServerStatus newStatus = ServerStatus.CLOSED;

        ServerSetStatusRequest request = mock(ServerSetStatusRequest.class);
        when(request.getId()).thenReturn(serverId);
        when(request.getStatus()).thenReturn(newStatus);

        GameServer gameServer = mock(GameServer.class);
        when(gameServerRepository.findById(serverId)).thenReturn(Optional.of(gameServer));

        // WHEN
        matchmakingService.setStatus(request);

        // THEN
        verify(gameServer).setStatus(newStatus);
        verify(gameServerRepository).save(gameServer);
    }

    @Test
    public void givenValidServerId_whenSetStatus_thenReturnResponse() throws ApiException {
        // GIVEN
        String serverId = "testId";
        ServerStatus newStatus = ServerStatus.CLOSED;

        ServerSetStatusRequest request = mock(ServerSetStatusRequest.class);
        when(request.getId()).thenReturn(serverId);
        when(request.getStatus()).thenReturn(newStatus);

        GameServer gameServer = mock(GameServer.class);
        when(gameServerRepository.findById(serverId)).thenReturn(Optional.of(gameServer));

        // WHEN
        ServerSetStatusResponse response = matchmakingService.setStatus(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(serverId);
        assertThat(response.getStatus()).isEqualTo(newStatus);
    }
}
