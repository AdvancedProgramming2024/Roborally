package dk.dtu.compute.se.pisd.roborally.Online;

import java.util.ArrayList;

public class Lobby {
    private final String id;

    private boolean inGame = false;

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

    public boolean startGame() {
        if (players.size() < 2) return false;
        inGame = true;
        // TODO: Make new thread with game
        return true;
    }
}
