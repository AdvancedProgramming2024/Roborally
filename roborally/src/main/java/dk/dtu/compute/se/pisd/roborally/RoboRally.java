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
package dk.dtu.compute.se.pisd.roborally;

import dk.dtu.compute.se.pisd.roborally.controller.AppController;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.view.BoardView;
import dk.dtu.compute.se.pisd.roborally.view.MenuButtons;
import dk.dtu.compute.se.pisd.roborally.view.RoboRallyMenuBar;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class RoboRally extends Application {

    private static final int MIN_APP_WIDTH = 600;

    private static Stage stage;
    private BorderPane boardRoot;
    private VBox gameRoot;
    private static TilePane menuPane;
    private static Scene scene;

    @Override
    public void init() throws Exception {
        super.init();
    }

    /**
     * Creates the stage and scene. The input into the scene changes depending on if the start menu is needed or the in game menu is needed.
     * @author Oscar (224752)
     */
    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setMaximized(true);
        double screenWidth = stage.getMaxWidth();
        double screenHeight = stage.getMaxHeight();

        AppController appController = new AppController(this);

        // create the primary scene with the a menu bar and a pane for
        // the board view (which initially is empty); it will be filled
        // when the user creates a new game or loads a game
        RoboRallyMenuBar menuBar = new RoboRallyMenuBar(appController);
        MenuButtons menuButtons = new MenuButtons(appController);
        boardRoot = new BorderPane();
        gameRoot = new VBox(menuBar, boardRoot);
        gameRoot.setMinWidth(MIN_APP_WIDTH);
        menuPane = new TilePane(Orientation.VERTICAL);
        menuPane.getChildren().add(menuButtons.newGameButton);
        menuPane.getChildren().add(menuButtons.loadGameButton);
        menuPane.getChildren().add(menuButtons.exitGameButton);

        //style for the menu
        menuPane.setAlignment(Pos.CENTER);
        menuPane.setVgap(15);

        //placeholder indtil vi har et billede
        menuPane.setStyle("-fx-background-color: green;");

        scene = new Scene(menuPane, screenWidth, screenHeight);
        stage.setScene(scene);
        stage.setTitle("RoboRally");
        stage.setOnCloseRequest(
                e -> {
                    e.consume();
                    appController.exit();} );
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
    }

    public void createBoardView(GameController gameController) {
        // if present, remove old BoardView
        boardRoot.getChildren().clear();

        if (gameController != null) {
            // create and add view for new board
            BoardView boardView = new BoardView(gameController);
            boardRoot.setCenter(boardView);
            boardView.updateView(gameController.board);
            scene.setRoot(gameRoot);
        }
        //stage.setMaximized(true);
    }
    /**
     * changes the menu from the in game menu to the start menu.
     * @author Oscar (224752)
     */
    public static void returnToMenu() {
        scene.setRoot(menuPane);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        // XXX just in case we need to do something here eventually;
        //     but right now the only way for the user to exit the app
        //     is delegated to the exit() method in the AppController,
        //     so that the AppController can take care of that.
    }

    public static void main(String[] args) {
        launch(args);
    }

}