package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.*;
import org.jetbrains.annotations.NotNull;

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
        List<Space> LOS = space.board.getLOS(space, heading);
        if (LOS == null) {
            return false;
        }
        System.out.println(LOS.size());

        //If list is length 1 check for player/wall
        if (LOS.size() == 1) {
            Space hit = LOS.get(0);
            if (hit.getPlayer() != null) {
                for (int i = 0; i < lazer; i++) {
                    hit.getPlayer().addCommandCard(new CommandCard(SPAM));

                }
                space.board.resetLOS();
                return true;
            } else if (hit.getWalls().contains(heading)) {
                space.board.resetLOS();
                return false;
            }
        }

        //Otherwise check the last space in the list
        Space hit = LOS.get(LOS.size() -1);
        Heading reverse = heading.next().next();

        if (hit.getWalls().contains(reverse)) {
            space.board.resetLOS();
            return false;
        } else if (hit.getPlayer() != null) {
            for (int i = 0; i < lazer; i++) {
                hit.getPlayer().addCommandCard(new CommandCard(SPAM));
            }
            space.board.resetLOS();
            return true;
        }
        space.board.resetLOS();
        return false;
    }
}
