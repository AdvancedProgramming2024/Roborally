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
package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.RoboRallyClient;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.BoardTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.SpaceTemplate;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Phase;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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

    private GridPane mainBoardPane;
    private SpaceView[][] spaces;
    private PlayersView playersView;
    private UpgradeShopView upgradeShopView;

    private Label statusLabel;

    private static Map<SpaceTemplate, SpaceView> spaceViewMap = new HashMap<>();

    public BoardView(@NotNull GameTemplate gameState, @NotNull RoboRallyClient client) {
        this.client = client;
        this.gameState = gameState;
        board = gameState.board;
        mainBoardPane = new GridPane();
        playersView = new PlayersView(gameState);
        upgradeShopView = new UpgradeShopView(gameState);
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
        setLeft(upgradeShopView);

        // Add statusLabel to the bottom of the BorderPane
        //setBottom(statusLabel);

        spaces = new SpaceView[board.height][board.width];


        for (int x = 0; x < board.width; x++) {
            for (int y = 0; y < board.height; y++) {
                SpaceTemplate space = board.spaces.get(x * board.height + y);
                SpaceView spaceView = new SpaceView(gameState, space);
                spaces[y][x] = spaceView;
                spaceViewMap.put(space, spaceView);
                mainBoardPane.add(spaceView, x, y);
            }
        }
    }

    public void updateView(GameTemplate gameState) {
        this.gameState = gameState;
        statusLabel.setText(getStatusMessage());
        playersView.updateView(gameState);
        upgradeShopView.updateView();
        for (SpaceView[] spaceRow : spaces) {
            for (SpaceView space : spaceRow) {
                space.updateView(gameState);
            }
        }
    }
    public static SpaceView getSpaceView(SpaceTemplate space) {
        return spaceViewMap.get(space);
    }

    public String getStatusMessage() {
        return "Phase: " + Phase.values()[gameState.playPhase].name() +
                ", Current Player = " + gameState.players.get(gameState.currentPlayer).name +
                ", Step: " + gameState.step;
    }
}
