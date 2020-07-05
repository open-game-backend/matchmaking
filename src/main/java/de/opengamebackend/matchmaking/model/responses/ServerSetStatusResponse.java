package de.opengamebackend.matchmaking.model.responses;

import de.opengamebackend.matchmaking.model.ServerStatus;

public class ServerSetStatusResponse {
    private String id;
    private ServerStatus status;

    public ServerSetStatusResponse(String id, ServerStatus status) {
        this.id = id;
        this.status = status;
    }

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
