package dk.dtu.compute.se.pisd.roborally.online;

import org.springframework.stereotype.Component;

/**
 * This class is used to store the location of the different resources.
 * @auther Daniel Jensen
 */
@Component
public abstract class ResourceLocation {
    public static final String baseLocation = "http://localhost:8080";
    public static final String games = "/games";
    public static final String lobbies = "/lobbies";
    public static final String joinLobby = lobbies + "/join";
    public static final String lobbyState = lobbies + "/state";
    public static final String gameState = games + "/state";
    public static final String players = games + "/players";
    public static final String playerReady = players + "/ready";
}
