package de.opengamebackend.matchmaking.controller;

import com.google.common.base.Strings;
import de.opengamebackend.matchmaking.model.PlayerStatus;
import de.opengamebackend.matchmaking.model.entities.GameServer;
import de.opengamebackend.matchmaking.model.entities.Player;
import de.opengamebackend.matchmaking.model.repositories.GameServerRepository;
import de.opengamebackend.matchmaking.model.repositories.PlayerRepository;
import de.opengamebackend.matchmaking.model.requests.DeregisterGameServerRequest;
import de.opengamebackend.matchmaking.model.requests.EnqueueRequest;
import de.opengamebackend.matchmaking.model.requests.RegisterGameServerRequest;
import de.opengamebackend.matchmaking.model.requests.SendHeartbeatRequest;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RestController
public class GameServerController {
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
    private static final ErrorResponse ERROR_MISSING_GAME_SERVER_NOT_FOUND =
            new ErrorResponse(106, "Game server not found.");
    private static final ErrorResponse ERROR_MISSING_PLAYER_ID =
            new ErrorResponse(107, "Missing player id.");

    @Autowired
    GameServerRepository gameServerRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/get")
    public Iterable<GameServer> get() {
        return gameServerRepository.findAll();
    }

    @GetMapping("/getQueue")
    public Iterable<Player> getQueue() {
        return playerRepository.findAll();
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterGameServerRequest request) {
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
        }

        gameServer.setLastHeartbeat(LocalDateTime.now());

        gameServerRepository.save(gameServer);

        RegisterGameServerResponse response = new RegisterGameServerResponse(gameServer.getId());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/deregister")
    public ResponseEntity deregister(@RequestBody DeregisterGameServerRequest request) {
        if (Strings.isNullOrEmpty(request.getId())) {
            return new ResponseEntity(ERROR_MISSING_GAME_SERVER_ID, HttpStatus.BAD_REQUEST);
        }

        Optional<GameServer> gameServer = gameServerRepository.findById(request.getId());

        if (!gameServer.isPresent()) {
            return new ResponseEntity(ERROR_MISSING_GAME_SERVER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        gameServerRepository.deleteById(request.getId());

        DeregisterGameServerResponse response = new DeregisterGameServerResponse(request.getId());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/sendHeartbeat")
    public ResponseEntity sendHeartbeat(@RequestBody SendHeartbeatRequest request) {
        if (Strings.isNullOrEmpty(request.getId())) {
            return new ResponseEntity(ERROR_MISSING_GAME_SERVER_ID, HttpStatus.BAD_REQUEST);
        }

        Optional<GameServer> optionalGameServer = gameServerRepository.findById(request.getId());

        if (!optionalGameServer.isPresent()) {
            return new ResponseEntity(ERROR_MISSING_GAME_SERVER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        GameServer gameServer = optionalGameServer.get();
        gameServer.setLastHeartbeat(LocalDateTime.now());
        gameServerRepository.save(gameServer);

        SendHeartbeatResponse response = new SendHeartbeatResponse(request.getId());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/enqueue")
    public ResponseEntity enqueue(@RequestBody EnqueueRequest request) {
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
        playerRepository.save(player);

        EnqueueResponse response = new EnqueueResponse(request.getPlayerId(), player.getStatus());
        return new ResponseEntity(response, HttpStatus.OK);
    }
}
