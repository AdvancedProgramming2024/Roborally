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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;

import dk.dtu.compute.se.pisd.roborally.fileaccess.Adapter;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.PlayerTemplate;
import dk.dtu.compute.se.pisd.roborally.online.RequestCenter;
import dk.dtu.compute.se.pisd.roborally.RoboRallyClient;

import dk.dtu.compute.se.pisd.roborally.online.ResourceLocation;
import dk.dtu.compute.se.pisd.roborally.online.Response;
import dk.dtu.compute.se.pisd.roborally.view.SpaceView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static dk.dtu.compute.se.pisd.roborally.online.ResourceLocation.*;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class AppController implements Observer {

    final private List<Integer> PLAYER_NUMBER_OPTIONS = Arrays.asList(2, 3, 4, 5, 6);

    @Getter
    final private RoboRallyClient roboRally;
    private volatile Thread waitForPlayers;
    private volatile Thread waitForGame;

    private GameController gameController; // TODO: Remove later and fix functions depending on this

    public AppController(@NotNull RoboRallyClient roboRally) {
        this.roboRally = roboRally;
    }


    public void newLobby() {
        TextInputDialog nameInput = new TextInputDialog();
        nameInput.setTitle("Player name");
        nameInput.setHeaderText("Please state your name");
        Optional<String> name = nameInput.showAndWait();

        try {
            if (name.isEmpty()) {
                return;
            }
            while (name.get().trim().isEmpty()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("You must enter a name");
                alert.showAndWait();

                name = nameInput.showAndWait();
                if (name.isEmpty()) {
                    return;
                }
            }
            Map<String, Object> playerName = Map.of("playerName", name.get());
            Response<String> lobbyResponse = RequestCenter.postRequest(makeUri(lobbies), playerName);
            if (!lobbyResponse.getStatusCode().is2xxSuccessful()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(lobbyResponse.getItem());
                alert.showAndWait();
                return;
            }
            roboRally.setLobbyId(lobbyResponse.getItem());
            roboRally.setPlayerName(name.get());
            roboRally.createLobbyView();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        startWaitingForPlayers();
        startWaitingForGame();
    }

    public void joinLobby() {
        TextInputDialog idInput = new TextInputDialog();
        idInput.setTitle("Join lobby");
        idInput.setHeaderText("Enter a lobby ID");
        Optional<String> id = idInput.showAndWait();
        try {
            boolean successful = false;
            while (!successful) {
                if (id.isEmpty()) {
                    return;
                }
                while (id.get().length() != 4) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Lobby ID must be 4 digits long");
                    alert.showAndWait();

                    id = idInput.showAndWait();
                    if (id.isEmpty()) {
                        return;
                    }
                }
                Response<String> lobbyResponse = RequestCenter.getRequest(makeUri(lobbyPath(id.get())));
                if (!lobbyResponse.getStatusCode().is2xxSuccessful()) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(lobbyResponse.getItem());
                    alert.showAndWait();
                    id = idInput.showAndWait();
                } else {
                    successful = true;
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        TextInputDialog nameInput = new TextInputDialog();
        nameInput.setTitle("Player name");
        nameInput.setHeaderText("Please state your name");
        Optional<String> name = nameInput.showAndWait();

        try {
            boolean successful = false;
            while (!successful) {
                if (name.isEmpty()) {
                    return;
                }
                while (name.get().trim().isEmpty()) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("You must enter a name");
                    alert.showAndWait();

                    name = nameInput.showAndWait();
                    if (name.isEmpty()) {
                        return;
                    }
                }
                Map<String, Object> playerName = Map.of("playerName", name.get());
                Response<String> joinResponse = RequestCenter.postRequest(makeUri(joinLobbyPath(id.get())), playerName);
                if (!joinResponse.getStatusCode().is2xxSuccessful()) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(joinResponse.getItem());
                    alert.showAndWait();
                    name = nameInput.showAndWait();
                } else {
                    successful = true;
                }
            }
            roboRally.setLobbyId(id.get());
            roboRally.setPlayerName(name.get());
            roboRally.createLobbyView();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        startWaitingForPlayers();
        startWaitingForGame();
    }

    public void leaveLobby() {
        try {
            Map<String, Object> playerName = Map.of("playerName", roboRally.getPlayerName());
            Response<String> response = RequestCenter.postRequest(makeUri(leaveLobbyPath(roboRally.getLobbyId())), playerName);
            if (!response.getStatusCode().is2xxSuccessful()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(response.getItem());
                alert.showAndWait();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        stopWaiting();
        roboRally.returnToMenu();
    }

    private void waitForPlayers() {
        Thread thisThread = Thread.currentThread();
        while (waitForPlayers == thisThread) {
            try {
                Response<JsonObject> response = RequestCenter.getRequestJson(makeUri(lobbyStatePath(roboRally.getLobbyId())));
                roboRally.updateLobbyView(response.getItem());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Thread has stopped");
    }

    private void waitForGame() {
        Thread thisThread = Thread.currentThread();
        while (waitForGame == thisThread) {
            try {
                Thread.sleep(2000);
                Response<JsonObject> response = RequestCenter.getRequestJson(ResourceLocation.makeUri(
                        ResourceLocation.gameStatePath(roboRally.getLobbyId())+"/"+roboRally.getPlayerName()));
                if (!response.getStatusCode().is2xxSuccessful()) {
                    continue;
                }

                GsonBuilder simpleBuilder = new GsonBuilder().
                        registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>()).
                        setPrettyPrinting();
                Gson gson = simpleBuilder.create();

                GameTemplate gameState = gson.fromJson(response.getItem().getAsJsonObject().get("gameState").getAsString(), GameTemplate.class);

                Platform.runLater(() -> startGame(gameState));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Thread has stopped");
    }

    public void startWaitingForPlayers() {
        if (waitForPlayers == null) {
            waitForPlayers = new Thread(this::waitForPlayers);
            waitForPlayers.start();
        }
    }
    public void startWaitingForGame() {
        if (waitForGame == null) {
            waitForGame = new Thread(this::waitForGame);
            waitForGame.start();
        }
    }

    public void stopWaiting() {
        if (waitForPlayers != null) {
            Thread tempThread = waitForPlayers;
            waitForPlayers = null;
            try {
                tempThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (waitForGame != null) {
            Thread tempThread = waitForGame;
            waitForGame = null;
            try {
                tempThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void startGame(GameTemplate gameState) {
        stopWaiting();
        if (roboRally.getBoardView() == null) roboRally.createBoardView(gameState);
    }

    public void createGame() {
        ChoiceDialog<String> mapDialog = new ChoiceDialog<>("dizzy_highway", "defaultboard", "dizzy_highway", "high_octane");
        mapDialog.setTitle("Map selection");
        mapDialog.setHeaderText("Select map to play on");
        Optional<String> mapName = mapDialog.showAndWait();
        if (mapName.isEmpty()) {
            return;
        }
        try {
            JsonObject info = new JsonObject();
            info.addProperty("mapName", mapName.get());
            info.addProperty("playerName", roboRally.getPlayerName());

            Response<JsonObject> response = RequestCenter.postRequestJson(makeUri(gamePath(roboRally.getLobbyId())), info);
            if (!response.getStatusCode().is2xxSuccessful()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(response.getItem().getAsJsonObject().get("info").getAsString());
                alert.showAndWait();
            } else {

                GsonBuilder simpleBuilder = new GsonBuilder().
                        registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>()).
                        setPrettyPrinting();
                Gson gson = simpleBuilder.create();

                GameTemplate gameState = gson.fromJson(response.getItem().getAsJsonObject().get("gameState").getAsString(), GameTemplate.class);
                startGame(gameState);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void drawLasers() {
        // TODO: Draw lasers

        // Destroy lasers after 500ms
        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                SpaceView.destroyLasers();
            }}).start();
    }

    public boolean moveCards(@NotNull String source, @NotNull String target, PlayerTemplate player) {
        GameTemplate gameState;

        int sourceIndex = Integer.parseInt(source.split(",")[1]);
        boolean sourceIsProgrammingCard = source.split(",")[0].equals("P");
        int targetIndex = Integer.parseInt(target.split(",")[1]);
        boolean targetIsProgrammingCard = target.split(",")[0].equals("P");
        if ((sourceIsProgrammingCard ? player.program : player.hand)[sourceIndex] == -1 ||
                (targetIsProgrammingCard ? player.program : player.hand)[targetIndex] != -1) {
            return false;
        }

        JsonObject info = new JsonObject();
        info.addProperty("sourceIndex", sourceIndex);
        info.addProperty("targetIndex", targetIndex);
        info.addProperty("sourceIsProgram", sourceIsProgrammingCard);
        info.addProperty("targetIsProgram", targetIsProgrammingCard);
        info.addProperty("playerName", roboRally.getPlayerName());
        try {
            Response<JsonObject> response = RequestCenter.postRequestJson(makeUri(playerCardMovementPath(roboRally.getLobbyId(), player.id)), info);
            if (!response.getStatusCode().is2xxSuccessful()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(response.getItem().getAsString());
                alert.showAndWait();
                return false;
            } else {

                GsonBuilder simpleBuilder = new GsonBuilder().
                        registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>()).
                        setPrettyPrinting();
                Gson gson = simpleBuilder.create();

                gameState = gson.fromJson(response.getItem().getAsJsonObject().get("gameState").getAsString(), GameTemplate.class);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        roboRally.updateBoardView(gameState);
        return true;
    }

    public void sendReadySignal() {
        try {
            GameTemplate gameState = roboRally.getGameState();
            int playerId = -1;
            for (int i = 0; i < gameState.players.size(); i++) {
                if (gameState.players.get(i).name.equals(roboRally.getPlayerName())) {
                    playerId = i;
                    break;
                }
            }
            Response<String> response = RequestCenter.getRequest(makeUri(playerReadyPath(roboRally.getLobbyId(), playerId)));
            if (!response.getStatusCode().is2xxSuccessful()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(response.getItem());
                alert.showAndWait();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /*public void newGame() {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(PLAYER_NUMBER_OPTIONS.get(0), PLAYER_NUMBER_OPTIONS);
        dialog.setTitle("Player number");
        dialog.setHeaderText("Select number of players");
        Optional<Integer> result = dialog.showAndWait();


        // Temporary map selection dialog. Should be replaced with a new scene for selecting maps
        ChoiceDialog<String> mapDialog = new ChoiceDialog<>("dizzy_highway", "defaultboard", "dizzy_highway", "high_octane");
        mapDialog.setTitle("Map selection");
        mapDialog.setHeaderText("Select map to play on");
        Optional<String> mapName = mapDialog.showAndWait();

        if (result.isPresent() && mapName.isPresent()) {
            if (gameController != null) {
                // The UI should not allow this, but in case this happens anyway.
                // give the user the option to save the game or abort this operation!
                if (!stopGame(true)) {
                    return;
                }
            }

            // @TODO set new scene where you can choose which map to play on

            // XXX the board should eventually be created programmatically or loaded from a file
            //     here we just create an empty board with the required number of players.
            Board board = loadBoard(mapName.get());
            assert board != null;
            board.setGameId((int)(Math.random() * 100));
            //saveBoard(board, "test");

            gameController = new GameController(board);
            Player.server = this;
            int no = result.get();
            for (int i = 0; i < no; i++) {
                Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1), i);
                board.addPlayer(player);
                player.setSpace(board.getSpace(i % board.width, i));
            }

            // XXX: V2
            // board.setCurrentPlayer(board.getPlayer(0));
            gameController.startProgrammingPhase();

            roboRally.createBoardView(gameController);
        }
    }

    public void saveGame() {
        String fileName = inputBox(true);
        if (fileName == null) return;

        saveGameState(gameController, fileName);
    }

    public void loadGame() {
        if (gameController == null) {
            String fileName = inputBox(false);
            if (fileName == null) return;
            gameController = loadGameState(fileName);
            if (gameController == null) return;
            roboRally.createBoardView(gameController);
        }
    }*/

    /**
     * Create a dialog box for the user to input a filename.
     * @author Kresten (s235103)
     * @param saving true if the dialog box is for saving a file, false if it is for loading a file
     * @return the filename input by the user
     */
    private String inputBox(boolean saving) {
        Label label = new Label(saving ? "Save game as:" : "Load game from:");
        TextField filenameField = new TextField();
        Button button = new Button(saving ? "Save" : "Load");
        button.setOnAction(e -> {
            Stage stage = (Stage) button.getScene().getWindow();
            stage.close();
        });
        Stage stage = new Stage();
        stage.setOnCloseRequest(e -> filenameField.setText(null));
        VBox root = new VBox();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(saving ? "Save" : "Load" + " game");
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        root.getChildren().addAll(label, filenameField, button);
        root.setPadding(new Insets(10));
        stage.showAndWait();

        return filenameField.getText();
    }

    /**
     * Stop playing the current game, giving the user the option to save
     * the game or to cancel stopping the game. The method returns true
     * if the game was successfully stopped (with or without saving the
     * game); returns false, if the current game was not stopped. In case
     * there is no current game, false is returned.
     *
     * @return true if the current game was stopped, false otherwise
     */
    public boolean stopGame(boolean savedDialog) {
        if (gameController != null) {
            if (savedDialog) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Exit RoboRally?");
                alert.setContentText("Are you sure you want to close RoboRally?\n" +
                        "Have you remembered to save the game? Unsaved progress wil be deleted!");
                Optional<ButtonType> result = alert.showAndWait();

                if (!result.isPresent() || result.get() != ButtonType.OK) {
                    return false;
                }
            }

            roboRally.returnToMenu();
            gameController = null;
            roboRally.createBoardView(null);
            return true;
        }
        return false;
    }

    public void exit() {
        // If the user did not cancel, the RoboRally application will exit
        if (gameController == null || stopGame(true)) {
            Platform.exit();
        }
    }

    public boolean isGameRunning() {
        return gameController != null;
    }


    @Override
    public void update(Subject subject) {
        // XXX do nothing for now
    }

}
