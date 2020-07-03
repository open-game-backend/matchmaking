package de.opengamebackend.matchmaking.model.responses;

public class DeregisterGameServerResponse {
    private String removedId;

    public DeregisterGameServerResponse(String removedId) {
        this.removedId = removedId;
    }

    public String getRemovedId() {
        return removedId;
    }

    public void setRemovedId(String removedId) {
        this.removedId = removedId;
    }
}
