package de.opengamebackend.matchmaking.model.responses;

import de.opengamebackend.matchmaking.model.entities.GameServer;
import de.opengamebackend.matchmaking.model.entities.Player;
import org.modelmapper.ModelMapper;

import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GetServersResponse {
    private ArrayList<GetServersResponseServer> servers;

    public GetServersResponse(ModelMapper modelMapper, Iterable<GameServer> servers) {
        this.servers = new ArrayList<>();

        for (GameServer server : servers) {
            GetServersResponseServer responseServer = modelMapper.map(server, GetServersResponseServer.class);

            ArrayList<String> playerIds = new ArrayList<>();

            for (Player player : server.getPlayers()) {
                playerIds.add(player.getPlayerId());
            }

            responseServer.setPlayerIds(playerIds);

            this.servers.add(responseServer);
        }
    }

    public ArrayList<GetServersResponseServer> getServers() {
        return servers;
    }

    public void setServers(ArrayList<GetServersResponseServer> servers) {
        this.servers = servers;
    }
}
