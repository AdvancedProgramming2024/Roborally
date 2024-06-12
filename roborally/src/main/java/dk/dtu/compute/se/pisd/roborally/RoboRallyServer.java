package dk.dtu.compute.se.pisd.roborally;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.SpaceTemplate;
import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.online.Lobby;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave.createSpaceTemplate;
import static dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave.loadBoard;
@Setter
@Getter
public class RoboRallyServer {
    private final GameController gameController;
    private Lobby lobby;
    private boolean gameWon;
    private GameTemplate gameState = null;
    private Map<List<SpaceTemplate>,Heading> laser = new HashMap<>();

    public RoboRallyServer(ArrayList<String> players, String mapName, Lobby lobby) {
        Board board = loadBoard(mapName);
        assert board != null;
        board.setGameId(Integer.parseInt(lobby.getID()));

        gameController = new GameController(board, this);
        Player.server = this;
        List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");
        for (int i = 0; i < players.size(); i++) {
            Player player = new Player(board, PLAYER_COLORS.get(i), players.get(i), i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(0, i));
            player.setHeading(Heading.EAST);
        }

        //TODO: Send game state to all players and wait for ack

        // XXX: V2
        // board.setCurrentPlayer(board.getPlayer(0));

        gameController.startProgrammingPhase();
        updateGameState();
    }

    public void startGameLoop() {
        boolean gameRunning = true;
        while (gameRunning) {
            waitForAcks(); // Wait for players to have sent their programming registers
            System.out.println("Starting activation phase");
            gameController.finishProgrammingPhase();
            while (gameController.board.getPhase() == Phase.ACTIVATION) {
                gameController.executeStep();
                waitForAcks();
                if (gameWon) {
                    gameRunning = false;
                    break;
                }
            }
        }
        lobby.stopGame();
    }

    public GameController getGameController() {
        return gameController;
    }

    public void stopGame() {
        // TODO: Do something
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
        List<SpaceTemplate> temp = new ArrayList<>();
        for (Space space : los) {
            temp.add(createSpaceTemplate(space, gameController.board));
        }
        laser.put(temp, heading);
    }

    public Map<List<SpaceTemplate>, Heading> getLaser() {
        return laser;
    }

    public GameTemplate getGameState(String playerName) {
        GameTemplate tmp = gameState.clone();
        for (int i = 0; i < gameState.players.size(); i++) {
            if (!tmp.players.get(i).name.equals(playerName)) {
                Arrays.fill(tmp.players.get(i).program, -1);
                Arrays.fill(tmp.players.get(i).hand, -1);
            }
        }
        return tmp;
    }
}
