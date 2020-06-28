package de.opengamebackend.matchmaking.controller;

import com.google.common.base.Strings;
import de.opengamebackend.matchmaking.model.entities.GameServer;
import de.opengamebackend.matchmaking.model.repositories.GameServerRepository;
import de.opengamebackend.matchmaking.model.requests.RegisterGameServerRequest;
import de.opengamebackend.matchmaking.model.responses.ErrorResponse;
import de.opengamebackend.matchmaking.model.responses.RegisterGameServerResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class GameServerController {
    @Autowired
    GameServerRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/get")
    public List<GameServer> get() {
        return repository.findAll();
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterGameServerRequest request) {
        if (Strings.isNullOrEmpty(request.getGameMode())) {
            ErrorResponse response = new ErrorResponse(100, "Missing game mode.");
            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(request.getIpV4Address())) {
            ErrorResponse response = new ErrorResponse(101, "Missing IPv4 address.");
            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(request.getPort())) {
            ErrorResponse response = new ErrorResponse(102, "Missing port.");
            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(request.getRegion())) {
            ErrorResponse response = new ErrorResponse(103, "Missing region.");
            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(request.getVersion())) {
            ErrorResponse response = new ErrorResponse(104, "Missing version.");
            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        GameServer gameServer = modelMapper.map(request, GameServer.class);
        gameServer.setId(UUID.randomUUID().toString());
        repository.save(gameServer);

        RegisterGameServerResponse response = new RegisterGameServerResponse(gameServer.getId());
        return new ResponseEntity(response, HttpStatus.OK);
    }
}
