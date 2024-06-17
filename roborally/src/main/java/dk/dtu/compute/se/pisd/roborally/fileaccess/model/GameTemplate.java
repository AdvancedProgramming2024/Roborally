package dk.dtu.compute.se.pisd.roborally.fileaccess.model;

import java.util.ArrayList;
import java.util.List;

public class GameTemplate implements Cloneable {
    public int gameId;

    public BoardTemplate board;
    public List<PlayerTemplate> players = new ArrayList<>();
    public int currentPlayer;
    public List<Integer> playerOrder = new ArrayList<>();
    public int playPhase;
    public int step;
    public int currentCommand;
    public String winnerName;

    @Override
    public GameTemplate clone() {
        try {
            GameTemplate gameTemplate = (GameTemplate) super.clone();
            List<PlayerTemplate> players = new ArrayList<>();
            for (PlayerTemplate player : this.players) {
                players.add(player.clone());
            }
            gameTemplate.players = players;
            return gameTemplate;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
