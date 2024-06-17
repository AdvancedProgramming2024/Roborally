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

import com.google.gson.*;
import dk.dtu.compute.se.pisd.roborally.controller.AppController;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.fileaccess.Adapter;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.SpaceTemplate;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.online.RequestCenter;
import dk.dtu.compute.se.pisd.roborally.online.ResourceLocation;
import dk.dtu.compute.se.pisd.roborally.online.Response;
import dk.dtu.compute.se.pisd.roborally.view.BoardView;
import dk.dtu.compute.se.pisd.roborally.view.MenuButtons;
import dk.dtu.compute.se.pisd.roborally.view.RoboRallyMenuBar;
import dk.dtu.compute.se.pisd.roborally.view.SpaceView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
@Getter
@Setter
public class RoboRallyClient extends Application {

    private static final int MIN_APP_WIDTH = 600;
    private boolean poll;
    private String lobbyId;
    private String playerName;
    private AppController appController;

    private static Stage stage;
    private BorderPane boardRoot;
    private VBox gameRoot;
    private static TilePane menuPane;
    private static TilePane lobbyPane;
    private static Scene scene;

    private ScheduledExecutorService executorService;
    private BoardView boardView;
    private GameTemplate gameState;

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
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        appController = new AppController(this);

        // create the primary scene with a menu bar and a pane for
        // the board view (which initially is empty); it will be filled
        // when the user creates a new game or loads a game
        RoboRallyMenuBar menuBar = new RoboRallyMenuBar(appController);
        MenuButtons menuButtons = new MenuButtons(appController);
        boardRoot = new BorderPane();
        gameRoot = new VBox(menuBar, boardRoot);
        gameRoot.setMinWidth(MIN_APP_WIDTH);
        menuPane = new TilePane(Orientation.VERTICAL);
        menuPane.getChildren().add(menuButtons.newGameButton);
        menuPane.getChildren().add(menuButtons.joinGameButton);
        menuPane.getChildren().add(menuButtons.exitGameButton);
        menuPane.getChildren().add(menuButtons.ruleButton);
        lobbyPane = new TilePane(Orientation.VERTICAL);

        //style for the menu
        menuPane.setAlignment(Pos.CENTER);
        menuPane.setVgap(15);

        //style for the lobby
        lobbyPane.setAlignment(Pos.CENTER);
        lobbyPane.setVgap(15);

        //Menu Background image
        Image menu = new Image("images/RoboRallyBackground.png");
        BackgroundImage backgroundMenu = new BackgroundImage(
                menu, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(100, 100, true, true, true, true)
        );
        menuPane.setBackground(new Background(backgroundMenu));

        //Lobby Background image
        Image lobby = new Image("images/empty.png");
        BackgroundImage backgroundLobby = new BackgroundImage(
                lobby, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(240, 240, false, false, false, false)
        );
        lobbyPane.setBackground(new Background(backgroundLobby));

