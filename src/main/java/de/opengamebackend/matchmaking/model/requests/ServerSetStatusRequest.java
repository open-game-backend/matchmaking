package de.opengamebackend.matchmaking.model.requests;

import de.opengamebackend.matchmaking.model.ServerStatus;

public class ServerSetStatusRequest {
    private String id;
    private ServerStatus status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ServerStatus getStatus() {
        return status;
    }

    public void setStatus(ServerStatus status) {
        this.status = status;
    }
}
