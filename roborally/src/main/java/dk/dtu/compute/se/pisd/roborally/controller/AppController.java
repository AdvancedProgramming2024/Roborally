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

import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;

import dk.dtu.compute.se.pisd.roborally.RoboRally;

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Player;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave.loadBoard;
import static dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave.saveBoard;
import static dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave.loadGameState;
import static dk.dtu.compute.se.pisd.roborally.fileaccess.LoadSave.saveGameState;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class AppController implements Observer {

    final private List<Integer> PLAYER_NUMBER_OPTIONS = Arrays.asList(2, 3, 4, 5, 6);
    final private List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");

    final private RoboRally roboRally;

    private GameController gameController;

    public AppController(@NotNull RoboRally roboRally) {
        this.roboRally = roboRally;
    }

    public void newGame() {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(PLAYER_NUMBER_OPTIONS.get(0), PLAYER_NUMBER_OPTIONS);
        dialog.setTitle("Player number");
        dialog.setHeaderText("Select number of players");
        Optional<Integer> result = dialog.showAndWait();


        // Temporary map selection dialog. Should be replaced with a new scene for selecting maps
        ChoiceDialog<String> mapDialog = new ChoiceDialog<>("defaultboard", "defaultboard", "dizzy_highway", "high_octane");
        mapDialog.setTitle("Map selection");
        mapDialog.setHeaderText("Select map to play on");
        Optional<String> mapName = mapDialog.showAndWait();

        if (result.isPresent() && mapName.isPresent()) {
            if (gameController != null) {
                // The UI should not allow this, but in case this happens anyway.
                // give the user the option to save the game or abort this operation!
                if (!stopGame()) {
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

        saveGameState(gameController, fileName);
    }

    public void loadGame() {
        if (gameController == null) {
            String fileName = inputBox(false);
            gameController = loadGameState(fileName);
            if (gameController == null) return;
            roboRally.createBoardView(gameController);
        }
    }

    private String inputBox(boolean saving) {
        Label label = new Label(saving ? "Save game as:" : "Load game from:");
        TextField filenameField = new TextField();
        Button button = new Button(saving ? "Save" : "Load");
        button.setOnAction(e -> {
            Stage stage = (Stage) button.getScene().getWindow();
            stage.close();
        });
        Stage stage = new Stage();
        VBox root = new VBox();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(saving ? "Save" : "Load" + "game");
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
    public boolean stopGame() {
        if (gameController != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Exit RoboRally?");
            alert.setContentText("Are you sure you want to close RoboRally?\n" +
                    "Have you remembered to save the game? Unsaved progress wil be deleted!");
            Optional<ButtonType> result = alert.showAndWait();

            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return false;
            }
          
            RoboRally.returnToMenu();
            gameController = null;
            roboRally.createBoardView(null);
            return true;
        }
        return false;
    }

    public void exit() {
        // If the user did not cancel, the RoboRally application will exit
        if (gameController == null || stopGame()) {
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
