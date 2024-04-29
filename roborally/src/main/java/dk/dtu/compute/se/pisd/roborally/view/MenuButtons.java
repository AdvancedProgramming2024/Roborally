package dk.dtu.compute.se.pisd.roborally.view;
import dk.dtu.compute.se.pisd.roborally.controller.AppController;

import javafx.scene.control.Button;


public class MenuButtons{
    private AppController appController;
    public static Button newGameButton = new Button("New Game");
    public static Button loadGameButton = new Button("Load Game");
    public static Button exitGameButton = new Button("Exit Game");
    public static Button tutorialButton = new Button("Tutorial");
   public MenuButtons(AppController appController){
       this.appController = appController;
    loadGameButton.setOnAction( e -> this.appController.loadGame());
    newGameButton.setOnAction( e -> this.appController.newGame());
    exitGameButton.setOnAction( e -> this.appController.exit());
    //tutorialButton.setOnAction( e -> download link til reglerne eller sådan noget);
    }
   }