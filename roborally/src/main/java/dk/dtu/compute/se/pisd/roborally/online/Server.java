package dk.dtu.compute.se.pisd.roborally.Online;


import com.google.gson.*;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.fileaccess.Adapter;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@RestController
public class Server {
    private final List<Lobby> lobbies = new ArrayList<>();
    private static final JsonParser jsonParser = new JsonParser();
    private final static ResponseCenter<String> responseCenter = new ResponseCenter<>();

    @PostMapping(ResourceLocation.lobbies)
    public ResponseEntity<String> lobbyCreateRequest(@RequestBody String playerName) {
        Random rand = new Random();
        StringBuilder lobbyId = new StringBuilder();
        boolean lobbyIdExists = false;

        do {
            lobbyId.delete(0, lobbyId.length());
            for (int i = 0; i < 4; i++) {
                lobbyId.append(rand.nextInt(10));
            }
            lobbyIdExists = lobbies.stream().anyMatch(lobby -> lobby.getID().contentEquals(lobbyId));
        } while(lobbyIdExists);

        Lobby lobby = new Lobby(lobbyId.toString());
        lobbies.add(lobby);

        lobby.addPlayer(playerName);

        return responseCenter.response(lobbyId.toString());
    }

    @GetMapping(ResourceLocation.lobbies)
    public ResponseEntity<String> getLobbies() {
        JsonObject response = new JsonObject();
        JsonArray ids = new JsonArray();
        for (Lobby lobby : lobbies) {
            ids.add(lobby.getID());
        }

        return responseCenter.response(response.toString());
    }
    @GetMapping(ResourceLocation.lobby)
    public ResponseEntity<String> getLobby(@PathVariable String lobbyId) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseCenter.badRequest("Lobby does not exist");
        }
        if (lobby.isInGame()) {
            return responseCenter.badRequest("Lobby is in a game");
        }

        return responseCenter.ok();
    }

    @PostMapping(ResourceLocation.joinLobby)
    public ResponseEntity<String> joinGameRequest(@PathVariable String lobbyId, @RequestBody String playerName) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseCenter.notFound();
        }
        if (lobby.isInGame()) {
            return responseCenter.badRequest("Lobby is in a game");
        }

        int playerId = lobby.addPlayer(playerName);
        switch (playerId) {
            case -1:
                return responseCenter.badRequest("Lobby is full");
            case -2:
                return responseCenter.badRequest("Name already taken");
        }

        JsonObject response = new JsonObject();
        response.addProperty("lobbyId", lobbyId);
        response.addProperty("playerId", playerId);

        return responseCenter.response(response.toString());
    }

    @GetMapping(ResourceLocation.lobbyState)
    public ResponseEntity<String> lobbyStateRequest(@PathVariable String lobbyId) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseCenter.notFound();
        }

        JsonObject response = new JsonObject();
        JsonArray players = new JsonArray();
        for (String player : lobby.getPlayers()) {
            players.add(player);
        }
        response.add("players", players);

        return responseCenter.response(response.toString());
    }

    @PostMapping(ResourceLocation.game)
    public ResponseEntity<String> gameCreateRequest(@PathVariable String lobbyId, @RequestBody String mapName) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseCenter.notFound();
        }

        if (!lobby.startGame(mapName)) {
            return responseCenter.badRequest("Not enough players");
        }


        GsonBuilder simpleBuilder = new GsonBuilder().
                registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>()).
                setPrettyPrinting();
        Gson gson = simpleBuilder.create();

        JsonObject response = new JsonObject();
        response.addProperty("gameState", gson.toJson(lobby.getGameServer().getGameState()));
        return responseCenter.response(response.toString());
    }

    @GetMapping(ResourceLocation.gameState)
    public ResponseEntity<String> gameStateRequest(@PathVariable String lobbyId) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseCenter.notFound();
        }

        if (!lobby.isInGame()) {
            return responseCenter.badRequest("Game not started");
        }

        JsonObject response = new JsonObject();
        GameTemplate gameState = lobby.getGameServer().getGameState();
        if (gameState == null) {
            return responseCenter.badRequest("No new game state available");
        }
        response.addProperty("gameState", gameState.toString());
        return responseCenter.response(response.toString());
    }

    @PostMapping(ResourceLocation.playerReady)
    public void playerReadySignal(@PathVariable String lobbyId, @PathVariable int playerId) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);

        assert lobby != null;
        lobby.getGameServer().getGameController().board.getPlayer(playerId).setReady(true);
    }
}
