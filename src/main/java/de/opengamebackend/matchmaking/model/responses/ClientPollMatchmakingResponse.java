package de.opengamebackend.matchmaking.model.responses;

import de.opengamebackend.matchmaking.model.MatchmakingStatus;

public class ClientPollMatchmakingResponse {
    private String serverId;
    private String ipV4Address;
    private int port;
    private MatchmakingStatus status;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getIpV4Address() {
        return ipV4Address;
    }

    public void setIpV4Address(String ipV4Address) {
        this.ipV4Address = ipV4Address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public MatchmakingStatus getStatus() {
        return status;
    }

    public void setStatus(MatchmakingStatus status) {
        this.status = status;
    }
}
