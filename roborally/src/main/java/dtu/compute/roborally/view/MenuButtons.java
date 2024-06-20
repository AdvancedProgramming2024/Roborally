package dtu.compute.roborally.view;
import dtu.compute.roborally.controller.AppController;

import javafx.scene.control.Button;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MenuButtons {
    private AppController appController;
    public Button newGameButton = new Button("New Game");
    public Button lobbyButton = new Button("Join a Game");
    public Button exitGameButton = new Button("Exit Game");
    public Button ruleButton = new Button("Rulebook");

    public MenuButtons(AppController appController) {
        this.appController = appController;
        //loadGameButton.setOnAction( e -> this.appController.loadGame());
        newGameButton.setOnAction(e -> this.appController.newLobby());

        lobbyButton.setOnAction(e -> {
            appController.showLobbies();
        });

        exitGameButton.setOnAction(e -> this.appController.exit());
        ruleButton.setOnAction(e -> {
            String url = "https://renegadegamestudios.com/content/File%20Storage%20for%20site/Rulebooks/Robo%20Rally/RoboRally_Rulebook_WEB.pdf";
            openWebPage(url);

        });
    }

    private void openWebPage(String url) {
        try {
            URI uri = new URI(url);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(uri);
            } else {
                System.out.println("Desktop is not supported on this platform.");
            }
        } catch (IOException | URISyntaxException ex) {
            System.out.println("Error occurred while trying to open the URL: " + ex.getMessage());
        }
    }

}