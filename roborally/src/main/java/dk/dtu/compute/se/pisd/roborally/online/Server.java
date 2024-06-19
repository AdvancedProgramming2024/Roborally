package dk.dtu.compute.se.pisd.roborally.online;


import com.google.gson.*;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.fileaccess.Adapter;
import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.PlayerTemplate;
import dk.dtu.compute.se.pisd.roborally.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static dk.dtu.compute.se.pisd.roborally.online.ResponseCenter.asJson;


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
        response.add("lobbies", ids);
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
            return responseCenter.badRequest(asJson("Only player 1 can start the game"));
        }

        if (!lobby.startGame(mapName)) {
            return responseCenter.badRequest(asJson("There should be 2-6 players to start the game"));
        }

        JsonObject response = new JsonObject();

        try {
            while (lobby.getGameServer() == null) Thread.sleep(100);
            while (lobby.getGameServer().getGameState() == null) Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        response.addProperty("gameState", gson.toJson(lobby.getGameServer().getGameState(playerName)));

        lobby.getGameServer().getLaser().clear();
        return responseCenter.response(response.toString());
    }

    @GetMapping(ResourceLocation.gameState+"/{playerName}")
    public ResponseEntity<String> gameStateRequest(@PathVariable String lobbyId, @PathVariable String playerName) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseCenter.notFound();
        }

        if (!lobby.isInGame() || lobby.getGameServer() == null || lobby.getGameServer().getGameState() == null) {
            return responseCenter.badRequest(asJson("Game has not started yet"));
        }

        JsonObject response = new JsonObject();
        GameTemplate gameState = lobby.getGameServer().getGameState(playerName);

        if (gameState == null) {
            return responseCenter.badRequest(asJson("No new game state available"));
        }

        response.addProperty("gameState", gson.toJson(gameState));

        JsonArray lasers = new JsonArray();
        Iterator<Map.Entry<List<Space>, Heading>> entrySets = lobby.getGameServer().getLaser().entrySet().iterator();
        for (int i = 0; i < lobby.getGameServer().getLaser().entrySet().size(); i++) {
            Map.Entry<List<Space>, Heading> entry = entrySets.next();
            JsonObject laser = new JsonObject();
            JsonArray LOS = new JsonArray();
            for (Space space : entry.getKey()) {
                JsonObject spaceObject = new JsonObject();
                spaceObject.addProperty("x", space.x);
                spaceObject.addProperty("y", space.y);
                LOS.add(spaceObject);
            }
            laser.add("LOS", LOS);
            laser.addProperty("heading", entry.getValue().ordinal());

            lasers.add(laser);
        }
        response.add("lasers", lasers);
        return responseCenter.response(response.toString());
    }

    @PostMapping(ResourceLocation.playerCardMovement)
    public ResponseEntity<String> playerCardMovement(@PathVariable String lobbyId, @PathVariable int playerId, @RequestBody String stringInfo) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);

        assert lobby != null;
        if (lobby.getGameServer().getGameController().board.getPhase() != Phase.PROGRAMMING) {
            return responseCenter.badRequest(asJson("Player can only move cards during the programming phase"));
        }

        JsonObject info = (JsonObject)jsonParser.parse(stringInfo);
        int sourceIndex = info.get("sourceIndex").getAsInt();
        int targetIndex = info.get("targetIndex").getAsInt();
        boolean sourceIsProgram = info.get("sourceIsProgram").getAsBoolean();
        boolean targetIsProgram = info.get("targetIsProgram").getAsBoolean();
        String playerName = info.get("playerName").getAsString();

        GameController gameController = lobby.getGameServer().getGameController();
        Player player = gameController.board.getPlayer(playerId);
        CommandCardField source = sourceIsProgram ? player.getProgramField(sourceIndex) : player.getCardField(sourceIndex);
        CommandCardField target = targetIsProgram ? player.getProgramField(targetIndex) : player.getCardField(targetIndex);
        if (lobby.getGameServer().getGameController().moveCards(source, target)) {
            JsonObject response = new JsonObject();
            GameTemplate gameState = lobby.getGameServer().getGameState(playerName);
            if (gameState == null) {
                return responseCenter.badRequest(asJson("No new game state available"));
            }
            response.addProperty("gameState", gson.toJson(gameState));
            return responseCenter.response(response.toString());
        } else {
            return responseCenter.badRequest(asJson("Invalid card movement"));
        }
    }

    @GetMapping(ResourceLocation.playerReady)
    public ResponseEntity<String> playerReadySignal(@PathVariable String lobbyId, @PathVariable int playerId) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        assert lobby != null;
        Board board = lobby.getGameServer().getGameController().board;
        Player player = board.getPlayer(playerId);
        if (player == null) return responseCenter.badRequest("Player not found");
        if (board.getPhase() == Phase.PROGRAMMING) {
            for (int i = 0; i < Player.NO_REGISTERS; i++) {
                if (player.getProgramField(i).getCard() == null) return responseCenter.badRequest("Player needs to fill all registers");
            }
        }
        player.setReady(true);
        return responseCenter.ok();
    }

    @GetMapping(ResourceLocation.gameSave)
    public ResponseEntity<String> saveGameRequest(@PathVariable String lobbyId) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseCenter.notFound();
        }

        if (!lobby.isInGame() || lobby.getGameServer() == null || lobby.getGameServer().getGameState() == null) {
            return responseCenter.badRequest(asJson("Game has not started yet, impossible to save"));
        }

        JsonObject response = new JsonObject();
        GameTemplate gameState = LoadSave.saveGameState(lobby.getGameServer().getGameController(), true);

        response.addProperty("gameState", gson.toJson(gameState));
        return responseCenter.response(response.toString());
    }

    @PostMapping(ResourceLocation.gameLoad)
    public ResponseEntity<String> gameLoadRequest(@PathVariable String lobbyId, @RequestBody String stringInfo) {
        JsonObject info = (JsonObject)jsonParser.parse(stringInfo);
        String playerName = info.get("playerName").getAsString();
        GameTemplate gameState = gson.fromJson(info.get("gameState").getAsString(), GameTemplate.class);
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        if (lobby == null) {
            return responseCenter.notFound();
        }

        if (!playerName.equals(lobby.getPlayers().get(0))) {
            return responseCenter.badRequest(asJson("Only player 1 can start the game"));
        }

        if (!lobby.loadGame(gameState)) {
            StringBuilder response = new StringBuilder();
            response.append("These players must be present to load the game (the names need to be the same):\n");
            for (PlayerTemplate player : gameState.players) {
                response.append("'").append(player.name).append("'").append(", ");
                response.delete(response.length() - 2, response.length() - 1);
            }
            return responseCenter.badRequest(asJson(response.toString()));
        }

        JsonObject response = new JsonObject();

        try {
            while (lobby.getGameServer() == null) Thread.sleep(100);
            while (lobby.getGameServer().getGameState() == null) Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        response.addProperty("gameState", gson.toJson(lobby.getGameServer().getGameState(playerName)));

        lobby.getGameServer().getLaser().clear();
        return responseCenter.response(response.toString());
    }
    
    @PostMapping(ResourceLocation.playerChoice)
    public ResponseEntity<String> playerChoice(@PathVariable String lobbyId, @PathVariable int playerId, @RequestBody String stringInfo) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        JsonObject info = (JsonObject)jsonParser.parse(stringInfo);
        int command = info.get("command").getAsInt();
        assert lobby != null;
        if (lobby.getGameServer().getGameController().makeChoice(Command.values()[command])) {
            return responseCenter.ok();
        } else {
            return responseCenter.badRequest("Invalid choice");
        }
    }

    @PostMapping(ResourceLocation.buyUpgrade)
    public ResponseEntity<String> buyUpgrade(@PathVariable String lobbyId, @PathVariable int playerId, @RequestBody String stringInfo) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        JsonObject info = (JsonObject)jsonParser.parse(stringInfo);
        int shopIndex = info.get("shopIndex").getAsInt();
        assert lobby != null;
        GameController gameController = lobby.getGameServer().getGameController();
        if (gameController.board.getCurrentPlayer().getId() != playerId) {
            return responseCenter.badRequest(asJson("Not your turn"));
        }
        if (gameController.board.getCurrentPlayer().hasUsedUpgradePhase()) {
            return responseCenter.badRequest(asJson("You have already used the upgrade phase"));
        }

        if (shopIndex == -1) {
            gameController.continueUpgradePhase();
            return responseCenter.ok();
        }
        if (gameController.buyUpgrade(gameController.getUpgradeShop()[shopIndex])) {
            gameController.continueUpgradePhase();
            return responseCenter.ok();
        } else {
            return responseCenter.badRequest(asJson("You can't buy this upgrade, you both need the energy cubes stated on the card and space in your player mat"));
        }
    }

    @PostMapping(ResourceLocation.discardUpgrade)
    public ResponseEntity<String> discardUpgrade(@PathVariable String lobbyId, @PathVariable int playerId, @RequestBody String stringInfo) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        JsonObject info = (JsonObject)jsonParser.parse(stringInfo);
        int index = info.get("index").getAsInt();
        boolean permanent = info.get("isPermanent").getAsBoolean();
        assert lobby != null;
        GameController gameController = lobby.getGameServer().getGameController();

        Player player = gameController.board.getPlayer(playerId);
        player.discardUpgradeCard(index, permanent);
        gameController.server.updateGameState();
        return responseCenter.ok();
    }

    @PostMapping(ResourceLocation.toggleUpgrade)
    public ResponseEntity<String> toggleUpgrade(@PathVariable String lobbyId, @PathVariable int playerId, @RequestBody String stringInfo) {
        Lobby lobby = lobbies.stream().filter(l -> l.getID().contentEquals(lobbyId)).findFirst().orElse(null);
        JsonObject info = (JsonObject)jsonParser.parse(stringInfo);
        int index = info.get("index").getAsInt();
        boolean permanent = info.get("isPermanent").getAsBoolean();
        assert lobby != null;
        GameController gameController = lobby.getGameServer().getGameController();

        Player player = gameController.board.getPlayer(playerId);
        player.toggleUpgradeCard(index, permanent);
        gameController.server.updateGameState();
        return responseCenter.ok();
    }
}
