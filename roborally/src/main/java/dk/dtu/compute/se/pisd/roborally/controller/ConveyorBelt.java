/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;

import static dk.dtu.compute.se.pisd.roborally.model.Heading.WEST;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class ConveyorBelt extends FieldAction {

    private Heading heading;
    private Heading turn;

    public Heading getHeading() {
        return heading;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }
    private int belt = 1;

    public int getBelt() {return belt;}

    public void setBelt(int belt) {this.belt = belt;}

    public Heading getTurn() {return turn;}

    public void setTurn(Heading turn) {this.turn = turn;}

    //If heading turn = west then turn left, all others turn right TODO: switch statement to better handle
    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        boolean turned = false;
        Player player = space.getPlayer();
        Heading optional = null;
        if (player == null) return false; // TODO: Remove if doAction is only called when a player is on a conveyor belt
        //if (player.isConveyorPush()) return false;

        //Check neighbour to see what the fuck to do
        Space neighbour = space.board.getNeighbour(space,heading);

        //This is a suprise tool that will help us later.
        for (FieldAction action : neighbour.getActions()) {
            if (action instanceof ConveyorBelt) {
               optional = ((ConveyorBelt) action).getHeading();
            }
        }


        //If neighbour is null or wall, move one forward. wall should block regardless.
        if (neighbour == null) {
            gameController.moveInDirection(player, heading, false);
            return false;
        }
        //Landing on a turningBelt, well, turns you
        for (int i = 0; i < belt; i++) {
            if (i == 1 && turned)  {
                gameController.moveInDirection(player, optional, false);
            } else {
                gameController.moveInDirection(player, heading, false);
                turned = turningBelt(player.getSpace(), heading);
            }
        }


        return false;
    }

    //Method to find a belt and turn player
    private boolean turningBelt(Space space, Heading heading1) {
        for (FieldAction action : space.getActions()) {
            if (action instanceof ConveyorBelt && ((ConveyorBelt) action).getHeading() != heading1) {
                if (((ConveyorBelt) action).getTurn() == WEST) {
                    space.getPlayer().setHeading(space.getPlayer().getHeading().prev());
                    return true;
                } else {
                    space.getPlayer().setHeading(space.getPlayer().getHeading().next());
                    return true;
                }
            }
        }
        return false;
    }

}
