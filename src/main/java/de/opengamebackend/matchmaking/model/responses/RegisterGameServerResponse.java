package de.opengamebackend.matchmaking.model.responses;

public class RegisterGameServerResponse {
    private String id;

    public RegisterGameServerResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
