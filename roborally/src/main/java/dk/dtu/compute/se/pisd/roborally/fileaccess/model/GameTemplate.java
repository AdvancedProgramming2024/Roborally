package dk.dtu.compute.se.pisd.roborally.fileaccess.model;

import java.util.ArrayList;
import java.util.List;

public class GameTemplate {
    public int gameId;

    public BoardTemplate board;
    public List<PlayerTemplate> players = new ArrayList<>();
    public int currentPlayer;
    public List<Integer> playerOrder = new ArrayList<>();
    public int playPhase;
    public int step;
    public int currentCommand;
}
