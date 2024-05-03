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

import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.view.BoardView;
import dk.dtu.compute.se.pisd.roborally.view.SpaceView;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class GameController {

    final public Board board;
    final public CommandCardController commandCardController;
    private List<Player> playerOrder;

    public GameController(Board board) {
        this.board = board;
        commandCardController = new CommandCardController();
    }

    public void moveForward(@NotNull Player player) {
        moveInDirection(player, player.getHeading(), true);
    }

    public void moveInDirection(@NotNull Player player, @NotNull Heading heading, boolean push) {
        if (player.board == board) {
            Space space = player.getSpace();

            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                if (target.getPlayer() != null && !push) {
                    return;
                }
                try {
                    move(player, target, heading);
                } catch (ImpossibleMoveException e) {
                    // we don't do anything here  for now; we just catch the
                    // exception so that we do not pass it on to the caller
                    // (which would be very bad style).
                }
            } else {
                player.reboot(this);
                System.out.println("Player fell off the board and reboots...");
            }
        }
    }

    public void turn(@NotNull Player player, int timesClockwise) {
        Heading playerHeading = player.getHeading();
        for (int i = 0; i < timesClockwise; i++) {
            playerHeading = playerHeading.next();
        }
        player.setHeading(playerHeading);
    }

    public boolean moveToSpace(@NotNull Player player, @NotNull Space space, @NotNull Heading heading) {
        Player other = space.getPlayer();
        if (other != null && other != player) {
            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                if (target == space) return false;
                try {
                    move(other, target, heading);
                } catch (ImpossibleMoveException e) {
                    return false;
                }
                assert space.getPlayer() == null : target; // make sure target is free now
            } else {
                other.reboot(this);
                System.out.println("Player fell off the board and reboots...");
            }
        }
        player.setSpace(space);
        return true;
    }

    private void move(@NotNull Player player, @NotNull Space space, @NotNull Heading heading) throws ImpossibleMoveException {
        assert board.getNeighbour(player.getSpace(), heading) == space; // make sure the move to here is possible in principle
        Player other = space.getPlayer();
        if (other != null && other != player) {
            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                if (target == space) throw new ImpossibleMoveException(player, space, heading);

                move(other, target, heading);

                assert target.getPlayer() == null : target; // make sure target is free now
            } else {
                other.reboot(this);
                System.out.println("Player fell off the board and reboots...");
            }
        }
        player.setSpace(space);
    }

    public void moveCurrentPlayerToSpace(Space space) {
        // TODO: Import or Implement this method. This method is only for debugging purposes. Not useful for the game.
        if (space.getPlayer() != null) {
            return;
        }
        board.getCurrentPlayer().setSpace(space);
    }

    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    /**
     * @author Jonathan (s235115)
     */
    public void finishProgrammingPhase() {
        for (int j = 0; j < board.getPlayersNumber(); j++) {
            Player player = board.getPlayer(j);
            for (int i = 0; i < Player.NO_REGISTERS; i++) {
                if (player.getProgramField(i).getCard() == null) return;
            }
        }
        for (int j = 0; j < board.getPlayersNumber(); j++) {
            Player player = board.getPlayer(j);
            for (int i = 0; i < Player.NO_CARDS; i++) {
                CommandCard card = player.getCardField(i).getCard();
                if (card != null) player.discardCommandCard(card);
            }
        }
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);

        board.setCurrentPlayer(playerOrder.get(0));
        board.setStep(0);
    }

    public List<Player> getPlayerOrder() {
        return playerOrder;
    }

    public void setPlayerOrder(List<Player> playerOrder) {
        this.playerOrder = playerOrder;
    }

    public void determinePlayerOrder() {
        playerOrder = new ArrayList<>();
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            for (int j = 0; j < playerOrder.size(); j++) {
                if (board.getDistanceToAntenna(board.getPlayer(i).getSpace()) <
                        board.getDistanceToAntenna(playerOrder.get(j).getSpace())) {
                    playerOrder.add(j, board.getPlayer(i));
                    break;
                }
                if (board.getDistanceToAntenna(board.getPlayer(i).getSpace()) ==
                        board.getDistanceToAntenna(playerOrder.get(j).getSpace())) {
                    if (board.getAngleToAntenna(board.getPlayer(i).getSpace()) <
                            board.getAngleToAntenna(playerOrder.get(j).getSpace())) {
                        playerOrder.add(j, board.getPlayer(i));
                        break;
                    }
                }
            }
            if (!playerOrder.contains(board.getPlayer(i)))
                playerOrder.add(playerOrder.size(), board.getPlayer(i));
        }
    }

    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }

    private void executeNextStep() {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                CommandCard card = currentPlayer.getProgramField(step).getCard();
                if (card != null && !currentPlayer.isRebooting()) {
                    Command command = card.command;
                    while (!commandCardController.executeCommand(this, currentPlayer, command)) {
                        CommandCardField field = currentPlayer.getProgramField(step);
                        field.setCard(currentPlayer.drawCommandCard());
                        card = field.getCard();
                        command = card.command;
                    }
                    // Another card is always chosen, so the damage card is removed while the new card is discarded properly
                    currentPlayer.discardCommandCard(card);

                } else if (currentPlayer.isRebooting()) {
                    currentPlayer.discardCommandCard(card);
                }
                if (board.getPhase() != Phase.PLAYER_INTERACTION) endTurn();
            } else {
                // this should not happen
                assert false;
            }
        } else {
            // this should not happen
            assert false;
        }
    }

    public void endTurn() {
        Player currentPlayer = board.getCurrentPlayer();
        int step = board.getStep();
        int nextPlayerNumber = playerOrder.indexOf(currentPlayer) + 1;
        if (nextPlayerNumber < board.getPlayersNumber()) {
            board.setCurrentPlayer(playerOrder.get(nextPlayerNumber));
        } else {
            step++;

            // TODO: Activate special fields and lasers
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                board.setCurrentPlayer(playerOrder.get(i));
                Space space = board.getCurrentPlayer().getSpace();
                for (FieldAction action : space.getActions()) {
                    if (!(action instanceof Laser)) {
                        action.doAction(this, space);
                    }
                }

            }
            //Fire lasers here
            for (int x = 0; x < board.width; x++) {
                for (int y = 0; y < board.height; y++) {
                    Space space = board.getSpace(x, y);
                    for (FieldAction action : space.getActions()) {
                        if (action instanceof Laser) {
                            action.doAction(this, space);
                        }
                    }
                }
            }

            // Destroy lasers after 500ms
            new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                SpaceView.destroyLasers();
            }}).start();


            if (step < Player.NO_REGISTERS) {
                makeProgramFieldsVisible(step);
                board.setStep(step);
                board.setCurrentPlayer(playerOrder.get(0));
            } else {
                startProgrammingPhase();
            }
        }
    }

    public void makeChoice(Command command) {
        board.setPhase(Phase.ACTIVATION);
        commandCardController.executeCommand(this, board.getCurrentPlayer(), command);
        endTurn();
    }

    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null && targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }


    public void startProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        determinePlayerOrder();
        board.setCurrentPlayer(playerOrder.get(0));
        board.setStep(0);

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                player.stopRebooting();
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    CommandCardField field = player.getProgramField(j);
                    field.setCard(null);
                    field.setVisible(true);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    CommandCardField field = player.getCardField(j);
                    field.setCard(player.drawCommandCard());
                    field.setVisible(true);
                }
            }
        }
    }

    /**
     * A method called when no corresponding controller operation is implemented yet. This
     * should eventually be removed.
     */
    public void notImplemented() {
        // XXX just for now to indicate that the actual method is not yet implemented
        assert false;
    }


    class ImpossibleMoveException extends Exception {

        private Player player;
        private Space space;
        private Heading heading;

        public ImpossibleMoveException(Player player, Space space, Heading heading) {
            super("Move impossible");
            this.player = player;
            this.space = space;
            this.heading = heading;
        }
    }

}
