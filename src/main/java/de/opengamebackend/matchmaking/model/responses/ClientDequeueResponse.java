package de.opengamebackend.matchmaking.model.responses;

public class ClientDequeueResponse {
    private String dequeuedPlayerId;

    public ClientDequeueResponse(String dequeuedPlayerId) {
        this.dequeuedPlayerId = dequeuedPlayerId;
    }

    public String getDequeuedPlayerId() {
        return dequeuedPlayerId;
    }

    public void setDequeuedPlayerId(String dequeuedPlayerId) {
        this.dequeuedPlayerId = dequeuedPlayerId;
    }
}
