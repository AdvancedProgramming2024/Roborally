package dk.dtu.compute.se.pisd.roborally;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.SpaceTemplate;
import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.online.Lobby;
import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave.createSpaceTemplate;
import static dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave.loadBoard;
@Setter
@Getter
public class RoboRallyServer {
    @Getter
    private GameController gameController;
    private Lobby lobby;
    private boolean gameWon;
    private GameTemplate gameState = null;
    private Map<List<Space>,Heading> laser = new HashMap<>();
    private List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");

    public RoboRallyServer(Lobby lobby) {
        this.lobby = lobby;
    }

    public void createGame(ArrayList<String> players, String mapName) {
        Board board = loadBoard(mapName);
        assert board != null;
        board.setGameId(Integer.parseInt(lobby.getID()));

        gameController = new GameController(board, this);
        Player.server = this;
        for (int i = 0; i < players.size(); i++) {
            Player player = new Player(board, PLAYER_COLORS.get(i), players.get(i), i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(0, i));
            player.setHeading(Heading.EAST);
        }
    }

    public void loadGame(GameTemplate gameState) {
        gameController = LoadSave.loadGameState(gameState, this);
        Player.server = this;
    }

    public void startGameLoop() {
        if (gameController.board.getPhase() == Phase.INITIALISATION) gameController.startProgrammingPhase();
        updateGameState();

        boolean gameRunning = true;
        while (gameRunning) {
            if (gameController.board.getPhase() == Phase.PROGRAMMING) {
                waitForAcks(); // Wait for players to have sent their programming registers
                System.out.println("Starting activation phase");
                gameController.finishProgrammingPhase();
            }
            while (gameController.board.getPhase() == Phase.ACTIVATION || gameController.board.getPhase() == Phase.PLAYER_INTERACTION) {
                // Player interaction should be used here to get to the loop when loading a game in the middle of a round
                gameController.executeStep();
                updateGameState();
                while (gameController.board.getPhase() == Phase.PLAYER_INTERACTION) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    updateGameState();
                }
                try {
                    Thread.sleep(750);
                    laser.clear();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (gameWon) {
                    gameRunning = false;
                    break;
                }
            }
        }
        lobby.stopGame();
    }

    public void updateGameState() {
        gameState = LoadSave.saveGameState(gameController, false);
    }

    public void waitForAcks() {
        // wait until the other players are also done
        boolean waiting = true;
        while (waiting) {
            waiting = false;
            for (int i = 0; i < gameController.board.getPlayersNumber(); i++) {
                if (!gameController.board.getPlayer(i).isReady()) {
                    waiting = true;
                }
            }
        }
        updateGameState();
        for (int i = 0; i < gameController.board.getPlayersNumber(); i++) {
            gameController.board.getPlayer(i).setReady(false);
        }
    }

    public void addLaser(List<Space> los, Heading heading) {
        laser.put(los, heading);
    }

    public Map<List<Space>, Heading> getLaser() {
        return laser;
    }

    public GameTemplate getGameState(String playerName) {
        GameTemplate tmp = gameState.clone();
        for (int i = 0; i < gameState.players.size(); i++) {
            if (!tmp.players.get(i).name.equals(playerName)) {
                if (gameController.board.getPhase() == Phase.PROGRAMMING) Arrays.fill(tmp.players.get(i).program, -1);
                Arrays.fill(tmp.players.get(i).hand, -1);
            }
        }
        return tmp;
    }

    public void gameWon(Player player) {
        this.gameWon = true;
        gameController.setWinner(player);
    }
}
