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

import dtu.compute.roborally.controller.AppController;
import dtu.compute.roborally.fileaccess.model.GameTemplate;
import dtu.compute.roborally.fileaccess.model.PlayerTemplate;
import dtu.compute.roborally.model.Phase;
import dtu.compute.roborally.model.Upgrade;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.control.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * ...
 *
 * @author Jamie (s236939)
 *
 */
public class UpgradeCardFieldView extends GridPane {

    public static enum Placement {
        SHOP, PERMANENT, TEMPORARY
    }

    final public static int CARDFIELD_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width/20;
    final public static int CARDFIELD_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height/15;

    final public static Border BORDER = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(2)));

    final public static Background BG_DEFAULT = new Background(new BackgroundFill(Color.WHITE, null, null));
    final public static Background BG_DRAG = new Background(new BackgroundFill(Color.GRAY, null, null));
    final public static Background BG_DROP = new Background(new BackgroundFill(Color.LIGHTGRAY, null, null));

    final public static Background BG_ACTIVE = new Background(new BackgroundFill(Color.YELLOW, null, null));
    final public static Background BG_DONE = new Background(new BackgroundFill(Color.GREENYELLOW, null, null));
    private Button discardButton;
    private final int index;
    private final Placement placement;
    private Label label;
    private Label activeLabel;
    private GameTemplate gameState;
    private PlayerTemplate player;
    private final AppController appController;

    public UpgradeCardFieldView(@NotNull AppController appController, @NotNull GameTemplate gameState, PlayerTemplate player, int card, int index, Placement placement) {
        this.appController = appController;
        this.gameState = gameState;
        this.index = index;
        this.placement = placement;
        this.player = player;

        this.setAlignment(Pos.TOP_CENTER);

        this.setBorder(BORDER);
        this.setBackground(BG_DEFAULT);

        this.setPrefWidth(CARDFIELD_WIDTH);
        this.setMinWidth(CARDFIELD_WIDTH);
        this.setMaxWidth(CARDFIELD_WIDTH);
        this.setPrefHeight(CARDFIELD_HEIGHT);
        this.setMinHeight(CARDFIELD_HEIGHT);
        this.setMaxHeight(CARDFIELD_HEIGHT);

        label = new Label("");
        label.setWrapText(true);
        label.setMouseTransparent(true);
        label.setFont(Font.font("Arial", 9));
        this.add(label, 0, 0);

        activeLabel = new Label("");
        activeLabel.setWrapText(true);
        activeLabel.setMouseTransparent(true);
        activeLabel.setFont(Font.font("Arial", 9));
        this.add(activeLabel, 0, 1);

        if (placement != Placement.SHOP) {
            HBox discardBox = new HBox();
            discardButton = new Button("X");
            discardButton.setStyle("-fx-base: red;");
            discardButton.setMaxSize(10, 10);
            discardBox.getChildren().add(discardButton);
            discardBox.setAlignment(Pos.TOP_RIGHT);
            this.add(discardBox, 1, 0);
            discardButton.setOnAction(event -> showConfirmationDialog(false));
        }

        this.setOnMouseClicked(new OnMouseClickedHandler());
        updateView(gameState, player, card);
    }

    private class OnMouseClickedHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            Object t = event.getTarget();
            if (t instanceof UpgradeCardFieldView source) {
                if (source.placement == Placement.SHOP) {
                    if (source.gameState.playPhase == Phase.UPGRADE.ordinal() && gameState.upgradeShop.get(index) != -1) {
                        showConfirmationDialog(true);
                    }
                } else {
                    if ((source.placement == Placement.PERMANENT ? source.player.permanent : source.player.temporary)[source.index] == -1) return;
                    if (source.player.name.equals(appController.getRoboRally().getPlayerName()) &&
                            (source.gameState.playPhase == Phase.UPGRADE.ordinal() || source.gameState.playPhase == Phase.PROGRAMMING.ordinal())) {
                        appController.toggleUpgrade(index, placement);
                    }
                }
            }
            event.consume();
        }
    }

    /**
     *
     * @param purchase true if it is for a purchase, false if it is for discarding
     */
    private void showConfirmationDialog(boolean purchase) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(purchase ? "Confirm Purchase" : "Confirm Discard");
        alert.setHeaderText(null);
        alert.setContentText(purchase ? "Do you want to purchase this upgrade?" : "Do you want to discard this upgrade?");

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.YES) {
                if (purchase) {
                    appController.buyUpgrade(index);
                } else {
                    appController.discardUpgrade(index, placement);
                }
            }
        });
    }

    public void updateView(GameTemplate gameState, PlayerTemplate player, int card) {
        this.player = player;
        this.gameState = gameState;
        if (card != -1) {
            StringBuilder labelText = new StringBuilder();
            labelText.append(Upgrade.values()[card].displayName).append("\n");
            labelText.append("Cost: ").append(Upgrade.values()[card].cost).append("\n");
            labelText.append(Upgrade.values()[card].isPermanent ? "Permanent" : "Temporary");
            label.setText(labelText.toString());
            if (placement != Placement.SHOP) {
                boolean active = placement == Placement.PERMANENT ? player.permanentActive[index] : player.temporaryActive[index];
                activeLabel.setText(active ? "Active" : "Inactive");
                activeLabel.setStyle(active ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                discardButton.setDisable(gameState.playPhase != Phase.UPGRADE.ordinal());
                discardButton.setVisible(gameState.playPhase == Phase.UPGRADE.ordinal());
            }
        } else {
            label.setText("");
            if (placement != Placement.SHOP) {
                activeLabel.setText("");
                discardButton.setDisable(false);
                discardButton.setVisible(false);
            }
        }
        if (placement != Placement.SHOP) {
            if (!player.name.equals(appController.getRoboRally().getPlayerName())) {
                discardButton.setDisable(false);
                discardButton.setVisible(false);
            }
        }
    }
}

