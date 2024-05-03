package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import dk.dtu.compute.se.pisd.roborally.view.BoardView;
import org.jetbrains.annotations.NotNull;

public class EnergyCubeField extends FieldAction {
    private int energyCubes = 1;

    public int getEnergyCubes() {
        return energyCubes;
    }

    //picks up an energy cube and adds it to the player's energy bank
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player player = space.getPlayer();
        if (player == null) return false;

        if (energyCubes != 1) {
            return false;
        } else {
            player.addEnergyCubes(1);
            if (gameController.board.getStep() % 4 != 0 || gameController.board.getStep() == 0) {
                energyCubes--;
                // Destroy the energy cube image
                BoardView.getSpaceView(space).eneryCubeImageView.setVisible(false);
            }
            return true;
        }
    }
}
