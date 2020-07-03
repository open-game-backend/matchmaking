package de.opengamebackend.matchmaking.model.requests;

public class ServerRegisterRequest {
    private String version;
    private String gameMode;
    private String region;
    private String ipV4Address;
    private int port;
    private int maxPlayers;

    public ServerRegisterRequest(String version, String gameMode, String region, String ipV4Address, int port, int maxPlayers) {
        this.version = version;
        this.gameMode = gameMode;
        this.region = region;
        this.ipV4Address = ipV4Address;
        this.port = port;
        this.maxPlayers = maxPlayers;
    }

    public String getVersion() {
        return version;
    }

    public String getGameMode() {
        return gameMode;
    }

    public String getRegion() {
        return region;
    }

    public String getIpV4Address() {
        return ipV4Address;
    }

    public int getPort() {
        return port;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
