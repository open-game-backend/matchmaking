package de.opengamebackend.matchmaking.model.responses;

public class ServerRegisterResponse {
    private String id;

    public ServerRegisterResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
