package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

public class Checkpoint extends FieldAction{

    private final int id;

    public Checkpoint(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * @author Jonathan (s235115)
     * @param gameController of the current game
     * @param space on which the checkpoint is located
     * @return True if player got the checkpoint, false if not
     */
    @Override
    public boolean doAction(GameController gameController, Space space) {
        Player player = gameController.board.getCurrentPlayer();
        if(player.getSpace().equals(space) && player.getCheckpoints() == id - 1){
            player.reachCheckpoint();
            return true;
        }
        return false;
    }
}
