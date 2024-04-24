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
package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dk.dtu.compute.se.pisd.roborally.model.Heading.SOUTH;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class Player extends Subject {

    final public static int NO_REGISTERS = 5;
    final public static int NO_CARDS = 8;

    final public Board board;

    private String name;
    private String color;

    private Space space;
    private Heading heading = SOUTH;

    private List<CommandCard> drawPile;
    private List<CommandCard> discardPile;
      
    private int checkpoints = 0;
    public int eneregyBank = 0;
    private CommandCardField[] program;
    private CommandCardField[] cards;

    boolean rebooting = false;

    public Player(@NotNull Board board, String color, @NotNull String name) {
        this.board = board;
        this.name = name;
        this.color = color;

        this.space = null;


        Command[] commands = Command.values();
        drawPile = new ArrayList<CommandCard>();
        discardPile = new ArrayList<CommandCard>();
        int[] commandValues = {0, 0, 0, 0, 1, 1, 1, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 6, 7, 8, 13, 13, 13, 13, 13, 9, 9, 9, 9, 9};
        for (int commandValue : commandValues) {
            drawPile.add(new CommandCard(commands[commandValue]));
        }

        shuffleDrawPile();

        program = new CommandCardField[NO_REGISTERS];
        for (int i = 0; i < program.length; i++) {
            program[i] = new CommandCardField(this);
        }

        cards = new CommandCardField[NO_CARDS];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = new CommandCardField(this);
        }
    }

    public void shuffleDrawPile() {
        for (int i = 0; i < drawPile.size(); i++) {
            int r = (int) (Math.random() * drawPile.size());
            CommandCard tmp = drawPile.get(i);
            drawPile.set(i, drawPile.get(r));
            drawPile.set(r, tmp);
        }
    }

    public CommandCard drawCommandCard() {
        if (drawPile.isEmpty()) {
            drawPile = discardPile;
            discardPile = new ArrayList<CommandCard>();
            shuffleDrawPile();
        }
        CommandCard card = drawPile.get(0);
        drawPile.remove(0);
        return card;
    }

    public void addCommandCard(CommandCard card) {
        discardPile.add(card);
    }

    public void discardCommandCard(CommandCard card) {
        discardPile.add(card);
    }
      
    public int getCheckpoints() {
        return checkpoints;
    }

    public void reachCheckpoint() {
        checkpoints++;
        notifyChange();
    }

    //energy cube functions
    public int getEnergyCubes() {
        return eneregyBank;
    }

    public void setEnergyCubes(int energyCubes) {
        this.eneregyBank = energyCubes;
    }

    public void addEnergyCubes(int energyCubes) {
        this.eneregyBank += energyCubes;
    }

    public void removeEnergyCubes(int energyCubes) {
        this.eneregyBank -= energyCubes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.equals(this.name)) {
            this.name = name;
            notifyChange();
            if (space != null) {
                space.playerChanged();
            }
        }
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        notifyChange();
        if (space != null) {
            space.playerChanged();
        }
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        Space oldSpace = this.space;
        if (space == null || space.board == this.board) {
            this.space = space;
            if (oldSpace != null) {
                oldSpace.setPlayer(null);
            }
            if (space != null) {
                space.setPlayer(this);
            }
            notifyChange();
        }
    }

    public Heading getHeading() {
        return heading;
    }

    public void setHeading(@NotNull Heading heading) {
        if (heading != this.heading) {
            this.heading = heading;
            notifyChange();
            if (space != null) {
                space.playerChanged();
            }
        }
    }

    public void reboot(GameController gameController) {
        rebooting = true;
        addCommandCard(new CommandCard(Command.SPAM));
        addCommandCard(new CommandCard(Command.SPAM));


        if (!gameController.moveToSpace(this, board.getRebootStation(), board.getRebootStationHeading())) {
            // TODO: What to do if the reboot station is blocked? Move to start space?
        }
        // TODO: Player should choose heading
    }

    public boolean isRebooting() {
        return rebooting;
    }

    public void stopRebooting() {
        rebooting = false;
    }

    public CommandCardField getProgramField(int i) {
        return program[i];
    }

    public CommandCardField getCardField(int i) {
        return cards[i];
    }

}
