package de.opengamebackend.matchmaking.controller;

import de.opengamebackend.matchmaking.model.PlayerStatus;
import de.opengamebackend.matchmaking.model.ServerStatus;
import de.opengamebackend.matchmaking.model.entities.GameServer;
import de.opengamebackend.matchmaking.model.entities.Player;
import de.opengamebackend.matchmaking.model.repositories.GameServerRepository;
import de.opengamebackend.matchmaking.model.repositories.PlayerRepository;
import de.opengamebackend.matchmaking.model.requests.*;
import de.opengamebackend.matchmaking.model.responses.*;
import de.opengamebackend.net.ApiException;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

        when(p1.getPlayerId()).thenReturn("TestPlayer1");
        when(p2.getPlayerId()).thenReturn("TestPlayer2");

        GameServer gameServer = mock(GameServer.class);

        when(gameServer.getId()).thenReturn("testId");
        when(gameServer.getGameMode()).thenReturn("GM");
        when(gameServer.getIpV4Address()).thenReturn("127.0.0.1");
        when(gameServer.getRegion()).thenReturn("EU");
        when(gameServer.getVersion()).thenReturn("1.0");
        when(gameServer.getPort()).thenReturn(1234);
        when(gameServer.getMaxPlayers()).thenReturn(2);
        when(gameServer.getLastHeartbeat()).thenReturn(LocalDateTime.now());
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
        assertThat(response.getServers().get(0).getPlayerIds()).containsExactly(p1.getPlayerId(), p2.getPlayerId());
    }

    @Test
    public void givenPlayers_whenGetQueue_thenReturnPlayers() {
        // GIVEN
        GameServer gameServer = mock(GameServer.class);
        when(gameServer.getId()).thenReturn("testServerId");

        Player player = mock(Player.class);
        when(player.getPlayerId()).thenReturn("testPlayerId");
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
        assertThat(response.getPlayers().get(0).getPlayerId()).isEqualTo(player.getPlayerId());
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
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.register(request));
    }

    @Test
    public void givenMissingIpAddress_whenRegister_thenThrowException() {
        // GIVEN
        ServerRegisterRequest request = new ServerRegisterRequest("1.0", "GM", "EU", "", 1234, 2);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.register(request));
    }

    @Test
    public void givenMissingRegion_whenRegister_thenThrowException() {
        // GIVEN
        ServerRegisterRequest request = new ServerRegisterRequest("1.0", "GM", "", "127.0.0.1", 1234, 2);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.register(request));
    }

    @Test
    public void givenMissingVersion_whenRegister_thenThrowException() {
        // GIVEN
        ServerRegisterRequest request = new ServerRegisterRequest("", "GM", "EU", "127.0.0.1", 1234, 2);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.register(request));
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
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.deregister(request));
    }

    @Test
    public void givenInvalidId_whenDeregister_thenThrowException() {
        // GIVEN
        ServerDeregisterRequest request = mock(ServerDeregisterRequest.class);
        when(request.getId()).thenReturn("testId");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.deregister(request));
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
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.sendHeartbeat(request));
    }

    @Test
    public void givenInvalidId_whenSendHeartbeat_thenThrowException() {
        // GIVEN
        ServerSendHeartbeatRequest request = mock(ServerSendHeartbeatRequest.class);
        when(request.getId()).thenReturn("testId");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.sendHeartbeat(request));
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
        ClientEnqueueRequest request = new ClientEnqueueRequest("testId", "1.0", "", "EU");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.enqueue(request));
    }

    @Test
    public void givenMissingPlayerId_whenEnqueue_thenThrowException() {
        // GIVEN
        ClientEnqueueRequest request = new ClientEnqueueRequest("", "1.0", "GM", "EU");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.enqueue(request));
    }

    @Test
    public void givenMissingRegion_whenEnqueue_thenThrowException() {
        // GIVEN
        ClientEnqueueRequest request = new ClientEnqueueRequest("testId", "1.0", "GM", "");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.enqueue(request));
    }

    @Test
    public void givenMissingVersion_whenEnqueue_thenThrowException() {
        // GIVEN
        ClientEnqueueRequest request = new ClientEnqueueRequest("testId", "", "GM", "EU");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.enqueue(request));
    }

    @Test
    public void givenPlayer_whenEnqueue_thenPlayerIsSaved() throws ApiException {
        // GIVEN
        ClientEnqueueRequest request = new ClientEnqueueRequest("testId", "1.0", "GM", "EU");

        // WHEN
        matchmakingService.enqueue(request);

        // THEN
        ArgumentCaptor<Player> argument = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(argument.capture());

        Player player = argument.getValue();

        assertThat(player.getPlayerId()).isEqualTo(request.getPlayerId());
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
        ClientEnqueueRequest request = new ClientEnqueueRequest("testId", "1.0", "GM", "EU");

        Player player = mock(Player.class);
        GameServer gameServer = mock(GameServer.class);

        when(player.getGameServer()).thenReturn(gameServer);

        ArrayList<Player> players = Lists.newArrayList(player);
        when(gameServer.getPlayers()).thenReturn(players);

        when(playerRepository.findById(request.getPlayerId())).thenReturn(Optional.of(player));

        // WHEN
        matchmakingService.enqueue(request);

        // THEN
        assertThat(players).doesNotContain(player);
        verify(gameServerRepository).save(gameServer);
    }

    @Test
    public void givenPlayer_whenEnqueue_thenReturnId() throws ApiException {
        // GIVEN
        ClientEnqueueRequest request = new ClientEnqueueRequest("testId", "1.0", "GM", "EU");

        // WHEN
        ClientEnqueueResponse response = matchmakingService.enqueue(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getPlayerId()).isEqualTo(request.getPlayerId());
    }

    @Test
    public void givenMissingId_whenDequeue_thenThrowException() {
        // GIVEN
        ClientDequeueRequest request = mock(ClientDequeueRequest.class);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.dequeue(request));
    }

    @Test
    public void givenInvalidId_whenDequeue_thenThrowException() {
        // GIVEN
        ClientDequeueRequest request = mock(ClientDequeueRequest.class);
        when(request.getPlayerId()).thenReturn("testId");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> matchmakingService.dequeue(request));
    }

    @Test
    public void givenValidId_whenDequeue_thenDeletePlayer() throws ApiException {
        // GIVEN
        ClientDequeueRequest request = mock(ClientDequeueRequest.class);
        when(request.getPlayerId()).thenReturn("testId");

        Player player = mock(Player.class);
        when(playerRepository.findById(request.getPlayerId())).thenReturn(Optional.of(player));

        // WHEN
        matchmakingService.dequeue(request);

        // THEN
        verify(playerRepository).delete(player);
    }

    @Test
    public void givenValidId_whenDequeue_thenReturnId() throws ApiException {
        // GIVEN
        ClientDequeueRequest request = mock(ClientDequeueRequest.class);
        when(request.getPlayerId()).thenReturn("testId");

        Player player = mock(Player.class);
        when(playerRepository.findById(request.getPlayerId())).thenReturn(Optional.of(player));

        // WHEN
        ClientDequeueResponse response = matchmakingService.dequeue(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getDequeuedPlayerId()).isEqualTo(request.getPlayerId());
    }
}
