package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.view.SpaceView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
        //First get the lasers path/LOS
        List<Space> LOS = new ArrayList<>();
        LOS = space.board.getLOS(space, heading, LOS);
        if (LOS == null) {
            return false;
        }

        gameController.server.addLaser(LOS, heading);

        //If list is length 1 check for player/wall
        if (LOS.size() == 1) {
            Space hit = LOS.get(0);
            if (hit.getPlayer() != null) {
                for (int i = 0; i < lazer; i++) {
                    hit.getPlayer().addCommandCard(new CommandCard(SPAM));

                }
                return true;
            } else if (hit.getWalls().contains(heading)) {
                return false;
            }
        }

        //Otherwise check the last space in the list
        Space hit = LOS.get(LOS.size() -1);
        Heading reverse = heading.next().next();

        if (hit.getWalls().contains(reverse)) {
            return false;
        } else if (hit.getPlayer() != null) {
            for (int i = 0; i < lazer; i++) {
                hit.getPlayer().addCommandCard(new CommandCard(SPAM));
            }
            return true;
        }
        return false;
    }
}
