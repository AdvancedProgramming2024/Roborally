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
import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * ...
 *
 * @author Jamie (s236939)
 *
 */
public class UpgradeCardFieldView extends GridPane implements ViewObserver {

    final public static int CARDFIELD_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width/20;
    final public static int CARDFIELD_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height/15;

    final public static Border BORDER = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(2)));

    final public static Background BG_DEFAULT = new Background(new BackgroundFill(Color.WHITE, null, null));
    final public static Background BG_DRAG = new Background(new BackgroundFill(Color.GRAY, null, null));
    final public static Background BG_DROP = new Background(new BackgroundFill(Color.LIGHTGRAY, null, null));

    final public static Background BG_ACTIVE = new Background(new BackgroundFill(Color.YELLOW, null, null));
    final public static Background BG_DONE = new Background(new BackgroundFill(Color.GREENYELLOW, null, null));

    private UpgradeCardField field;

    private Label label;

    private GameController gameController;

    public UpgradeCardFieldView(@NotNull GameController gameController, @NotNull UpgradeCardField field) {
        this.gameController = gameController;
        this.field = field;

        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(2, 2, 2, 2));

        this.setBorder(BORDER);
        this.setBackground(BG_DEFAULT);

        this.setPrefWidth(CARDFIELD_WIDTH);
        this.setMinWidth(CARDFIELD_WIDTH);
        this.setMaxWidth(CARDFIELD_WIDTH);
        this.setPrefHeight(CARDFIELD_HEIGHT);
        this.setMinHeight(CARDFIELD_HEIGHT);
        this.setMaxHeight(CARDFIELD_HEIGHT);

        label = new Label("This is a slightly longer text");
        label.setWrapText(true);
        label.setMouseTransparent(true);
        label.setFont(Font.font("Arial", 9));
        this.add(label, 0, 0);


        this.setOnMouseClicked(event -> {
            if (field.getCard() == null || !field.isInShop()) {
                System.out.println("Can't buy");
                return;
            }
            // TODO: change this to phase UPGRADE
            if (gameController.board.getPhase() == Phase.PROGRAMMING) {
                showConfirmationDialog();
            } else {
                System.out.println("Clicking is not allowed during this phase.");
            }
        });

        field.attach(this);
        update(field);
    }

    private void showConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Purchase");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to purchase this upgrade?");

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.YES) {
                if (gameController.buyUpgrade(field)) {
                    System.out.println("Upgrade bought successfully");
                } else {
                    System.out.println("Couldn't buy upgrade");
                }
            }
        });
    }
    @Override
    public void updateView(Subject subject) {
        if (subject == field && subject != null) {
            UpgradeCard card = field.getCard();
            if (card != null && field.isVisible()) {
                StringBuilder labelText = new StringBuilder();
                labelText.append(field.getCard().getName()).append("\n");
                labelText.append("Cost: ").append(field.getCard().getCost()).append("\n");
                labelText.append(field.getCard().getIsPermanent() ? "Permanent" : "Temporary");
                label.setText(labelText.toString());
            } else {
                label.setText("");
            }
        }
    }
}

