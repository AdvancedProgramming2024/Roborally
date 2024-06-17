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
import dk.dtu.compute.se.pisd.roborally.controller.AppController;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.PlayerTemplate;
import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.html.Option;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class PlayerView extends Tab {

    private VBox top;

    private Label programLabel;
    private Label energyCubeLabel;
    private GridPane programPane;
    private Label cardsLabel;
    private Label checkPointLabel;
    private GridPane cardsPane;

    private CardFieldView[] programCardViews;
    private CardFieldView[] cardViews;

    private VBox buttonPanel;

    private Button finishButton;

    private VBox playerInteractionPanel;

    private GameTemplate gameState;
    private PlayerTemplate player;
    private final AppController appController;

    public PlayerView(@NotNull AppController appController, @NotNull GameTemplate gameState, @NotNull PlayerTemplate player) {
        super(player.name);
        this.appController = appController;
        this.setStyle("-fx-text-base-color: " + player.color + ";");

        top = new VBox();
        top.setPrefHeight(600);
        this.setContent(top);

        this.gameState = gameState;
        this.player = player;

        programLabel = new Label("Program");

        programPane = new GridPane();
        programPane.setVgap(2.0);
        programPane.setHgap(2.0);

        checkPointLabel = new Label("Checkpoint\n" + player.checkpoints);
        energyCubeLabel = new Label("Energy Cubes\n" + player.energyBank);

        programPane.add(checkPointLabel, Player.NO_REGISTERS+1, 0);
        programPane.add(energyCubeLabel, Player.NO_REGISTERS+1, 1);

        programCardViews = new CardFieldView[Player.NO_REGISTERS];
        for (int i = 0; i < Player.NO_REGISTERS; i++) {
            programCardViews[i] = new CardFieldView(appController, gameState, player, i, true);
            programPane.add(programCardViews[i], i, 0);
        }


        // XXX  the following buttons should actually not be on the tabs of the individual
        //      players, but on the PlayersView (view for all players). This should be
        //      refactored.

        finishButton = new Button("Finish Programming");
        finishButton.setOnAction( e -> {
            if (appController.sendReadySignal()) {
                appController.getRoboRally().startPolling();
                for (CardFieldView cardFieldView : programCardViews) {
                    // Deactivate events for cards, so they aren't moved after having finished programming
                    cardFieldView.setDisable(true);
                }
                for (CardFieldView cardFieldView : cardViews) {
                    cardFieldView.setDisable(true);
                }
            }
        });

        buttonPanel = new VBox(finishButton);
        buttonPanel.setAlignment(Pos.CENTER_LEFT);
        buttonPanel.setSpacing(3.0);
        // programPane.add(buttonPanel, Player.NO_REGISTERS, 0); done in update now

        playerInteractionPanel = new VBox();
        playerInteractionPanel.setAlignment(Pos.CENTER_LEFT);
        playerInteractionPanel.setSpacing(3.0);

        cardsLabel = new Label("Command Cards");
        cardsPane = new GridPane();
        cardsPane.setVgap(2.0);
        cardsPane.setHgap(2.0);
        cardViews = new CardFieldView[Player.NO_CARDS];
        for (int i = 0; i < Player.NO_CARDS; i++) {
            cardViews[i] = new CardFieldView(appController, gameState, player, i, false);
            cardsPane.add(cardViews[i], i, 0);
        }

        top.getChildren().add(programLabel);
        top.getChildren().add(programPane);
        top.getChildren().add(cardsLabel);
        top.getChildren().add(cardsPane);
    }

    public void updateView(GameTemplate gameState, int playerId) {
        this.gameState = gameState;
        this.player = gameState.players.get(playerId);
        checkPointLabel.setText("Checkpoint\n" + player.checkpoints);
        energyCubeLabel.setText("Energy Cubes\n" + player.energyBank);
        for (int i = 0; i < Player.NO_REGISTERS; i++) {
            CardFieldView cardFieldView = programCardViews[i];
            if (cardFieldView != null) {
                if (gameState.playPhase == Phase.PROGRAMMING.ordinal()) {
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

        if (gameState.playPhase != Phase.PLAYER_INTERACTION.ordinal()) {
            if (!programPane.getChildren().contains(buttonPanel)) {
                programPane.getChildren().remove(playerInteractionPanel);
                programPane.add(buttonPanel, Player.NO_REGISTERS, 0);
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
                    programPane.add(playerInteractionPanel, Player.NO_REGISTERS, 0);
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
    }
}
