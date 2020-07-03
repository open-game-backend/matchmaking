package de.opengamebackend.matchmaking.model.requests;

public class ClientDequeueRequest {
    private String playerId;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
