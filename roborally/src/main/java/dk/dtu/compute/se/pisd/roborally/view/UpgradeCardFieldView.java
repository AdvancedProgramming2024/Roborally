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

import com.google.gson.JsonObject;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.AppController;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.PlayerTemplate;
import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.online.RequestCenter;
import dk.dtu.compute.se.pisd.roborally.online.ResourceLocation;
import dk.dtu.compute.se.pisd.roborally.online.Response;
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
import java.io.IOException;

import static dk.dtu.compute.se.pisd.roborally.online.ResourceLocation.*;

/**
 * ...
 *
 * @author Jamie (s236939)
 *
 */
public class UpgradeCardFieldView extends GridPane {

    final public static int CARDFIELD_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width/20;
    final public static int CARDFIELD_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height/15;

    final public static Border BORDER = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(2)));

    final public static Background BG_DEFAULT = new Background(new BackgroundFill(Color.WHITE, null, null));
    final public static Background BG_DRAG = new Background(new BackgroundFill(Color.GRAY, null, null));
    final public static Background BG_DROP = new Background(new BackgroundFill(Color.LIGHTGRAY, null, null));

    final public static Background BG_ACTIVE = new Background(new BackgroundFill(Color.YELLOW, null, null));
    final public static Background BG_DONE = new Background(new BackgroundFill(Color.GREENYELLOW, null, null));

    private final int shopIndex;
    private Label label;
    private GameTemplate gameState;
    private final AppController appController;

    public UpgradeCardFieldView(@NotNull AppController appController, @NotNull GameTemplate gameState, int card, int shopIndex) {
        this.appController = appController;
        this.gameState = gameState;
        this.shopIndex = shopIndex;

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
            if (gameState.playPhase == Phase.UPGRADE.ordinal()) {
                showConfirmationDialog();
            } else {
                System.out.println("Buying is not allowed during this phase.");
            }
        });
        updateView(card);
    }

    private void showConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Purchase");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to purchase this upgrade?");

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.YES) {
                JsonObject info = new JsonObject();
                info.addProperty("shopIndex", shopIndex);

                String lobbyId = appController.getRoboRally().getLobbyId();
                int playerId = -1;
                for (PlayerTemplate player : gameState.players) {
                    if (player.name.equals(appController.getRoboRally().getPlayerName())) {
                        playerId = player.id;
                        break;
                    }
                }
                try {
                    Response<JsonObject> response = RequestCenter.postRequestJson(makeUri(buyUpgradePath(lobbyId, playerId)), info);
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        System.out.println("Couldn't buy upgrade");
                        Alert responseAlert = new Alert(Alert.AlertType.ERROR);
                        responseAlert.setTitle("Error");
                        responseAlert.setHeaderText(response.getItem().get("info").getAsString());
                        responseAlert.showAndWait();
                        return;
                    }
                    System.out.println("Upgrade bought successfully");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void updateView(int card) {
        if (card != -1) {
            StringBuilder labelText = new StringBuilder();
            labelText.append(Upgrade.values()[card].displayName).append("\n");
            labelText.append("Cost: ").append(Upgrade.values()[card].cost).append("\n");
            labelText.append(Upgrade.values()[card].isPermanent ? "Permanent" : "Temporary");
            label.setText(labelText.toString());
        } else {
            label.setText("");
        }
    }
}

