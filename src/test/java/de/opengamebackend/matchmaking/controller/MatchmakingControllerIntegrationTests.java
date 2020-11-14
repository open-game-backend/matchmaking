package de.opengamebackend.matchmaking.controller;

import de.opengamebackend.matchmaking.model.ServerStatus;
import de.opengamebackend.matchmaking.model.entities.GameServer;
import de.opengamebackend.matchmaking.model.entities.Player;
import de.opengamebackend.matchmaking.model.requests.*;
import de.opengamebackend.matchmaking.model.responses.*;
import de.opengamebackend.test.HttpRequestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Transactional
public class MatchmakingControllerIntegrationTests {
    private MockMvc mvc;
    private TestEntityManager entityManager;
    private HttpRequestUtils httpRequestUtils;

    private GameServer gameServer;
    private Player player;

    @Autowired
    public MatchmakingControllerIntegrationTests(MockMvc mvc, TestEntityManager entityManager) {
        this.mvc = mvc;
        this.entityManager = entityManager;

        this.httpRequestUtils = new HttpRequestUtils();
    }

    @BeforeEach
    public void beforeEach() {
        this.gameServer = new GameServer();
        this.gameServer.setId("testId");
        this.gameServer.setIpV4Address("1.2.3.4");
        this.gameServer.setPort(8888);
        this.gameServer.setLastHeartbeat(LocalDateTime.now());
        entityManager.persist(this.gameServer);

        this.player = new Player();
        this.player.setPlayerId("awesomePlayer");
        entityManager.persist(player);

        entityManager.flush();
    }

    @Test
    public void whenGetServers_thenOk() throws Exception {
        httpRequestUtils.assertGetOk(mvc, "/servers", GetServersResponse.class);
    }

    @Test
    public void whenGetQueue_thenOk() throws Exception {
        httpRequestUtils.assertGetOk(mvc, "/queue", GetQueueResponse.class);
    }

    @Test
    public void whenRegister_thenOk() throws Exception {
        ServerRegisterRequest request = new ServerRegisterRequest("1.0", "GM", "EU", "127.0.0.1", 1234, 2);
        httpRequestUtils.assertPostOk(mvc, "/server/register", request, ServerRegisterResponse.class);
    }

    @Test
    public void givenServer_whenDeregister_thenOk() throws Exception {
        ServerDeregisterRequest request = new ServerDeregisterRequest(gameServer.getId());
        httpRequestUtils.assertPostOk(mvc, "/server/deregister", request, ServerDeregisterResponse.class);
    }

    @Test
    public void givenServer_whenSendHeartbeat_thenOk() throws Exception {
        ServerSendHeartbeatRequest request = new ServerSendHeartbeatRequest(gameServer.getId());
        httpRequestUtils.assertPostOk(mvc, "/server/sendHeartbeat", request, ServerSendHeartbeatResponse.class);
    }

    @Test
    public void whenEnqueue_thenOk() throws Exception {
        ClientEnqueueRequest request = new ClientEnqueueRequest("testId", "1.0", "GM", "EU");
        httpRequestUtils.assertPostOk(mvc, "/client/enqueue", request, ClientEnqueueResponse.class);
    }

    @Test
    public void givenPlayer_whenDequeue_thenOk() throws Exception {
        ClientDequeueRequest request = new ClientDequeueRequest(player.getPlayerId());
        httpRequestUtils.assertPostOk(mvc, "/client/dequeue", request, ClientDequeueResponse.class);
    }

    @Test
    public void givenPlayer_whenPollMatchmaking_thenOk() throws Exception {
        ClientPollMatchmakingRequest request = new ClientPollMatchmakingRequest(player.getPlayerId());
        httpRequestUtils.assertPostOk(mvc, "/client/pollMatchmaking", request, ClientPollMatchmakingResponse.class);
    }

    @Test
    public void givenServerAndPlayer_whenNotifyPlayerJoined_thenOk() throws Exception {
        gameServer.getPlayers().add(player);
        entityManager.persistAndFlush(gameServer);

        ServerNotifyPlayerJoinedRequest request = new ServerNotifyPlayerJoinedRequest(gameServer.getId(), player.getPlayerId());
        httpRequestUtils.assertPostOk(mvc, "/server/notifyPlayerJoined", request, ServerNotifyPlayerJoinedResponse.class);
    }

    @Test
    public void givenServerAndPlayer_whenNotifyPlayerLeft_thenOk() throws Exception {
        gameServer.getPlayers().add(player);
        entityManager.persistAndFlush(gameServer);

        ServerNotifyPlayerLeftRequest request = new ServerNotifyPlayerLeftRequest(gameServer.getId(), player.getPlayerId());
        httpRequestUtils.assertPostOk(mvc, "/server/notifyPlayerLeft", request, ServerNotifyPlayerLeftResponse.class);
    }

    @Test
    public void givenServer_whenSetStatus_thenOk() throws Exception {
        ServerSetStatusRequest request = new ServerSetStatusRequest(gameServer.getId(), ServerStatus.CLOSED);
        httpRequestUtils.assertPostOk(mvc, "/server/setStatus", request, ServerSetStatusResponse.class);
    }
}
