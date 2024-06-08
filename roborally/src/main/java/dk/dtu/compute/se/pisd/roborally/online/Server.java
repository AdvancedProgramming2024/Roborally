package dk.dtu.compute.se.pisd.roborally.online;


import com.google.gson.JsonObject;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@RestController
public class Server {
    private final List<Lobby> lobbies = new ArrayList<>();

    @PostMapping(ResourceLocation.lobbies)
    public ResponseEntity<String> lobbyCreateRequest(@RequestBody String stringInfo) {
        JsonObject info = (JsonObject)jsonParser.parse(stringInfo);

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

        int playerId = lobby.addPlayer(info.get("playerName").getAsString());

        JsonObject response = new JsonObject();
        response.addProperty("lobbyId", lobbyId.toString());
        response.addProperty("playerId", playerId);

        return responseMaker.created(response.toString());
    }

    @PostMapping(ResourceLocation.joinLobby)
    public ResponseEntity<String> joinGameRequest(@RequestBody String stringInfo) {
        JsonObject info = (JsonObject) jsonParser.parse(stringInfo);

        String lobbyId = info.get("lobbyId").getAsString();
        String playerName = info.get("playerName").getAsString();

        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseMaker.notFound();
        }
        if (lobby.isInGame()) {
            return responseMaker.badRequest("Lobby is in a game");
        }

        int playerId = lobby.addPlayer(playerName);
        switch (playerId) {
            case -1:
                return responseMaker.badRequest("Lobby is full");
            case -2:
                return responseMaker.badRequest("Name already taken");
        }

        JsonObject response = new JsonObject();
        response.addProperty("lobbyId", lobbyId);
        response.addProperty("playerId", playerId);

        return responseMaker.itemResponse(response.toString());
    }

    @GetMapping(ResourceLocation.lobbyState)
    public void lobbyStateRequest(@RequestBody String stringInfo) {
        JsonObject info = (JsonObject) jsonParser.parse(stringInfo);

        String lobbyId = info.get("lobbyId").getAsString();
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseMaker.notFound();
        }

        JsonObject response = new JsonObject();
        response.addProperty("players", lobby.getPlayers().toString());

        return responseMaker.itemResponse(response.toString());
    }

    @PostMapping(ResourceLocation.games)
    public ResponseEntity<String> gameCreateRequest(@RequestBody String stringInfo) {
        JsonObject info = (JsonObject) jsonParser.parse(stringInfo);

        String lobbyId = info.get("lobbyId").getAsString();
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseMaker.notFound();
        }

        if (!lobby.startGame(info.get("mapName").getAsString())) {
            return responseMaker.badRequest("Not enough players");
        }

        return responseMaker.itemResponse();
    }

    @GetMapping(ResourceLocation.gameState)
    public ResponseEntity<String> gameStateRequest(@RequestBody String stringInfo) {
        JsonObject info = (JsonObject) jsonParser.parse(stringInfo);

        String lobbyId = info.get("lobbyId").getAsString();
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseMaker.notFound();
        }

        if (!lobby.isInGame()) {
            return responseMaker.badRequest("Game not started");
        }

        JsonObject response = new JsonObject();
        GameTemplate gameState = lobby.getGameServer().getGameState();
        if (gameState == null) {
            return responseMaker.badRequest("No new game state available");
        }
        response.addProperty("gameState", gameState.toString());
        return responseMaker.itemResponse(response.toString());
    }

    @PostMapping(ResourceLocation.playerReady)
    public void playerReadySignal(@RequestBody String stringInfo) {
        JsonObject info = (JsonObject) jsonParser.parse(stringInfo);

        String lobbyId = info.get("lobbyId").getAsString();
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);

        int playerId = info.get("playerId").getAsInt();

        assert lobby != null;
        lobby.getGameServer().getGameController().board.getPlayer(playerId).setReady(true);
    }
}
