package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameControllerTest {

    private final int TEST_WIDTH = 8;
    private final int TEST_HEIGHT = 8;

    private GameController gameController;

    @BeforeEach
    void setUp() {
        Board board = new Board(TEST_WIDTH, TEST_HEIGHT);
        gameController = new GameController(board);
        for (int i = 0; i < 6; i++) {
            Player player = new Player(board, null,"Player " + i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(i, i));
            player.setHeading(Heading.values()[i % Heading.values().length]);
        }
        board.setCurrentPlayer(board.getPlayer(0));
    }

    @AfterEach
    void tearDown() {
        gameController = null;
    }

    @Test
    void moveForward() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.moveForward(current);

        Assertions.assertEquals(current, board.getSpace(0, 1).getPlayer(), "Player " + current.getName() + " should beSpace (0,1)!");
        Assertions.assertEquals(Heading.SOUTH, current.getHeading(), "Player 0 should be heading SOUTH!");
        Assertions.assertNull(board.getSpace(0, 0).getPlayer(), "Space (0,0) should be empty!");
    }

    /**
     * @author Jonathan (s235115)
     */
    @Test

    void turnRight() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.turnRight(current);

        Assertions.assertEquals(current, board.getSpace(0, 0).getPlayer(), "Player " + current.getName() + " should beSpace (0,0)!");
        Assertions.assertEquals(Heading.WEST, current.getHeading(), "Player 0 should be heading WEST!");
    }

    /**
     * @author Jonathan (s235115)
     */
    @Test
    void turnLeft() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.turnLeft(current);

        Assertions.assertEquals(current, board.getSpace(0, 0).getPlayer(), "Player " + current.getName() + " should beSpace (0,0)!");
        Assertions.assertEquals(Heading.EAST, current.getHeading(), "Player 0 should be heading EAST!");
    }

    /**
     * @author Jonathan (s235115)
     */
    @Test
    void determinePlayerOrder() {
        Board board = gameController.board;
        board.setAntenna(0, 3, Heading.EAST);

        gameController.determinePlayerOrder();

        Assertions.assertEquals(board.getPlayer(1), gameController.getPlayerOrder().get(0), "1. player should be " + board.getPlayer(2).getName());
        Assertions.assertEquals(board.getPlayer(2), gameController.getPlayerOrder().get(1), "1. player should be " + board.getPlayer(2).getName());
        Assertions.assertEquals(board.getPlayer(3), gameController.getPlayerOrder().get(2), "1. player should be " + board.getPlayer(2).getName());
        Assertions.assertEquals(board.getPlayer(0), gameController.getPlayerOrder().get(3), "1. player should be " + board.getPlayer(2).getName());
        Assertions.assertEquals(board.getPlayer(4), gameController.getPlayerOrder().get(4), "1. player should be " + board.getPlayer(2).getName());
        Assertions.assertEquals(board.getPlayer(5), gameController.getPlayerOrder().get(5), "1. player should be " + board.getPlayer(2).getName());
    }

    /**
     * @author Jonathan (s235115)
     */
    @Test
    void fallInVoid() {
        Board board = gameController.board;
        board.setRebootStation(0, 3, Heading.EAST);
        Player current = board.getCurrentPlayer();

        gameController.moveInDirection(current, Heading.NORTH);

        Assertions.assertEquals(current.getSpace(), board.getSpace(0, 3), "Player 0 should be at the reboot station!");
        Assertions.assertTrue(current.isRebooting(), "Player 0 should be rebooting!");
    }


}