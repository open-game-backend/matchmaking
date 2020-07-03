package de.opengamebackend.matchmaking.model.repositories;

import de.opengamebackend.matchmaking.model.entities.GameServer;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GameServerRepository extends CrudRepository<GameServer, String> {
    List<GameServer> findAll();
}
