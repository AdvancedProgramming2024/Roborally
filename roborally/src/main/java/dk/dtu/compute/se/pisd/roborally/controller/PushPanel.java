package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;

public class PushPanel extends FieldAction {
    private Heading heading;

    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player player = space.board.getNeighbour(space, heading).getPlayer();
        if (player == null) return false;

        gameController.moveInDirection(player, heading);
        return true;
    }
}
