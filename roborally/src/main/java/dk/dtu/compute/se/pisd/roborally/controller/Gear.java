package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;

public class Gear extends FieldAction {
    private Heading heading;

    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player player = space.getPlayer();
        if (player == null) return false;

        //If heading of gear is west, player turns counterclockwise
        if (heading == Heading.WEST) {
            player.setHeading(player.getHeading().prev());
        } else {                                            //If any other heading, turn clockwise
            player.setHeading(player.getHeading().next());
        }

        return true;
    }
}

