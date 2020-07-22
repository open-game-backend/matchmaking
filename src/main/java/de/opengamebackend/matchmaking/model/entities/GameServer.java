package de.opengamebackend.matchmaking.model.entities;

import de.opengamebackend.matchmaking.model.ServerStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime lastHeartbeat;
    private ServerStatus status;

    @OneToMany(mappedBy="gameServer")
    private List<Player> players;

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

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public ServerStatus getStatus() {
        return status;
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

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public void setStatus(ServerStatus status) {
        this.status = status;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }
}
