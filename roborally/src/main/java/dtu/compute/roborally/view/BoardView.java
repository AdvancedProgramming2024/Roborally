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
package dtu.compute.roborally.view;

import dtu.compute.roborally.RoboRallyClient;
import dtu.compute.roborally.controller.AppController;
import dtu.compute.roborally.fileaccess.model.BoardTemplate;
import dtu.compute.roborally.fileaccess.model.GameTemplate;
import dtu.compute.roborally.fileaccess.model.SpaceTemplate;
import dtu.compute.roborally.model.Phase;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;


/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class BoardView extends BorderPane {

    private GameTemplate gameState;
    private final RoboRallyClient client;
    private BoardTemplate board;
    private static BoardTemplate staticBoard;

    private GridPane mainBoardPane;
    @Getter
    private SpaceView[][] spaces;
    private PlayersView playersView;
    private UpgradeShopView upgradeShopView;
    private final AppController appController;

    private Label statusLabel;

    private static Map<Integer, SpaceView> spaceViewMap = new HashMap<>();

    public BoardView(@NotNull AppController appController, @NotNull GameTemplate gameState, @NotNull RoboRallyClient client) {
        staticBoard = gameState.board;
        this.client = client;
        this.appController = appController;
        this.gameState = gameState;
        board = gameState.board;
        mainBoardPane = new GridPane();
        playersView = new PlayersView(appController, gameState);
        upgradeShopView = new UpgradeShopView(appController, gameState);
        statusLabel = new Label("<no status>");


//        this.getChildren().add(mainBoardPane);
//        this.getChildren().add(playersView);
//
//        this.getChildren().add(statusLabel);
//        this.getChildren().add(upgradeShopView);
        // Add mainBoardPane to the center of the BorderPane
        setCenter(mainBoardPane);

        // Add playersView to the right of the BorderPane
        setBottom(playersView);

        // Add upgradeShopView to the left of the BorderPane
        setRight(upgradeShopView);

        // Add statusLabel to the bottom of the BorderPane
        //setBottom(statusLabel);

        spaces = new SpaceView[board.height][board.width];


        for (int x = 0; x < board.width; x++) {
            for (int y = 0; y < board.height; y++) {
                SpaceTemplate space = board.spaces.get(x * board.height + y);
                SpaceView spaceView = new SpaceView(gameState, space);
                spaces[y][x] = spaceView;
                spaceViewMap.put(x+y*board.width, spaceView);
                mainBoardPane.add(spaceView, x, y);
            }
        }
    }

    public void updateView(GameTemplate gameState) {
        this.gameState = gameState;
        staticBoard = gameState.board;
        statusLabel.setText(getStatusMessage());
        playersView.updateView(gameState);
        upgradeShopView.updateView(gameState);
        for (int x = 0; x < board.width; x++) {
            for (int y = 0; y < board.height; y++) {
                spaces[y][x].updateView(gameState, gameState.board.spaces.get(x * board.height + y));
            }
        }
    }
    public static SpaceView getSpaceView(SpaceTemplate space) {
        return spaceViewMap.get(space.x + space.y*staticBoard.width);
    }

    public String getStatusMessage() {
        return "Phase: " + Phase.values()[gameState.playPhase].name() +
                ", Current Player = " + gameState.players.get(gameState.currentPlayer).name +
                ", Step: " + gameState.step;
    }
}
