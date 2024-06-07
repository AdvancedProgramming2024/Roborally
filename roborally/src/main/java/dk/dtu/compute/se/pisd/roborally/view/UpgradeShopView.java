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
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class UpgradeShopView extends VBox implements ViewObserver {

    private VBox top;
    private Label cardsLabel;
    private GridPane cardsPane;

    private UpgradeCardFieldView[] cardViews;


    public UpgradeShopView(@NotNull GameTemplate gameState) {
        top = new VBox();
        top.setSpacing(10.0);
        getChildren().add(top);
        cardsLabel = new Label("Upgrade Shop (upgrade cards aren't implemented yet, so only visual)");
        cardsLabel.setWrapText(true);
        cardsPane = new GridPane();
        cardsPane.setVgap(20.0);
        cardsPane.setHgap(2.0);
        cardsPane.setAlignment(Pos.CENTER);
        cardViews = new UpgradeCardFieldView[5];

        Upgrade[] upgrades = Upgrade.values();
        ArrayList<UpgradeCard> upgradePile = new ArrayList<>();
        int[] upgradeValues = {0,1,2,3,4};
        for (int upgradeValue : upgradeValues) {
            upgradePile.add(new UpgradeCard(upgrades[upgradeValue]));
        }

        for (int i = 0; i < upgradePile.size(); i++) {

            UpgradeCardField cardField = new UpgradeCardField();
            cardField.setCard(upgradePile.get(i));

            cardViews[i] = new UpgradeCardFieldView(gameState, cardField);
            cardsPane.add(cardViews[i], 0, i);

        }

        top.setPrefWidth(120.0);
        top.getChildren().add(cardsLabel);
        top.getChildren().add(cardsPane);
        top.setAlignment(Pos.CENTER);
    }

    @Override
    public void updateView(Subject subject) {
    }
    }

