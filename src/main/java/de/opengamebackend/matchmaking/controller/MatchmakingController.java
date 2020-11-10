package de.opengamebackend.matchmaking.controller;

import de.opengamebackend.matchmaking.model.requests.*;
import de.opengamebackend.matchmaking.model.responses.*;
import de.opengamebackend.net.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MatchmakingController {
    private MatchmakingService matchmakingService;

    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @GetMapping("/servers")
    public ResponseEntity<GetServersResponse> getServers() {
        GetServersResponse response = matchmakingService.getServers();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/queue")
    public ResponseEntity<GetQueueResponse> getQueue() {
        GetQueueResponse response = matchmakingService.getQueue();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/register")
    public ResponseEntity<ServerRegisterResponse> register(@RequestBody ServerRegisterRequest request)
            throws ApiException {
        ServerRegisterResponse response = matchmakingService.register(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/deregister")
    public ResponseEntity<ServerDeregisterResponse> deregister(@RequestBody ServerDeregisterRequest request)
            throws ApiException {
        ServerDeregisterResponse response = matchmakingService.deregister(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/sendHeartbeat")
    public ResponseEntity<ServerSendHeartbeatResponse> sendHeartbeat(@RequestBody ServerSendHeartbeatRequest request)
            throws ApiException {
        ServerSendHeartbeatResponse response = matchmakingService.sendHeartbeat(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/client/enqueue")
    public ResponseEntity<ClientEnqueueResponse> enqueue(@RequestBody ClientEnqueueRequest request)
            throws ApiException {
        ClientEnqueueResponse response = matchmakingService.enqueue(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/client/dequeue")
    public ResponseEntity<ClientDequeueResponse> dequeue(@RequestBody ClientDequeueRequest request)
            throws ApiException {
        ClientDequeueResponse response = matchmakingService.dequeue(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/client/pollMatchmaking")
    public ResponseEntity<ClientPollMatchmakingResponse> pollMatchmaking(@RequestBody ClientPollMatchmakingRequest request)
            throws ApiException {
        ClientPollMatchmakingResponse response = matchmakingService.pollMatchmaking(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/notifyPlayerJoined")
    public ResponseEntity<ServerNotifyPlayerJoinedResponse> notifyPlayerJoined(@RequestBody ServerNotifyPlayerJoinedRequest request)
            throws ApiException {
        ServerNotifyPlayerJoinedResponse response = matchmakingService.notifyPlayerJoined(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/notifyPlayerLeft")
    public ResponseEntity<ServerNotifyPlayerLeftResponse> notifyPlayerLeft(@RequestBody ServerNotifyPlayerLeftRequest request)
            throws ApiException {
        ServerNotifyPlayerLeftResponse response = matchmakingService.notifyPlayerLeft(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/setStatus")
    public ResponseEntity<ServerSetStatusResponse> setStatus(@RequestBody ServerSetStatusRequest request)
            throws ApiException {
        ServerSetStatusResponse response = matchmakingService.setStatus(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
