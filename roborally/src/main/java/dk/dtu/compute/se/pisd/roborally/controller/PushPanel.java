package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;

public class PushPanel extends FieldAction {
    private Heading heading;
    private PushTime pushTime;
    public enum PushTime {
        EVEN, ODD
    }

    public void setHeading(Heading heading) {this.heading = heading;}

    public Heading getHeading() {return heading;}

    public PushTime getPushTime() {return pushTime;}

    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player player = space.getPlayer();
        if (player == null) return false;
        if (pushTime == PushTime.EVEN && (space.board.getStep() + 1) % 2 == 0) {  // +1 because step is 0-indexed
            gameController.moveInDirection(player, heading);
        } else if (pushTime == PushTime.ODD && (space.board.getStep() + 1) % 2 == 1) {  // +1 because step is 0-indexed
            gameController.moveInDirection(player, heading);
        }
        return true;
    }
}
