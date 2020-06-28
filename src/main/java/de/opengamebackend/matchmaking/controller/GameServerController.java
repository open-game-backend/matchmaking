package de.opengamebackend.matchmaking.controller;

import de.opengamebackend.matchmaking.model.entities.GameServer;
import de.opengamebackend.matchmaking.model.repositories.GameServerRepository;
import de.opengamebackend.matchmaking.model.requests.RegisterGameServerRequest;
import de.opengamebackend.matchmaking.model.responses.RegisterGameServerResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    public RegisterGameServerResponse register(@RequestBody RegisterGameServerRequest request) {
        GameServer gameServer = modelMapper.map(request, GameServer.class);
        gameServer.setId(UUID.randomUUID().toString());
        repository.save(gameServer);

        RegisterGameServerResponse response = new RegisterGameServerResponse(gameServer.getId());
        return response;
    }
}
