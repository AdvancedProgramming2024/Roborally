package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;

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
        if (player == null) {return false;}

        Space hit = space.board.getLOS(space, heading);
        if (hit.getPlayer() != null) {
            for (int i = 0; i < lazer; i++) {
                //hit.getPLayer().doloadsofdamage() , TODO: Implement damage
                System.out.println("HEADSHOT!");
                System.out.println("Player " + hit.getPlayer().getName() + "  got hit with a lazer");
            }


            return true;
        }
        return false;
    }
}
