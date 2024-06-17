package dk.dtu.compute.se.pisd.roborally.online;

import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * This class is used to store the location of the different resources.
 * @auther Daniel Jensen
 */
@Component
public class ResourceLocation {
    public static URI makeUri(String path) {
        return URI.create(baseLocation + path);
    }

    public static String lobbyPath(String lobbyId) {
        return lobby.replace("{lobbyId}", lobbyId);
    }
    public static String joinLobbyPath(String lobbyId) {
        return joinLobby.replace("{lobbyId}", lobbyId);
    }
    public static String leaveLobbyPath(String lobbyId) {
        return leaveLobby.replace("{lobbyId}", lobbyId);
    }
    public static String lobbyStatePath(String lobbyId) {
        return lobbyState.replace("{lobbyId}", lobbyId);
    }
    public static String gamePath(String lobbyId) {
        return game.replace("{lobbyId}", lobbyId);
    }
    public static String gameStatePath(String lobbyId) {
        return gameState.replace("{lobbyId}", lobbyId);
    }
    public static String gameSavePath(String lobbyId) {
        return gameSave.replace("{lobbyId}", lobbyId);
    }
    public static String gameLoadPath(String lobbyId) {
        return gameLoad.replace("{lobbyId}", lobbyId);
    }
    public static String playerCardMovementPath(String lobbyId, int playerId) {
        return playerCardMovement.replace("{lobbyId}", lobbyId).replace("{playerId}", playerId + "");
    }
    public static String playerReadyPath(String lobbyId, int playerId) {
        return playerReady.replace("{lobbyId}", lobbyId).replace("{playerId}", playerId + "");
    }
    public static String playerChoicePath(String lobbyId, int playerId) {
        return playerChoice.replace("{lobbyId}", lobbyId).replace("{playerId}", playerId + "");
    }

    public static final String baseLocation = "http://localhost:8080";
    public static final String lobbies = "/lobbies";
    public static final String lobby = lobbies + "/{lobbyId}";
    public static final String game = lobby + "/game";
    public static final String joinLobby = lobby + "/join";
    public static final String leaveLobby = lobby + "/leave";
    public static final String lobbyState = lobby + "/state";
    public static final String gameState = game + "/state";
    public static final String gameLoad = game + "/Load";
    public static final String gameSave = game + "/saveFile";
    public static final String players = game + "/players";
    public static final String player = players + "/{playerId}";
    public static final String playerCardMovement = player + "/cardMovement";
    public static final String playerReady = player + "/ready";
    public static final String playerChoice = player + "/choice";
}
