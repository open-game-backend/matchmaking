package de.opengamebackend.matchmaking.model.responses;

import de.opengamebackend.matchmaking.model.PlayerStatus;

public class EnqueueResponse {
    private String playerId;
    private PlayerStatus status;

    public EnqueueResponse(String playerId, PlayerStatus status) {
        this.playerId = playerId;
        this.status = status;
    }

    public String getPlayerId() {
        return playerId;
    }

    public PlayerStatus getStatus() {
        return status;
    }
}
