package de.opengamebackend.matchmaking.model.entities;

import de.opengamebackend.matchmaking.model.PlayerStatus;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "matchmaking_player")
public class Player {
    @Id
    private String id;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String gameMode;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private PlayerStatus status;

    @ManyToOne
    private GameServer gameServer;

    private String ticket;
    private OffsetDateTime matchedTime;
    private OffsetDateTime joinedTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
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
