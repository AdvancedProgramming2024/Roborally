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
import dtu.compute.RoborallyClient.fileaccess.model.PlayerTemplate;
import dtu.compute.RoborallyClient.model.Command;
import dtu.compute.RoborallyClient.model.Phase;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import org.jetbrains.annotations.NotNull;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class PlayerView extends Tab {

    final public static int NO_REGISTERS = 5;
    final public static int NO_CARDS = 8;
    final public static int NO_UPGRADE_CARDS = 3; // Both for temporary and permanent, so 3 for each
    private Label energyCubeLabel;
    private GridPane programPane;
    private Label checkPointLabel;
    private GridPane cardsPane;
    private GridPane permanentUpgradesPane;
    private GridPane temporaryUpgradesPane;

    private CardFieldView[] programCardViews;
    private CardFieldView[] cardViews;
    private UpgradeCardFieldView[] permanentUpgrades;
    private UpgradeCardFieldView[] temporaryUpgrades;

    private VBox buttonPanel;

    private Button finishButton;
    private Button skipUpgradeButton;

    private VBox playerInteractionPanel;

    private GameTemplate gameState;
    private PlayerTemplate player;
    private final AppController appController;

    public PlayerView(@NotNull AppController appController, @NotNull GameTemplate gameState, @NotNull PlayerTemplate player) {
        super(player.name);
        this.appController = appController;
        this.setStyle("-fx-text-base-color: " + player.color + ";");

        HBox top = new HBox();
        top.setPrefHeight(600);
        this.setContent(top);

        VBox cardsAndProgram = new VBox();
        cardsAndProgram.setPrefHeight(600);
        top.getChildren().add(cardsAndProgram);

        VBox info = new VBox();
        info.setPrefHeight(600);
        top.getChildren().add(info);

        VBox upgradeCards = new VBox();
        upgradeCards.setPrefHeight(600);
        top.getChildren().add(upgradeCards);

        this.gameState = gameState;
        this.player = player;

        Label programLabel = new Label("Program");

        programPane = new GridPane();
        programPane.setVgap(2.0);
        programPane.setHgap(2.0);

        checkPointLabel = new Label("Checkpoint\n" + player.checkpoints);
        energyCubeLabel = new Label("Energy Cubes\n" + player.energyBank);

        info.getChildren().add(checkPointLabel);
        info.getChildren().add(energyCubeLabel);

        programCardViews = new CardFieldView[NO_REGISTERS];
        for (int i = 0; i < NO_REGISTERS; i++) {
            programCardViews[i] = new CardFieldView(appController, gameState, player, i, true);
            programPane.add(programCardViews[i], i, 0);
        }


        // XXX  the following buttons should actually not be on the tabs of the individual
        //      players, but on the PlayersView (view for all players). This should be
        //      refactored.

        finishButton = new Button("Finish Programming");
        finishButton.setOnAction( e -> {
            if (appController.sendReadySignal()) {
                for (CardFieldView cardFieldView : programCardViews) {
                    // Deactivate events for cards, so they aren't moved after having finished programming
                    cardFieldView.setDisable(true);
                }
                for (CardFieldView cardFieldView : cardViews) {
                    cardFieldView.setDisable(true);
                }
            }
        });

        skipUpgradeButton = new Button("Don't buy an upgrade card");
        skipUpgradeButton.setOnAction( e -> {
            appController.buyUpgrade(-1);
        });

        buttonPanel = new VBox(skipUpgradeButton);
        buttonPanel.setAlignment(Pos.CENTER_LEFT);
        buttonPanel.setSpacing(3.0);
        // programPane.add(buttonPanel, NO_REGISTERS, 0); done in update now

        playerInteractionPanel = new VBox();
        playerInteractionPanel.setAlignment(Pos.CENTER_LEFT);
        playerInteractionPanel.setSpacing(3.0);

        Label cardsLabel = new Label("Command Cards");
        cardsPane = new GridPane();
        cardsPane.setVgap(2.0);
        cardsPane.setHgap(2.0);
        cardViews = new CardFieldView[NO_CARDS];
        for (int i = 0; i < NO_CARDS; i++) {
            cardViews[i] = new CardFieldView(appController, gameState, player, i, false);
            cardsPane.add(cardViews[i], i, 0);
        }

        Label permanentLabel = new Label("Permanent Upgrade Cards");
        permanentUpgradesPane = new GridPane();
        permanentUpgradesPane.setVgap(2.0);
        permanentUpgradesPane.setHgap(2.0);
        permanentUpgrades = new UpgradeCardFieldView[NO_UPGRADE_CARDS];
        for (int i = 0; i < NO_UPGRADE_CARDS; i++) {
            permanentUpgrades[i] = new UpgradeCardFieldView(appController, gameState, player, player.permanent[i], i, UpgradeCardFieldView.Placement.PERMANENT);
            permanentUpgradesPane.add(permanentUpgrades[i], i, 0);
        }

        Label temporaryLabel = new Label("Temporary Upgrade Cards");
        temporaryUpgradesPane = new GridPane();
        temporaryUpgradesPane.setVgap(2.0);
        temporaryUpgradesPane.setHgap(2.0);
        temporaryUpgrades = new UpgradeCardFieldView[NO_UPGRADE_CARDS];
        for (int i = 0; i < NO_UPGRADE_CARDS; i++) {
            temporaryUpgrades[i] = new UpgradeCardFieldView(appController, gameState, player, player.temporary[i], i, UpgradeCardFieldView.Placement.TEMPORARY);
            temporaryUpgradesPane.add(temporaryUpgrades[i], i, 0);
        }

        cardsAndProgram.getChildren().add(programLabel);
        cardsAndProgram.getChildren().add(programPane);
        cardsAndProgram.getChildren().add(cardsLabel);
        cardsAndProgram.getChildren().add(cardsPane);

        upgradeCards.getChildren().add(permanentLabel);
        upgradeCards.getChildren().add(permanentUpgradesPane);
        upgradeCards.getChildren().add(temporaryLabel);
        upgradeCards.getChildren().add(temporaryUpgradesPane);

    }

    public void updateView(GameTemplate gameState, int playerId) {
        this.gameState = gameState;
        this.player = gameState.players.get(playerId);
        checkPointLabel.setText("Checkpoint\n" + player.checkpoints);
        energyCubeLabel.setText("Energy Cubes\n" + player.energyBank);
        for (int i = 0; i < NO_REGISTERS; i++) {
            CardFieldView cardFieldView = programCardViews[i];
            if (cardFieldView != null) {
                if (gameState.playPhase != Phase.ACTIVATION.ordinal() && gameState.playPhase != Phase.PLAYER_INTERACTION.ordinal()) {
                    cardFieldView.setBackground(CardFieldView.BG_DEFAULT);
                } else {
                    if (i < gameState.step) {
                        cardFieldView.setBackground(CardFieldView.BG_DONE);
                    } else if (i == gameState.step) {
                        if (gameState.currentPlayer == player.id) {
                            cardFieldView.setBackground(CardFieldView.BG_ACTIVE);
                        } else if (gameState.playerOrder.indexOf(player.id) < gameState.playerOrder.indexOf(gameState.currentPlayer)) {
                            cardFieldView.setBackground(CardFieldView.BG_DONE);
                        } else {
                            cardFieldView.setBackground(CardFieldView.BG_DEFAULT);
                        }
                    } else {
                        cardFieldView.setBackground(CardFieldView.BG_DEFAULT);
                    }
                }
            }
        }

        if (gameState.playPhase == Phase.UPGRADE.ordinal()) {
            if (!buttonPanel.getChildren().contains(skipUpgradeButton)) {
                buttonPanel.getChildren().remove(finishButton);
                buttonPanel.getChildren().add(skipUpgradeButton);
            }
            String currentPlayerName = "";
            for (int i = 0; i < gameState.players.size(); i++) {
                if (gameState.players.get(i).id == gameState.currentPlayer) currentPlayerName = gameState.players.get(i).name;
            }

            // Disable button if it is another player's turn
            if (currentPlayerName.equals(appController.getRoboRally().getPlayerName())) {
                skipUpgradeButton.setDisable(false);
                skipUpgradeButton.setStyle("-fx-base: lightgreen");
            } else {
                skipUpgradeButton.setDisable(true);
                skipUpgradeButton.setStyle(null);
            }
        } else {
            if (!buttonPanel.getChildren().contains(finishButton)) {
                buttonPanel.getChildren().remove(skipUpgradeButton);
                buttonPanel.getChildren().add(finishButton);
            }
        }

        if (gameState.playPhase != Phase.PLAYER_INTERACTION.ordinal()) {
            if (!programPane.getChildren().contains(buttonPanel)) {
                programPane.getChildren().remove(playerInteractionPanel);
                programPane.add(buttonPanel, NO_REGISTERS, 0);
            }
            switch (Phase.values()[gameState.playPhase]) {
                case INITIALISATION:
                    finishButton.setDisable(true);
                    break;

                case PROGRAMMING:
                    finishButton.setDisable(false);
                    break;

                case ACTIVATION:
                    finishButton.setDisable(true);
                    break;

                default:
                    finishButton.setDisable(true);
            }
        } else {
            finishButton.setDisable(true);
            if (gameState.currentPlayer == player.id && appController.getRoboRally().getPlayerName().equals(player.name)) {
                if (!programPane.getChildren().contains(playerInteractionPanel)) {
                    programPane.getChildren().remove(buttonPanel);
                    programPane.add(playerInteractionPanel, NO_REGISTERS, 0);
                }
                playerInteractionPanel.getChildren().clear();

                Command command = Command.values()[gameState.currentCommand];
                for (Command option : command.getOptions()) {
                    Button optionButton = new Button(option.displayName);
                    optionButton.setOnAction(e -> appController.sendChoice(option));
                    optionButton.setDisable(false);
                    playerInteractionPanel.getChildren().add(optionButton);
                }
            }
        }
        for (CardFieldView cardFieldView : cardViews) {
            if (gameState.playPhase == Phase.ACTIVATION.ordinal()) {
                cardFieldView.setDisable(false); // Activate events again after programming phase
            }
            cardFieldView.updateView(gameState, playerId);
        }
        for (CardFieldView cardFieldView : programCardViews) {
            if (gameState.playPhase == Phase.ACTIVATION.ordinal()) {
                cardFieldView.setDisable(false); // Activate events again after programming phase
            }
            cardFieldView.updateView(gameState, playerId);
        }
        for (int i = 0; i < permanentUpgrades.length; i++) {
            permanentUpgrades[i].updateView(gameState, player, player.permanent[i]);
        }
        for (int i = 0; i < temporaryUpgrades.length; i++) {
            temporaryUpgrades[i].updateView(gameState, player, player.temporary[i]);
        }
    }
}
