package dk.dtu.compute.se.pisd.roborally.online;

import dk.dtu.compute.se.pisd.roborally.RoboRallyServer;

import java.util.ArrayList;

public class Lobby {
    private final String id;
    private boolean inGame = false;
    private RoboRallyServer gameServer = null;

    private ArrayList<String> players = new ArrayList<>();

    public Lobby(String id) {
        this.id = id;
    }

    public String getID() {
        return this.id;
    }

    public int addPlayer(String name) {
        if (players.size() >= 6) return -1;
        if (players.contains(name)) return -2;
        players.add(name);
        return players.size()-1;
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

    public boolean isInGame() {
        return inGame;
    }

    public boolean startGame(String mapName) {
        if (players.size() < 2) return false;
        inGame = true;

        // TODO: Make new thread with game
        gameServer = new RoboRallyServer(players, mapName);

        return true;
    }

    public RoboRallyServer getGameServer() {
        return gameServer;
    }
}
