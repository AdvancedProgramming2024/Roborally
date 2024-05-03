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
import dk.dtu.compute.se.pisd.roborally.model.CommandCard;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import javafx.scene.Node;
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
import javafx.stage.Screen;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static dk.dtu.compute.se.pisd.roborally.model.Command.SPAM;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class SpaceView extends StackPane implements ViewObserver {

    final public static int SPACE_SIZE = Toolkit.getDefaultToolkit().getScreenSize().height/20; // 75;

    public final Space space;

    public static List<ImageView> lasers = new ArrayList<>();

    public ImageView eneryCubeImageView;

    public SpaceView(@NotNull Space space) {
        this.space = space;

        // XXX the following styling should better be done with styles
        this.setPrefWidth(SPACE_SIZE);
        this.setMinWidth(SPACE_SIZE);
        this.setMaxWidth(SPACE_SIZE);

        this.setPrefHeight(SPACE_SIZE);
        this.setMinHeight(SPACE_SIZE);
        this.setMaxHeight(SPACE_SIZE);

        drawBoard();

        // This space view should listen to changes of the space
        space.attach(this);
        update(space);
    }
    /**
     * @author Kresten (s235103)
     * @Description Draws the lasers on the board for a given time
     * @param LOS List of spaces the laser passes through
     */
    public static void drawLaser(List<Space> LOS) {
        Heading heading = null;
        Laser laser;
        for (FieldAction action : LOS.get(0).getActions()) {
            if (action instanceof Laser) {
                laser = (Laser) action;
                heading = laser.getHeading();
                break;
            }
        }

        // Draw the laser on the Space which contains the laserStart
        Image laserImage = new Image("images/laser.png");
        ImageView laserImageView = new ImageView();
        laserImageView.setCache(true);
        laserImageView.setImage(laserImage);
        laserImageView.setFitHeight(SPACE_SIZE/8);
        laserImageView.setFitWidth(SPACE_SIZE-(SPACE_SIZE/3));
        int rotation = 0;
        switch (heading) {
            case NORTH:
                rotation= 90;
                laserImageView.setTranslateY(-laserImageView.getFitWidth()/2+(SPACE_SIZE/6));
                break;
            case EAST:
                laserImageView.setTranslateX(laserImageView.getFitWidth()/2-(SPACE_SIZE/6));
                break;
            case SOUTH:
                rotation= 270;
                laserImageView.setTranslateY(laserImageView.getFitWidth()/2-(SPACE_SIZE/6));
                break;
            case WEST:
                rotation= 180;
                laserImageView.setTranslateX((-laserImageView.getFitWidth()/2)+SPACE_SIZE/6);
                break;
        }
        laserImageView.setRotate(rotation);
        lasers.add(laserImageView);
        BoardView.getSpaceView(LOS.get(0)).getChildren().add(laserImageView);

        // Draw the laser on the Spaces the laser passes through
        for (int i = 1 ; i < LOS.size()-1 ; i++) {
            ImageView laserImageView2 = new ImageView();
            laserImageView2.setCache(true);
            laserImageView2.setImage(laserImage);
            laserImageView2.setFitHeight(SPACE_SIZE/8);
            laserImageView2.setFitWidth(SPACE_SIZE);
            laserImageView2.setRotate(rotation);
            lasers.add(laserImageView2);
            BoardView.getSpaceView(LOS.get(i)).getChildren().add(laserImageView2);
        }
        // Determine if and how the last laser should be drawn
        Space hit = LOS.get(LOS.size() -1);
        Heading reverse = heading.next().next();

        if (hit.getWalls().contains(reverse)) {
            return;
        }

        if (hit.getPlayer() != null) {
            // Draw half-length laser
            ImageView laserImageView3 = new ImageView();
            laserImageView3.setCache(true);
            laserImageView3.setImage(laserImage);
            laserImageView3.setFitHeight(SPACE_SIZE/8);
            laserImageView3.setFitWidth(SPACE_SIZE/2);
            switch (heading) {
                case NORTH -> laserImageView3.setTranslateY(SPACE_SIZE/4);
                case EAST -> laserImageView3.setTranslateX(-SPACE_SIZE/4);
                case SOUTH -> laserImageView3.setTranslateY(-SPACE_SIZE/4);
                case WEST -> laserImageView3.setTranslateX(SPACE_SIZE/4);
            }
            laserImageView3.setRotate(rotation);
            lasers.add(laserImageView3);
            BoardView.getSpaceView(hit).getChildren().add(laserImageView3);
        } else {
            // Draw full length laser
            ImageView laserImageView4 = new ImageView();
            laserImageView4.setCache(true);
            laserImageView4.setImage(laserImage);
            laserImageView4.setFitHeight(SPACE_SIZE/8);
            laserImageView4.setFitWidth(SPACE_SIZE);
            laserImageView4.setRotate(rotation);
            lasers.add(laserImageView4);
            BoardView.getSpaceView(hit).getChildren().add(laserImageView4);
        }
    }

    public static void destroyLasers() {
        for (ImageView laser : lasers) {
            laser.setImage(null);
        }
        lasers.clear();
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
        spaceImageView.setFitHeight(SPACE_SIZE);
        spaceImageView.setFitWidth(SPACE_SIZE);
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
                wallImageView.setFitHeight(SPACE_SIZE);
                wallImageView.setFitWidth(SPACE_SIZE/6);
                switch (wall) {
                    case NORTH:
                        wallImageView.setRotate(90);
                        wallImageView.setTranslateY((-SPACE_SIZE/2)+wallImageView.getFitWidth()/2);
                        break;
                    case SOUTH:
                        wallImageView.setRotate(90);
                        wallImageView.setTranslateY((SPACE_SIZE/2)-wallImageView.getFitWidth()/2);
                        break;
                    case EAST:
                        wallImageView.setTranslateX((SPACE_SIZE/2)-wallImageView.getFitWidth()/2);
                        break;
                    case WEST:
                        wallImageView.setTranslateX((-SPACE_SIZE/2)+wallImageView.getFitWidth()/2);
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
        spaceImageView.setViewOrder(1);
        if (energyCubeField.getEnergyCubes() == 0) {
            return;
        }
        Image energyCubeImage = new Image("images/energyCube.png");
        ImageView energyCubeImageView = new ImageView();
        energyCubeImageView.setImage(energyCubeImage);
        energyCubeImageView.setFitHeight(SPACE_SIZE/1.2);
        energyCubeImageView.setFitWidth(SPACE_SIZE/1.2);
        this.eneryCubeImageView = energyCubeImageView;
        this.getChildren().add(energyCubeImageView);
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
        Image laserImage;
        if (getLaser().getLazer() == 2) {
            laserImage = new Image("images/doublelaser.png");
            laserImageView.setFitHeight(SPACE_SIZE/2.0);
        } else if (getLaser().getLazer() > 2) {
            laserImage = new Image("images/triplelaser.png");
            laserImageView.setFitHeight(SPACE_SIZE/1.2);
        } else {
            laserImage = new Image("images/laserStart.png");
            laserImageView.setFitHeight(SPACE_SIZE/4.0);
        }

        laserImageView.setPreserveRatio(true);
        laserImageView.setImage(laserImage);

        switch (getLaser().getHeading()) {
            case NORTH:
                laserImageView.setRotate(270);
                laserImageView.setTranslateY(SPACE_SIZE/4.3);
                break;
            case SOUTH:
                laserImageView.setRotate(90);
                laserImageView.setTranslateY(-SPACE_SIZE/4.3);
                break;
            case EAST:
                laserImageView.setRotate(0);
                laserImageView.setTranslateX(-SPACE_SIZE/4.3);
                break;
            case WEST:
                laserImageView.setRotate(180);
                laserImageView.setTranslateX(SPACE_SIZE/4.3);
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
        pushPanelImageView.setFitHeight(SPACE_SIZE);
        pushPanelImageView.setPreserveRatio(true);
        switch (getPushPanel().getHeading()) {
            case NORTH:
                pushPanelImageView.setRotate(270);
                pushPanelImageView.setTranslateY(SPACE_SIZE/4.3);
                break;
            case SOUTH:
                pushPanelImageView.setRotate(90);
                pushPanelImageView.setTranslateY(-SPACE_SIZE/4.3);
                break;
            case EAST:
                pushPanelImageView.setRotate(0);
                pushPanelImageView.setTranslateX(-SPACE_SIZE/4.3);
                break;
            case WEST:
                pushPanelImageView.setRotate(180);
                pushPanelImageView.setTranslateX(SPACE_SIZE/4.3);
                break;
        }
        return pushPanelImageView;
    }
}
