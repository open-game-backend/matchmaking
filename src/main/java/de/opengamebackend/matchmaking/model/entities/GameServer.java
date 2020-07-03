package de.opengamebackend.matchmaking.model.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class GameServer {
    @Id
    private String id;

    private String version;
    private String gameMode;
    private String region;
    private String ipV4Address;
    private int port;
    private int maxPlayers;

    public String getId() {
        return id;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setIpV4Address(String ipV4Address) {
        this.ipV4Address = ipV4Address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
}
