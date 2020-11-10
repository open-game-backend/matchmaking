package de.opengamebackend.matchmaking.controller;

import de.opengamebackend.matchmaking.model.entities.GameServer;
import de.opengamebackend.matchmaking.model.entities.Player;
import de.opengamebackend.matchmaking.model.repositories.GameServerRepository;
import de.opengamebackend.matchmaking.model.repositories.PlayerRepository;
import de.opengamebackend.matchmaking.model.responses.GetQueueResponse;
import de.opengamebackend.matchmaking.model.responses.GetQueueResponsePlayer;
import de.opengamebackend.matchmaking.model.responses.GetServersResponse;
import de.opengamebackend.matchmaking.model.responses.GetServersResponseServer;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MatchmakingServiceTests {
    private GameServerRepository gameServerRepository;
    private PlayerRepository playerRepository;
    private ModelMapper modelMapper;

    private MatchmakingService matchmakingService;

    @BeforeEach
    public void setUp() {
        gameServerRepository = mock(GameServerRepository.class);
        playerRepository = mock(PlayerRepository.class);
        modelMapper = mock(ModelMapper.class);

        matchmakingService = new MatchmakingService(gameServerRepository, playerRepository, modelMapper);
    }

    @Test
    public void givenServers_whenGetServers_thenReturnServers() {
        // GIVEN
        String playerId1 = "TestPlayer1";
        String playerId2 = "TestPlayer2";

        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);

        when(p1.getPlayerId()).thenReturn(playerId1);
        when(p2.getPlayerId()).thenReturn(playerId2);

        GameServer s1 = mock(GameServer.class);
        GameServer s2 = mock(GameServer.class);

        when(s1.getPlayers()).thenReturn(Lists.newArrayList(p1));
        when(s2.getPlayers()).thenReturn(Lists.newArrayList(p2));

        when(gameServerRepository.findAll()).thenReturn(Lists.newArrayList(s1, s2));

        GetServersResponseServer r1 = mock(GetServersResponseServer.class);
        GetServersResponseServer r2 = mock(GetServersResponseServer.class);

        when(modelMapper.map(s1, GetServersResponseServer.class)).thenReturn(r1);
        when(modelMapper.map(s2, GetServersResponseServer.class)).thenReturn(r2);

        // WHEN
        GetServersResponse response = matchmakingService.getServers();

        // THEN
        assertThat(response.getServers()).isNotNull();
        assertThat(response.getServers()).containsExactly(r1, r2);

        verify(r1).setPlayerIds(Lists.newArrayList(playerId1));
        verify(r2).setPlayerIds(Lists.newArrayList(playerId2));
    }

    @Test
    public void givenPlayers_whenGetQueue_thenReturnPlayers() {
        // GIVEN
        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);

        when(playerRepository.findAll()).thenReturn(Lists.newArrayList(p1, p2));

        String testServerId = "TestServer";

        GameServer testServer = mock(GameServer.class);

        when(testServer.getId()).thenReturn(testServerId);
        when(p1.getGameServer()).thenReturn(testServer);

        GetQueueResponsePlayer r1 = mock(GetQueueResponsePlayer.class);
        GetQueueResponsePlayer r2 = mock(GetQueueResponsePlayer.class);

        when(modelMapper.map(p1, GetQueueResponsePlayer.class)).thenReturn(r1);
        when(modelMapper.map(p2, GetQueueResponsePlayer.class)).thenReturn(r2);

        // WHEN
        GetQueueResponse response = matchmakingService.getQueue();

        // THEN
        assertThat(response.getPlayers()).isNotNull();
        assertThat(response.getPlayers()).containsExactly(r1, r2);

        verify(r1).setServerId(testServerId);
    }
}
