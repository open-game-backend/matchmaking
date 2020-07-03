package de.opengamebackend.matchmaking.model.repositories;

import de.opengamebackend.matchmaking.model.entities.Player;
import org.springframework.data.repository.CrudRepository;

public interface PlayerRepository extends CrudRepository<Player, String> {
}
