package de.opengamebackend.matchmaking.controller;

import de.opengamebackend.net.ApiError;

public class ApiErrors {
    public static final ApiError ERROR_MISSING_GAME_MODE =
            new ApiError(100, "Missing game mode.");
    public static final ApiError ERROR_MISSING_IPV4_ADDRESS =
            new ApiError(101, "Missing IPv4 address.");
    public static final ApiError ERROR_MISSING_REGION =
            new ApiError(103, "Missing region.");
    public static final ApiError ERROR_MISSING_VERSION =
            new ApiError(104, "Missing version.");
    public static final ApiError ERROR_MISSING_GAME_SERVER_ID =
            new ApiError(105, "Missing game server id.");
    public static final ApiError ERROR_GAME_SERVER_NOT_FOUND =
            new ApiError(106, "Game server not found.");
    public static final ApiError ERROR_MISSING_PLAYER_ID =
            new ApiError(107, "Missing player id.");
    public static final ApiError ERROR_PLAYER_NOT_FOUND =
            new ApiError(108, "Player not found.");
    public static final ApiError ERROR_PLAYER_NOT_FOUND_FOR_SERVER =
            new ApiError(109, "Player not found for server.");
}
