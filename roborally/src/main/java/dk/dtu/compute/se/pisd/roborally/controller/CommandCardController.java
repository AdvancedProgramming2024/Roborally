package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Command;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import org.jetbrains.annotations.NotNull;

public class CommandCardController {
    public void executeCommand(GameController gameController, @NotNull Player player, Command command) {
        if (player.board == gameController.board && command != null) {
            // XXX This is a very simplistic way of dealing with some basic cards and
            //     their execution. This should eventually be done in a more elegant way
            //     (this concerns the way cards are modelled as well as the way they are executed).

            switch (command) {
                case MOVE_1:
                    gameController.moveForward(player);
                    break;
                case MOVE_2:
                    gameController.moveForward(player);
                    gameController.moveForward(player);
                    break;
                case MOVE_3:
                    gameController.moveForward(player);
                    gameController.moveForward(player);
                    gameController.moveForward(player);
                    break;
                case RIGHT:
                    gameController.turn(player, 1);
                    break;
                case LEFT:
                    gameController.turn(player, 3);
                    break;
                case OPTION_LEFT_RIGHT:
                    // TODO: Do something here
                    break;
                case U_TURN:
                    gameController.turn(player, 2);
                    break;
                case MOVE_BACK:
                    gameController.moveInDirection(player, player.getHeading().next().next());
                    break;
                case POWER_UP:
                    // TODO: Do something here
                    break;
                case AGAIN:
                    int i = gameController.board.getStep()-1;
                    if (i < 0 ) return;
                    Command c = gameController.board.getCurrentPlayer().
                            getProgramField(i).getCard().command;
                    executeCommand(gameController, player, c);
                    break;
                default:
                    // DO NOTHING (for now)
            }
        }
    }
}
