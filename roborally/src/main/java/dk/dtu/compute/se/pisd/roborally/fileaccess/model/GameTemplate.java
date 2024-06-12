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

    @Override
    public GameTemplate clone() {
        try {
            return (GameTemplate) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
