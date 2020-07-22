package de.opengamebackend.matchmaking.model.responses;

import de.opengamebackend.matchmaking.model.PlayerStatus;

public class ClientEnqueueResponse {
    private final String playerId;
    private final PlayerStatus status;

    public ClientEnqueueResponse(String playerId, PlayerStatus status) {
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
