package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;

public class EnergyCubeField {

    //picks up an energy cube and adds it to the player's energy bank
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player player = space.getPlayer();
        if (player == null) return false; // TODO: Remove if doAction is only called when a player is on a conveyor belt

        player.addEnergyCubes(1);
        return false;
    }
}
