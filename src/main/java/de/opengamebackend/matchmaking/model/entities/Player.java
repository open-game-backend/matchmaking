package de.opengamebackend.matchmaking.model.entities;

import de.opengamebackend.matchmaking.model.PlayerStatus;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.OffsetDateTime;

@Entity
public class Player {
    @Id
    private String playerId;

    private String version;
    private String gameMode;
    private String region;
    private PlayerStatus status;

    @ManyToOne
    private GameServer gameServer;
    private OffsetDateTime matchedTime;
    private OffsetDateTime joinedTime;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public GameServer getGameServer() {
        return gameServer;
    }

    public void setGameServer(GameServer gameServer) {
        this.gameServer = gameServer;
    }

    public OffsetDateTime getMatchedTime() {
        return matchedTime;
    }

    public void setMatchedTime(OffsetDateTime matchedTime) {
        this.matchedTime = matchedTime;
    }

    public OffsetDateTime getJoinedTime() {
        return joinedTime;
    }

    public void setJoinedTime(OffsetDateTime joinedTime) {
        this.joinedTime = joinedTime;
    }
}
