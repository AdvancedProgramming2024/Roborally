package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dk.dtu.compute.se.pisd.roborally.model.Command;

class CommandCardControllerTest {

    private GameController gameController;

    @BeforeEach
    void setUp() {
        int TEST_HEIGHT = 8;
        int TEST_WIDTH = 8;
        Board board = new Board(TEST_WIDTH, TEST_HEIGHT);
        gameController = new GameController(board);
        for (int i = 0; i < 6; i++) {
            Player player = new Player(board, null,"Player " + i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(i, i));
            player.setHeading(Heading.SOUTH);
        }
        board.setCurrentPlayer(board.getPlayer(0));
    }

    @AfterEach
    void tearDown() {
        gameController = null;
    }

    /**
     * @author Jonathan (s235115)
     */
    @Test
    void moveCommands() {
        Board board = gameController.board;
        Player player1 = board.getPlayer(0);
        Player player2 = board.getPlayer(1);
        Player player3 = board.getPlayer(2);
        Player player4 = board.getPlayer(3);

        gameController.commandCardController.executeCommand(gameController, player1, Command.MOVE_3);
        gameController.commandCardController.executeCommand(gameController, player2, Command.MOVE_2);
        gameController.commandCardController.executeCommand(gameController, player3, Command.MOVE_1);
        gameController.commandCardController.executeCommand(gameController, player4, Command.MOVE_BACK);

        Assertions.assertEquals(player1, board.getSpace(0, 3).getPlayer(), "Player " + player1.getName() + " should be Space (0,3)!");
        Assertions.assertEquals(player2, board.getSpace(1, 3).getPlayer(), "Player " + player2.getName() + " should be Space (1,3)!");
        Assertions.assertEquals(player3, board.getSpace(2, 3).getPlayer(), "Player " + player3.getName() + " should be Space (2,3)!");
        Assertions.assertEquals(player4, board.getSpace(3, 2).getPlayer(), "Player " + player4.getName() + " should be Space (3,2)!");
        Assertions.assertNull(board.getSpace(0, 0).getPlayer(), "Space (0,0) should be empty!");
        Assertions.assertNull(board.getSpace(1, 1).getPlayer(), "Space (1,1) should be empty!");
        Assertions.assertNull(board.getSpace(2, 2).getPlayer(), "Space (2,2) should be empty!");
        Assertions.assertNull(board.getSpace(3, 3).getPlayer(), "Space (3,3) should be empty!");
    }

    /**
     * @author Jonathan (s235115)
     */
    @Test
    void turnCommands() {
        Board board = gameController.board;
        Player player1 = board.getPlayer(0);
        Player player2 = board.getPlayer(1);
        Player player3 = board.getPlayer(2);

        gameController.commandCardController.executeCommand(gameController, player1, Command.LEFT);
        gameController.commandCardController.executeCommand(gameController, player2, Command.RIGHT);
        gameController.commandCardController.executeCommand(gameController, player3, Command.U_TURN);

        Assertions.assertEquals(Heading.EAST, player1.getHeading(), "Player " + player1.getName() + " should be facing EAST!");
        Assertions.assertEquals(Heading.WEST, player2.getHeading(), "Player " + player2.getName() + " should be facing WEST!");
        Assertions.assertEquals(Heading.NORTH, player3.getHeading(), "Player " + player3.getName() + " should be facing NORTH!");
    }

    /**
     * @author Jonathan (s235115)
     */
    @Test
    void powerUp() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.commandCardController.executeCommand(gameController, current, Command.POWER_UP);

        // TODO: Add test when energy cubes are implemented
    }
}