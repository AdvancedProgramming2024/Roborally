package dk.dtu.compute.se.pisd.roborally.online;


import com.google.gson.*;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.fileaccess.Adapter;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.SpaceTemplate;
import dk.dtu.compute.se.pisd.roborally.model.CommandCardField;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Phase;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


@RestController
public class Server {
    private final List<Lobby> lobbies = new ArrayList<>();
    private static final JsonParser jsonParser = new JsonParser();
    private final static ResponseCenter<String> responseCenter = new ResponseCenter<>();

    private final Gson gson;

    public Server() {
        GsonBuilder simpleBuilder = new GsonBuilder().
                registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>()).
                setPrettyPrinting();
        gson = simpleBuilder.create();
    }

    @PostMapping(ResourceLocation.lobbies)
    public ResponseEntity<String> lobbyCreateRequest(@RequestBody String stringInfo) {
        JsonObject info = (JsonObject)jsonParser.parse(stringInfo);
        String playerName = info.get("playerName").getAsString();
        Random rand = new Random();
        StringBuilder lobbyId = new StringBuilder();
        boolean lobbyIdExists;

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

        return responseCenter.response(lobby.getID());
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
    public ResponseEntity<String> joinGameRequest(@PathVariable String lobbyId, @RequestBody String stringInfo) {
        JsonObject info = (JsonObject)jsonParser.parse(stringInfo);
        String playerName = info.get("playerName").getAsString();
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseCenter.notFound();
        }
        if (lobby.isInGame()) {
            return responseCenter.badRequest("Lobby is in a game");
        }

        return switch (lobby.addPlayer(playerName)) {
            case -1 -> responseCenter.badRequest("Lobby is full");
            case -2 -> responseCenter.badRequest("Name already taken");
            default -> responseCenter.ok();
        };
    }

    @PostMapping(ResourceLocation.leaveLobby)
    public ResponseEntity<String> leaveGameRequest(@PathVariable String lobbyId, @RequestBody String stringInfo) {
        JsonObject info = (JsonObject)jsonParser.parse(stringInfo);
        String playerName = info.get("playerName").getAsString();
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseCenter.notFound();
        }

        lobby.removePlayer(playerName);
        if (lobby.getPlayers().isEmpty()) {
            lobbies.remove(lobby);
        }

        return responseCenter.ok();
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
    public ResponseEntity<String> gameCreateRequest(@PathVariable String lobbyId, @RequestBody String stringInfo) {
        JsonObject info = (JsonObject)jsonParser.parse(stringInfo);
        String playerName = info.get("playerName").getAsString();
        String mapName = info.get("mapName").getAsString();
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseCenter.notFound();
        }

        if (!playerName.equals(lobby.getPlayers().get(0))) {
            return responseCenter.badRequest("Only player 1 can start the game");
        }

        if (!lobby.startGame(mapName)) {
            return responseCenter.badRequest("There should be 2-6 players to start the game");
        }

        JsonObject response = new JsonObject();

        try {
            while (lobby.getGameServer() == null) Thread.sleep(100);
            while (lobby.getGameServer().getGameState() == null) Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        response.addProperty("gameState", gson.toJson(lobby.getGameServer().getGameState()));

        lobby.getGameServer().getLaser().clear();
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
        response.addProperty("gameState", gson.toJson(lobby.getGameServer().getGameState()));

        JsonArray lasers = new JsonArray();
        for (Map.Entry<List<SpaceTemplate>, Heading> entry : lobby.getGameServer().getLaser().entrySet()) {
            JsonObject laser = new JsonObject();
            laser.addProperty("laser", gson.toJson(entry.getKey()));
            laser.addProperty("heading", entry.getValue().ordinal());
            lasers.add(laser);
        }
        response.addProperty("lasers", lasers.toString());
        return responseCenter.response(response.toString());
    }

    @PostMapping(ResourceLocation.playerCardMovement)
    public ResponseEntity<String> playerProgram(@PathVariable String lobbyId, @PathVariable int playerId, @RequestBody String stringInfo) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);

        assert lobby != null;
        if (lobby.getGameServer().getGameController().board.getPhase() != Phase.PROGRAMMING) {
            return responseCenter.badRequest("Player can only move cards during the programming phase");
        }

        JsonObject info = (JsonObject)jsonParser.parse(stringInfo);
        int sourceIndex = info.get("sourceIndex").getAsInt();
        int targetIndex = info.get("targetIndex").getAsInt();
        boolean sourceIsProgram = info.get("sourceIsProgram").getAsBoolean();
        boolean targetIsProgram = info.get("targetIsProgram").getAsBoolean();

        GameController gameController = lobby.getGameServer().getGameController();
        Player player = gameController.board.getPlayer(playerId);
        CommandCardField source = sourceIsProgram ? player.getProgramField(sourceIndex) : player.getCardField(sourceIndex);
        CommandCardField target = targetIsProgram ? player.getProgramField(targetIndex) : player.getCardField(targetIndex);
        if (lobby.getGameServer().getGameController().moveCards(source, target)) {
            JsonObject response = new JsonObject();
            GameTemplate gameState = lobby.getGameServer().getGameState();
            if (gameState == null) {
                return responseCenter.badRequest("No new game state available");
            }
            response.addProperty("gameState", gson.toJson(gameState));
            return responseCenter.response(response.toString());
        } else {
            return responseCenter.badRequest("Invalid card movement");
        }
    }

    @PostMapping(ResourceLocation.playerReady)
    public ResponseEntity<String> playerReadySignal(@PathVariable String lobbyId, @PathVariable int playerId) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        assert lobby != null;
        if (lobby.getGameServer().getGameController().board.getPhase() == Phase.PROGRAMMING) {
            return responseCenter.badRequest("Player needs to send their program");
        }
        lobby.getGameServer().getGameController().board.getPlayer(playerId).setReady(true);
        return responseCenter.ok();
    }
}
