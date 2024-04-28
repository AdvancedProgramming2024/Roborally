package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.Laser;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;


import java.util.ArrayList;
import java.util.List;

public class LaserView implements ViewObserver{

    public List<ImageView> laserImages = new ArrayList<>();
    final public static int SPACE_HEIGHT = SpaceView.SPACE_HEIGHT;
    final public static int SPACE_WIDTH = SpaceView.SPACE_WIDTH;

    public LaserView(Board board, SpaceView spaceView, int x, int y) {
        Space space = board.getSpace(x, y);
        for (FieldAction action : space.getActions()) {
            if (action instanceof Laser) {
                drawLaserPath(space, (Laser) action, spaceView);
            }
        }
    }

    private void drawLaserPath(Space space, Laser laser, SpaceView spaceView) {
        space.board.resetLOS();
        List<Space> LOS = space.board.getLOS(space, laser.getHeading());
        System.out.println(LOS.size());
        Image laserImage = new Image("images/laser.png");
        ImageView laserImageView = new ImageView();
        laserImageView.setCache(true);
        laserImageView.setFitHeight(SPACE_HEIGHT/8);
        laserImageView.setFitWidth((SPACE_WIDTH*LOS.size())-SPACE_WIDTH/3);
        // @TODO switch to determine last space in LOS should be included in length
        switch (laser.getHeading()) {
            case NORTH:
                laserImageView.setRotate(90);
                laserImageView.setTranslateY(-(SPACE_HEIGHT/1.5));
                break;
            case EAST:
                laserImageView.setRotate(0);
                laserImageView.setTranslateX(laserImageView.getFitWidth()/2-(SPACE_WIDTH/6));
                break;
            case SOUTH:
                laserImageView.setRotate(270);
                laserImageView.setTranslateY(laserImageView.getFitWidth()/2-(SPACE_HEIGHT/6));
                break;
            case WEST:
                laserImageView.setRotate(180);
                laserImageView.setTranslateX((-laserImageView.getFitWidth()/2)+SPACE_WIDTH/6);
                break;
        }
        laserImageView.setImage(laserImage);
        laserImages.add(laserImageView);

        spaceView.getChildren().add(laserImageView);
    }

    public void setLaserVisibility(boolean visible) {
        for (ImageView laserImage : laserImages) {
            laserImage.toFront();
            laserImage.setVisible(visible);
        }
    }

    @Override
    public void updateView(Subject subject) {

    }
}
