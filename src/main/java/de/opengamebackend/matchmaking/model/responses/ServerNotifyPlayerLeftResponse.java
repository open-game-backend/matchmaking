package de.opengamebackend.matchmaking.model.responses;

public class ServerNotifyPlayerLeftResponse {
    private String serverId;
    private String playerId;

    public ServerNotifyPlayerLeftResponse(String serverId, String playerId) {
        this.serverId = serverId;
        this.playerId = playerId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
