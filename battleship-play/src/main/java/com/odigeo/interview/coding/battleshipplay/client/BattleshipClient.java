package com.odigeo.interview.coding.battleshipplay.client;

import com.google.gson.Gson;
import com.odigeo.interview.coding.battleshipapi.contract.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * HTTP client for interacting with the Battleship REST API
 */
public class BattleshipClient {

    @SuppressWarnings("java:S1075") // URL is localhost configuration for CLI app
    private static final String BASE_URL = "http://localhost:8080/battleship-service/api";
    @SuppressWarnings("java:S1075") // Path constant for API endpoints
    private static final String GAMES_PATH = "/games/";
    private static final Gson gson = new Gson();

    public GameResponse createGame(String playerId, boolean vsComputer) throws IOException {
        GameStartCommand command = new GameStartCommand();
        command.setPlayerId(playerId);
        command.setVsComputer(vsComputer);

        String response = post("/games/new", gson.toJson(command));
        return gson.fromJson(response, GameResponse.class);
    }

    public void joinGame(String gameId, String playerId) throws IOException {
        GameJoinCommand command = new GameJoinCommand();
        command.setPlayerId(playerId);

        post(GAMES_PATH + gameId + "/join", gson.toJson(command));
    }

    public void deployShips(String gameId, DeployShipsCommand command) throws IOException {
        post(GAMES_PATH + gameId + "/fields/ships/deploy", gson.toJson(command));
    }

    public GameFireResponse fire(String gameId, String playerId, String coordinate) throws IOException {
        GameFireCommand command = new GameFireCommand();
        command.setPlayerId(playerId);
        command.setCoordinate(coordinate);

        String response = post(GAMES_PATH + gameId + "/fields/fire", gson.toJson(command));
        return gson.fromJson(response, GameFireResponse.class);
    }

    @SuppressWarnings("java:S1874") // URL constructor is deprecated but sufficient for this simple CLI client
    private String post(String endpoint, String jsonBody) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();

        BufferedReader reader;
        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("HTTP error " + responseCode + ": " + response);
        }

        return response.toString();
    }
}
