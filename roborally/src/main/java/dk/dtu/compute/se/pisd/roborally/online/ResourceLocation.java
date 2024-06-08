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
    public static String lobbyStatePath(String lobbyId) {
        return lobbyState.replace("{lobbyId}", lobbyId);
    }

    public static final String baseLocation = "http://localhost:8080";
    public static final String lobbies = "/lobbies";
    public static final String lobby = lobbies + "/{lobbyId}";
    public static final String game = lobby + "/game";
    public static final String joinLobby = lobby + "/join";
    public static final String lobbyState = lobby + "/state";
    public static final String gameState = game + "/state";
    public static final String players = game + "/players";
    public static final String player = players + "/{playerId}";
    public static final String playerReady = player + "/ready";
}
