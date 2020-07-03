package de.opengamebackend.matchmaking.model.responses;

public class SendHeartbeatResponse {
    private String updatedId;

    public SendHeartbeatResponse(String updatedId) {
        this.updatedId = updatedId;
    }

    public String getUpdatedId() {
        return updatedId;
    }

    public void setUpdatedId(String updatedId) {
        this.updatedId = updatedId;
    }
}
