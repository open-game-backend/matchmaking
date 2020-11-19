package de.opengamebackend.matchmaking.controller;

import de.opengamebackend.matchmaking.model.requests.*;
import de.opengamebackend.matchmaking.model.responses.*;
import de.opengamebackend.net.ApiErrors;
import de.opengamebackend.net.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Gets all game servers that are currently available for matchmaking.")
    public ResponseEntity<GetServersResponse> getServers() {
        GetServersResponse response = matchmakingService.getServers();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/queue")
    @Operation(summary = "Gets all players that are currently queued for matchmaking.")
    public ResponseEntity<GetQueueResponse> getQueue() {
        GetQueueResponse response = matchmakingService.getQueue();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/register")
    @Operation(summary = "Registers the specified server, accepting players for matchmaking.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Server registered.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServerRegisterResponse.class)) }),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.MISSING_GAME_MODE_CODE + ": " + ApiErrors.MISSING_GAME_MODE_MESSAGE + "<br />" +
                            "Error " + ApiErrors.MISSING_IPV4_ADDRESS_CODE + ": " + ApiErrors.MISSING_IPV4_ADDRESS_MESSAGE + "<br />" +
                            "Error " + ApiErrors.MISSING_REGION_CODE + ": " + ApiErrors.MISSING_REGION_MESSAGE + "<br />" +
                            "Error " + ApiErrors.MISSING_VERSION_CODE + ": " + ApiErrors.MISSING_VERSION_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<ServerRegisterResponse> register(@RequestBody ServerRegisterRequest request)
            throws ApiException {
        ServerRegisterResponse response = matchmakingService.register(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/deregister")
    @Operation(summary = "Deregisters the specified server, no longer accepting players for matchmaking.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Server deregistered.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServerDeregisterResponse.class)) }),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.MISSING_GAME_SERVER_ID_CODE + ": " + ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE + "<br />" +
                            "Error " + ApiErrors.GAME_SERVER_NOT_FOUND_CODE + ": " + ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<ServerDeregisterResponse> deregister(@RequestBody ServerDeregisterRequest request)
            throws ApiException {
        ServerDeregisterResponse response = matchmakingService.deregister(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/sendHeartbeat")
    @Operation(summary = "Sends a heartbeat for the specified server, allowing it to keep accepting players for matchmaking.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Server heartbeat received.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServerSendHeartbeatResponse.class)) }),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.MISSING_GAME_SERVER_ID_CODE + ": " + ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE + "<br />" +
                            "Error " + ApiErrors.GAME_SERVER_NOT_FOUND_CODE + ": " + ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<ServerSendHeartbeatResponse> sendHeartbeat(@RequestBody ServerSendHeartbeatRequest request)
            throws ApiException {
        ServerSendHeartbeatResponse response = matchmakingService.sendHeartbeat(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/client/enqueue")
    @Operation(summary = "Enqueues the specified player for matchmaking.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Player enqueued.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClientEnqueueResponse.class)) }),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.MISSING_PLAYER_ID_CODE + ": " + ApiErrors.MISSING_PLAYER_ID_MESSAGE + "<br />" +
                            "Error " + ApiErrors.MISSING_GAME_MODE_CODE + ": " + ApiErrors.MISSING_GAME_MODE_MESSAGE + "<br />" +
                            "Error " + ApiErrors.MISSING_REGION_CODE + ": " + ApiErrors.MISSING_REGION_MESSAGE + "<br />" +
                            "Error " + ApiErrors.MISSING_VERSION_CODE + ": " + ApiErrors.MISSING_VERSION_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<ClientEnqueueResponse> enqueue(@RequestBody ClientEnqueueRequest request)
            throws ApiException {
        ClientEnqueueResponse response = matchmakingService.enqueue(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/client/dequeue")
    @Operation(summary = "Dequeues the specified player from matchmaking.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Player dequeued.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClientDequeueResponse.class)) }),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.MISSING_PLAYER_ID_CODE + ": " + ApiErrors.MISSING_PLAYER_ID_MESSAGE + "<br />" +
                            "Error " + ApiErrors.PLAYER_NOT_FOUND_CODE + ": " + ApiErrors.PLAYER_NOT_FOUND_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<ClientDequeueResponse> dequeue(@RequestBody ClientDequeueRequest request)
            throws ApiException {
        ClientDequeueResponse response = matchmakingService.dequeue(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/client/pollMatchmaking")
    @Operation(summary = "Checks whether a match for the specified player has been found.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Match found, waiting for players, or servers full (see 'status').",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClientPollMatchmakingResponse.class)) }),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.MISSING_PLAYER_ID_CODE + ": " + ApiErrors.MISSING_PLAYER_ID_MESSAGE + "<br />" +
                            "Error " + ApiErrors.PLAYER_NOT_FOUND_CODE + ": " + ApiErrors.PLAYER_NOT_FOUND_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<ClientPollMatchmakingResponse> pollMatchmaking(@RequestBody ClientPollMatchmakingRequest request)
            throws ApiException {
        ClientPollMatchmakingResponse response = matchmakingService.pollMatchmaking(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/notifyPlayerJoined")
    @Operation(summary = "Notifies the matchmaker that a matched player has joined the server.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Player status updated.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServerNotifyPlayerJoinedResponse.class)) }),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.MISSING_GAME_SERVER_ID_CODE + ": " + ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE + "<br />" +
                            "Error " + ApiErrors.GAME_SERVER_NOT_FOUND_CODE + ": " + ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE  + "<br />" +
                            "Error " + ApiErrors.MISSING_PLAYER_ID_CODE + ": " + ApiErrors.MISSING_PLAYER_ID_MESSAGE + "<br />" +
                            "Error " + ApiErrors.PLAYER_NOT_FOUND_FOR_SERVER_CODE + ": " + ApiErrors.PLAYER_NOT_FOUND_FOR_SERVER_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<ServerNotifyPlayerJoinedResponse> notifyPlayerJoined(@RequestBody ServerNotifyPlayerJoinedRequest request)
            throws ApiException {
        ServerNotifyPlayerJoinedResponse response = matchmakingService.notifyPlayerJoined(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/notifyPlayerLeft")
    @Operation(summary = "Notifies the matchmaker that a player has left the server.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Player removed.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServerNotifyPlayerLeftResponse.class)) }),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.MISSING_GAME_SERVER_ID_CODE + ": " + ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE + "<br />" +
                            "Error " + ApiErrors.GAME_SERVER_NOT_FOUND_CODE + ": " + ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE  + "<br />" +
                            "Error " + ApiErrors.MISSING_PLAYER_ID_CODE + ": " + ApiErrors.MISSING_PLAYER_ID_MESSAGE + "<br />" +
                            "Error " + ApiErrors.PLAYER_NOT_FOUND_FOR_SERVER_CODE + ": " + ApiErrors.PLAYER_NOT_FOUND_FOR_SERVER_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<ServerNotifyPlayerLeftResponse> notifyPlayerLeft(@RequestBody ServerNotifyPlayerLeftRequest request)
            throws ApiException {
        ServerNotifyPlayerLeftResponse response = matchmakingService.notifyPlayerLeft(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/server/setStatus")
    @Operation(summary = "Sets whether the specified server is currently accepting new players, or not.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Server status updated.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServerSetStatusResponse.class)) }),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.MISSING_GAME_SERVER_ID_CODE + ": " + ApiErrors.MISSING_GAME_SERVER_ID_MESSAGE + "<br />" +
                            "Error " + ApiErrors.GAME_SERVER_NOT_FOUND_CODE + ": " + ApiErrors.GAME_SERVER_NOT_FOUND_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<ServerSetStatusResponse> setStatus(@RequestBody ServerSetStatusRequest request)
            throws ApiException {
        ServerSetStatusResponse response = matchmakingService.setStatus(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
