package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.*;
import org.jetbrains.annotations.NotNull;

import static dk.dtu.compute.se.pisd.roborally.model.Command.SPAM;

public class Laser extends FieldAction {
    private Heading heading;

    public void setHeading(Heading heading) {this.heading = heading;}

    public Heading getHeading() {return heading;}

    private int lazer;

    public int getLazer() {return lazer;}

    public void setLazer(int lazer) {this.lazer = lazer;}

    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player player = space.getPlayer();
        if (player != null) {
            Command[] commands = Command.values();
            player.addCommandCard(new CommandCard(commands[9]));
            return true;
        }

        Space hit = space.board.getLOS(space, heading);
        //If we reach out of bounds do nothing
        if (hit == null) {
            return false;
        }
        if (hit.getPlayer() != null) {
            for (int i = 0; i < lazer; i++) {
                hit.getPlayer().addCommandCard(new CommandCard(Command.SPAM));
            }
            return true;
        }
        return false;
    }
}
