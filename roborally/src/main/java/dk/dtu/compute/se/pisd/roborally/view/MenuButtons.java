package dk.dtu.compute.se.pisd.roborally.view;
import dk.dtu.compute.se.pisd.roborally.controller.AppController;

import javafx.scene.control.Button;


public class MenuButtons{
    private AppController appController;
    public static Button newGameButton = new Button("New Game");
    public static Button loadGameButton = new Button("Load Game");
   public MenuButtons(AppController appController){
       this.appController = appController;
    loadGameButton.setOnAction( e -> this.appController.loadGame());
    newGameButton.setOnAction( e -> this.appController.newGame());
   }
}
