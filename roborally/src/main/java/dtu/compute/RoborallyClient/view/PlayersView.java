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
package dtu.compute.RoborallyClient.view;

import dtu.compute.RoborallyClient.controller.AppController;
import dtu.compute.RoborallyClient.fileaccess.model.GameTemplate;
import dtu.compute.RoborallyClient.model.Phase;
import javafx.scene.control.TabPane;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class PlayersView extends TabPane {

    private GameTemplate gameState;

    private PlayerView[] playerViews;
    private final AppController appController;

    public PlayersView(AppController appController, GameTemplate gameState) {
        this.appController = appController;
        this.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        int ownIndex = 0;
        playerViews = new PlayerView[gameState.players.size()];
        for (int i = 0; i < gameState.players.size();  i++) {
            playerViews[i] = new PlayerView(appController, gameState, gameState.players.get(i));
            this.getTabs().add(playerViews[i]);
            if (gameState.players.get(i).name.equals(appController.getClient().getPlayerName())) ownIndex = i;
        }
        this.getSelectionModel().select(ownIndex);
    }

    public void updateView(GameTemplate gameState) {
        this.gameState = gameState;
        if (gameState.playPhase == Phase.ACTIVATION.ordinal()) {
            this.getSelectionModel().select(gameState.currentPlayer);
        }

        for (int i = 0; i < playerViews.length; i++) {
            playerViews[i].updateView(gameState, i);
        }
    }
}
