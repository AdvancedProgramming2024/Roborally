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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class UpgradeShopView extends VBox {

    private VBox top;
    private Label cardsLabel;
    private GridPane cardsPane;

    private UpgradeCardFieldView[] cardViews;
    private final AppController appController;


    public UpgradeShopView(@NotNull AppController appController, @NotNull GameTemplate gameState) {
        this.appController = appController;
        top = new VBox();
        top.setSpacing(10.0);
        getChildren().add(top);
        cardsLabel = new Label("Upgrade Shop");
        cardsLabel.setWrapText(true);
        cardsPane = new GridPane();
        cardsPane.setVgap(20.0);
        cardsPane.setHgap(2.0);
        cardsPane.setAlignment(Pos.CENTER);
        cardViews = new UpgradeCardFieldView[5];

        for (int i = 0; i < gameState.upgradeShop.size(); i++) {
            cardViews[i] = new UpgradeCardFieldView(appController, gameState, null, gameState.upgradeShop.get(i), i, UpgradeCardFieldView.Placement.SHOP);
            cardsPane.add(cardViews[i], 0, i);
        }

        top.setPrefWidth(120.0);
        top.getChildren().add(cardsLabel);
        top.getChildren().add(cardsPane);
        top.setAlignment(Pos.CENTER);
    }

    public void updateView(GameTemplate gameState) {
        for (int i = 0; i < gameState.upgradeShop.size(); i++) {
            cardViews[i].updateView(gameState, null, gameState.upgradeShop.get(i));
        }
    }
}