        scene = new Scene(menuPane, screenWidth/1.5, screenHeight/1.5);
        stage.setScene(scene);
        stage.setTitle("RoboRally");
        stage.setOnCloseRequest(
                e -> {
                    e.consume();
                    if (lobbyId != null) {appController.leaveLobby();}
                        if (poll) {suspendPolling();}
                    appController.exit();}
        );
        stage.setResizable(true);
        stage.setMaximized(false);
        stage.show();
    }

    public void suspendPolling() {
        executorService.shutdown();
    }
    /**
     * This appoach must change if the timer is implemented.
     * Client will not start polling the server until they click the "Finish Programming" button.
     */
    public void startPolling() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::pollServer, 0, 500, TimeUnit.MILLISECONDS);
    }


    private void pollServer() {
        if (lobbyId == null) {
            return;
        }
        try {
            poll = true;
            Response<JsonObject> response = RequestCenter.getRequestJson(ResourceLocation.makeUri(ResourceLocation.gameStatePath(lobbyId)+"/"+getPlayerName()));
            if (!response.getStatusCode().is2xxSuccessful()) {
                return;
            }
            JsonObject gameStateJson = response.getItem().getAsJsonObject();
            GsonBuilder simpleBuilder = new GsonBuilder().
                    registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>()).
                    setPrettyPrinting().setLenient();
            Gson gson = simpleBuilder.create();
            gameState = gson.fromJson(gameStateJson.get("gameState").getAsString(), GameTemplate.class);
            if (gameState == null || boardView == null) return;
            // TODO: Implement timestamp check before phase check, else it stops immediately because gamestate
            // since gamestate hasn't been updated with the new phase yet
            //if (!(gameState.playPhase == Phase.ACTIVATION.ordinal() || gameState.playPhase == Phase.UPGRADE.ordinal())) suspendPolling();
            Platform.runLater(() -> updateBoardView(gameState));

            //JsonElement tmp = gameStateJson.get("lasers");
            JsonArray lasers = gameStateJson.get("lasers").getAsJsonArray();
            if (lasers.size() == 0) SpaceView.destroyLasers();
            for (JsonElement laser : lasers) {
                JsonObject laserObj = laser.getAsJsonObject();
                JsonArray LOS = laserObj.get("LOS").getAsJsonArray();
                int heading = laserObj.get("heading").getAsInt();

                List<SpaceTemplate> spaces = new ArrayList<>();
                for (JsonElement los : LOS) {
                    JsonObject spaceObject = los.getAsJsonObject();
                    int x = spaceObject.get("x").getAsInt();
                    int y = spaceObject.get("y").getAsInt();

                    spaces.add(gameState.board.spaces.get(x * gameState.board.height + y));
                }
                while (boardView.getSpaces()[gameState.board.height-1][gameState.board.width-1].gameState != gameState) {
                    Thread.sleep(100);
                }
                Platform.runLater(() -> SpaceView.drawLaser(spaces, Heading.values()[heading]));
            }
            if (gameState.winnerName != null) {
                suspendPolling();
                Platform.runLater(this::displayWinner);
            }
            /*if (gameStateJson.get("lasers") == null) SpaceView.destroyLasers();
            JsonArray lasers = gameStateJson.get("lasers").getAsJsonArray();
            for (JsonElement laser : lasers) {
                JsonObject laserObj = laser.getAsJsonObject();
                JsonArray LOS = laserObj.get("laser").getAsJsonArray();
                List<SpaceTemplate> spaces = new ArrayList<>();
                for (JsonElement los : LOS) {
                    spaces.add(gson.fromJson(los.getAsString(), SpaceTemplate.class));
                }
                Heading heading = Heading.valueOf(laserObj.get("heading").getAsString());
                SpaceView.drawLaser(spaces, heading);
            }*/
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void displayWinner() {
        Alert winnerAlert = new Alert(Alert.AlertType.INFORMATION);
        winnerAlert.setTitle("A player has won the game!");
        winnerAlert.setHeaderText("Congratulations to " + gameState.winnerName + "!\nThey have won the game!");
        winnerAlert.setContentText("Returning to main menu.");
        winnerAlert.showAndWait();
        appController.leaveLobby();
    }

    public void createLobbyView() {
        boardRoot.getChildren().clear();
        lobbyPane.getChildren().clear();
        stage.setMaximized(false);

        Text lobbyInfo = new Text("Lobby: " + lobbyId + "\nYour username: " + getPlayerName());
        lobbyInfo.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 20));
        lobbyInfo.setTextAlignment(TextAlignment.LEFT);
        lobbyPane.getChildren().add(lobbyInfo);

        Text playersText = new Text("Players:");
        playersText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 20));
        playersText.setTextAlignment(TextAlignment.LEFT);
        lobbyPane.getChildren().add(playersText);

        Button startBtn = new Button("Start Game");
        Button loadBtn = new Button("Load Game");
        Button leaveBtn = new Button("Leave Lobby");
        startBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> appController.createGame());
        //loadBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> appController.loadGame()); //TODO: this
        leaveBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> appController.leaveLobby());

        lobbyPane.getChildren().add(new HBox(15, startBtn, loadBtn, leaveBtn));
        scene.setRoot(lobbyPane);
    }

    public void updateLobbyView(JsonObject lobbyContent) {
        if (lobbyContent == null) {
            return;
        }
        JsonArray players = lobbyContent.get("players").getAsJsonArray();
        String host = players.get(0).getAsString();
        Text info = (Text) lobbyPane.getChildren().get(0);
        String infoContent = info.getText();
        int hostStartIndex = infoContent.indexOf("\nHost: ");
        if (infoContent.contains("Host: ")) {
            infoContent = infoContent.substring(0, hostStartIndex);
        }
        info.setText(infoContent + "\nHost: " + host);

        Text text = ((Text) lobbyPane.getChildren().get(1));
        StringBuilder newText = new StringBuilder();
        newText.append("Players:");
        for (int i = 0; i < players.size(); i++) {
            newText.append("\nPlayer ").append(i+1).append(": ").append(players.get(i).getAsString());
        }
        if (!text.getText().contentEquals(newText)) {
            text.setText(newText.toString());
        }
    }

    public void createBoardView(GameTemplate gameState) {
        // if present, remove old BoardView
        boardRoot.getChildren().clear();
        stage.setMaximized(true);
        this.gameState = gameState;

        if (gameState != null) {
            // create and add view for new board
            boardView = new BoardView(appController, gameState, this);
            boardRoot.setCenter(boardView);
            //boardView.updateView(gameState.board); // TODO figure out what to do
            scene.setRoot(gameRoot);
            updateBoardView(gameState);
        }
        //stage.setMaximized(true);
    }

    public void updateBoardView(GameTemplate gameState) {
        if (gameState != null) {
            this.gameState = gameState;
            BoardView boardView = (BoardView) boardRoot.getCenter();
            boardView.updateView(gameState);
        }
    }
    /**
     * changes the menu from the in game menu to the start menu.
     * @author Oscar (224752)
     */
    public void returnToMenu() {
        setPlayerName(null);
        setLobbyId(null);
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