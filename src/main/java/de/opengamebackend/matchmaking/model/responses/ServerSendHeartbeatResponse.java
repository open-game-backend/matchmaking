package de.opengamebackend.matchmaking.model.responses;

public class ServerSendHeartbeatResponse {
    private String updatedId;

    public ServerSendHeartbeatResponse(String updatedId) {
        this.updatedId = updatedId;
    }

    public String getUpdatedId() {
        return updatedId;
    }

    public void setUpdatedId(String updatedId) {
        this.updatedId = updatedId;
    }
}
