package dk.dtu.compute.se.pisd.roborally;

import com.fasterxml.jackson.databind.util.JSONPObject;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.SpaceTemplate;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import lombok.Getter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave.createSpaceTemplate;
import static dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave.loadBoard;

public class RoboRallyServer {
    private GameController gameController;
    private boolean ready = false;
    @Getter
    private Map<List<SpaceTemplate>,Heading> laser = new HashMap<>();

    public RoboRallyServer(ArrayList<String> players, String mapName, String lobbyId) {
        Board board = loadBoard(mapName);
        assert board != null;
        board.setGameId(Integer.parseInt(lobbyId));

        gameController = new GameController(board, this);
        Player.server = this;
        List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");
        for (int i = 0; i < players.size(); i++) {
            Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1), i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(0, i));
            player.setHeading(Heading.EAST);
        }

        //TODO: Send game state to all players and wait for ack

        // XXX: V2
        // board.setCurrentPlayer(board.getPlayer(0));
        gameController.startProgrammingPhase();
        while (true) {
            waitForAcks(); // Wait for players to have sent their programming registers
            gameController.finishProgrammingPhase();
            for (int i = 0; i < 5; i++) {
                gameController.executeStep();
                waitForAcks();
            }
        }
    }

    public GameController getGameController() {
        return gameController;
    }

    public void stopGame() {
        // TODO: Do something
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void waitForAcks() {
        // wait until the other players are also done
        boolean waiting = true;
        ready = true;
        while (waiting) {
            waiting = false;
            for (int i = 0; i < gameController.board.getPlayersNumber(); i++) {
                if (gameController.board.getPlayer(i).isReady()) {
                    waiting = true;
                }
            }
        }
        ready = false;
        for (int i = 0; i < gameController.board.getPlayersNumber(); i++) {
            gameController.board.getPlayer(i).setReady(false);
        }
    }

    public GameTemplate getGameState() {
        if (ready) {
            return LoadSave.saveGameState(gameController);
        }
        return null;
    }

    public void addLaser(List<Space> los, Heading heading) {
        List<SpaceTemplate> temp = new ArrayList<>();
        for (Space space : los) {
            temp.add(createSpaceTemplate(space, gameController.board));
        }
        laser.put(temp, heading);
    }

    public Map<List<SpaceTemplate>, Heading> getLaser() {
        return laser;
    }
}
