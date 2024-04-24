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
import dk.dtu.compute.se.pisd.roborally.controller.Checkpoint;
import dk.dtu.compute.se.pisd.roborally.controller.Gear;
import dk.dtu.compute.se.pisd.roborally.controller.Laser;
import dk.dtu.compute.se.pisd.roborally.controller.PushPanel;
import dk.dtu.compute.se.pisd.roborally.controller.ConveyorBelt;
import dk.dtu.compute.se.pisd.roborally.controller.EnergyCubeField;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import org.jetbrains.annotations.NotNull;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class SpaceView extends StackPane implements ViewObserver {

    final public static int SPACE_HEIGHT = 60; // 75;
    final public static int SPACE_WIDTH = 60; // 75;

    public final Space space;


    public SpaceView(@NotNull Space space) {
        this.space = space;

        // XXX the following styling should better be done with styles
        this.setPrefWidth(SPACE_WIDTH);
        this.setMinWidth(SPACE_WIDTH);
        this.setMaxWidth(SPACE_WIDTH);

        this.setPrefHeight(SPACE_HEIGHT);
        this.setMinHeight(SPACE_HEIGHT);
        this.setMaxHeight(SPACE_HEIGHT);

        drawBoard();

        // This space view should listen to changes of the space
        space.attach(this);
        update(space);
    }

    private void updatePlayer() {
        this.getChildren().removeIf(node -> node instanceof Polygon);

        Player player = space.getPlayer();
        if (player != null) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    10.0, 20.0,
                    20.0, 0.0 );
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90*player.getHeading().ordinal())%360);
            this.getChildren().add(arrow);
        }
    }

    @Override
    public void updateView(Subject subject) {
        if (subject == this.space) {
            updatePlayer();
        }
    }
    /**
     * @author Kresten (s235103)
     * @return pushPanel if the space contains one, null otherwise
     */
    private PushPanel getPushPanel() {
        if (space.getActions().isEmpty()) {
            return null;
        }
        for (FieldAction action : space.getActions()) {
            if (action instanceof PushPanel) {
                return (PushPanel) action;
            }
        }
        return null;
    }
    /**
     * @Description Draws the spaces and their content
     * @author Kresten (s235103)
     */
    private void drawBoard() {
        ImageView spaceImageView = new ImageView();
        Image spaceImage;
        spaceImageView.setFitHeight(SPACE_HEIGHT);
        spaceImageView.setFitWidth(SPACE_WIDTH);
        for (FieldAction action : space.getActions()) {
            if (action instanceof ConveyorBelt) {
                drawConveyorBelt(spaceImageView);
            } else if (action instanceof Checkpoint) {
                drawCheckpoint(spaceImageView, (Checkpoint) action);
            } else if (action instanceof EnergyCubeField) {
                drawEnergyField(spaceImageView, (EnergyCubeField) action);
            } else if (action instanceof Gear) {
                drawGear(spaceImageView, (Gear) action);
            }
        }
        if (space == space.board.getAntenna()) {
            spaceImage = new Image("images/antenna.png");
            spaceImageView.setImage(spaceImage);
        } else if (space == space.board.getRebootStation()) {
            spaceImage = new Image("images/reboot.png");
            spaceImageView.setImage(spaceImage);
        } else if (space.getActions().isEmpty() || getPushPanel() != null || getLaser() != null) {
            spaceImage = new Image("images/empty.png");
            spaceImageView.setImage(spaceImage);
        }
        this.getChildren().add(spaceImageView);

        if (getPushPanel() != null) {
            ImageView pushPanelImageView = createPushImageView();
            this.getChildren().add(pushPanelImageView);
        }

        if (getLaser() != null) {
            ImageView laserImageView = createLaserImageView();
            this.getChildren().add(laserImageView);
        }

        if (!space.getWalls().isEmpty()) {
            for (Heading wall : space.getWalls()) {
                ImageView wallImageView = new ImageView();
                Image wallImage = new Image("images/wall.png");
                wallImageView.setImage(wallImage);
                wallImageView.setFitHeight(SPACE_HEIGHT);
                wallImageView.setFitWidth(SPACE_HEIGHT/6);
                switch (wall) {
                    case NORTH:
                        wallImageView.setRotate(90);
                        wallImageView.setTranslateY((-SPACE_HEIGHT/2)+wallImageView.getFitWidth()/2);
                        break;
                    case SOUTH:
                        wallImageView.setRotate(90);
                        wallImageView.setTranslateY((SPACE_HEIGHT/2)-wallImageView.getFitWidth()/2);
                        break;
                    case EAST:
                        wallImageView.setTranslateX((SPACE_WIDTH/2)-wallImageView.getFitWidth()/2);
                        break;
                    case WEST:
                        wallImageView.setTranslateX((-SPACE_WIDTH/2)+wallImageView.getFitWidth()/2);
                        break;
                    default:
                        continue;
                }
                this.getChildren().add(wallImageView);
            }
        }
    }

    private void drawGear(ImageView spaceImageView, Gear gear) {
        Image spaceImage;
        if (gear.getHeading() == Heading.WEST) {
            spaceImage = new Image("images/gearLeft.png");
        } else {
            spaceImage = new Image("images/gearRight.png");
        }
        spaceImageView.setImage(spaceImage);
    }

    private void drawEnergyField(ImageView spaceImageView, EnergyCubeField energyCubeField) {
        Image spaceImage = new Image("images/energyField.png");
        spaceImageView.setImage(spaceImage);
        /*@TODO indicate if energy has been taken*/
    }

    private void drawCheckpoint(ImageView spaceImageView, Checkpoint checkpoint) {
        Image spaceImage = switch (checkpoint.getId()) {
            case 2 -> new Image("images/checkpoint2.png");
            case 3 -> new Image("images/checkpoint3.png");
            case 4 -> new Image("images/checkpoint4.png");
            case 5 -> new Image("images/checkpoint5.png");
            case 6 -> new Image("images/checkpoint6.png");
            default -> new Image("images/checkpoint1.png");
        };
        spaceImageView.setImage(spaceImage);
    }

    private void drawConveyorBelt(ImageView spaceImageView) {
        Image spaceImage = new Image("images/greenConveyor.png");
        spaceImageView.setImage(spaceImage);
        switch (((ConveyorBelt)space.getActions().get(0)).getHeading()) {
            case NORTH:
                spaceImageView.setRotate(0);
                break;
            case SOUTH:
                spaceImageView.setRotate(180);
                break;
            case EAST:
                spaceImageView.setRotate(90);
                break;
            case WEST:
                spaceImageView.setRotate(270);
                break;
        }
    }

    private ImageView createLaserImageView() {
        ImageView laserImageView = new ImageView();
        Image laserImage = new Image("images/laserStart.png");
        laserImageView.setImage(laserImage);
        laserImageView.setFitHeight(SPACE_HEIGHT/4);
        laserImageView.setPreserveRatio(true);
        switch (getLaser().getHeading()) {
            case NORTH:
                laserImageView.setRotate(270);
                laserImageView.setTranslateY(SPACE_HEIGHT/4.3);
                break;
            case SOUTH:
                laserImageView.setRotate(90);
                laserImageView.setTranslateY(-SPACE_HEIGHT/4.3);
                break;
            case EAST:
                laserImageView.setRotate(0);
                laserImageView.setTranslateX(-SPACE_WIDTH/4.3);
                break;
            case WEST:
                laserImageView.setRotate(180);
                laserImageView.setTranslateX(SPACE_WIDTH/4.3);
                break;
        }
        return laserImageView;
    }

    private Laser getLaser() {
        if (space.getActions().isEmpty()) {
            return null;
        }
        for (FieldAction action : space.getActions()) {
            if (action instanceof Laser) {
                return (Laser) action;
            }
        }
        return null;
    }

    /**
     * @author Kresten (s235103)
     * @return Placed ImageView of the pushPanel
     */
    @NotNull
    private ImageView createPushImageView() {
        ImageView pushPanelImageView = new ImageView();
        if (getPushPanel().getPushTime() == PushPanel.PushTime.EVEN) {
            Image pushPanelImage = new Image("images/pushEven.png");
            pushPanelImageView.setImage(pushPanelImage);
        } else {
            Image pushPanelImage = new Image("images/pushOdd.png");
            pushPanelImageView.setImage(pushPanelImage);
        }
        pushPanelImageView.setFitHeight(SPACE_HEIGHT);
        pushPanelImageView.setPreserveRatio(true);
        switch (getPushPanel().getHeading()) {
            case NORTH:
                pushPanelImageView.setRotate(270);
                pushPanelImageView.setTranslateY(SPACE_HEIGHT/4.3);
                break;
            case SOUTH:
                pushPanelImageView.setRotate(90);
                pushPanelImageView.setTranslateY(-SPACE_HEIGHT/4.3);
                break;
            case EAST:
                pushPanelImageView.setRotate(0);
                pushPanelImageView.setTranslateX(-SPACE_WIDTH/4.3);
                break;
            case WEST:
                pushPanelImageView.setRotate(180);
                pushPanelImageView.setTranslateX(SPACE_WIDTH/4.3);
                break;
        }
        return pushPanelImageView;
    }
}
