package de.opengamebackend.matchmaking.model.responses;

import de.opengamebackend.matchmaking.model.PlayerStatus;
import de.opengamebackend.matchmaking.model.entities.Player;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class GetQueueResponse {
    private ArrayList<GetQueueResponsePlayer> players;

    public GetQueueResponse(ModelMapper modelMapper, Iterable<Player> players) {
        this.players = new ArrayList<>();

        for (Player player : players) {
            GetQueueResponsePlayer responsePlayer = modelMapper.map(player, GetQueueResponsePlayer.class);
            responsePlayer.setServerId(player.getGameServer() != null ? player.getGameServer().getId() : null);
            this.players.add(responsePlayer);
        }
    }

    public ArrayList<GetQueueResponsePlayer> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<GetQueueResponsePlayer> players) {
        this.players = players;
    }
}
