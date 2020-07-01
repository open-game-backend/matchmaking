package de.opengamebackend.matchmaking.model.requests;

public class RegisterGameServerRequest {
    private String version;
    private String gameMode;
    private String region;
    private String ipV4Address;
    private String port;

    public RegisterGameServerRequest(String version, String gameMode, String region, String ipV4Address, String port) {
        this.version = version;
        this.gameMode = gameMode;
        this.region = region;
        this.ipV4Address = ipV4Address;
        this.port = port;
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

    public String getPort() {
        return port;
    }
}
