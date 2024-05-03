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
package dk.dtu.compute.se.pisd.roborally.fileaccess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.BoardTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.GameTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.PlayerTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.SpaceTemplate;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.model.*;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class LoadSave {

    private static final String BOARDSFOLDER = "boards";
    private static final String GAMESFOLDER = "games";
    private static final String DEFAULTBOARD = "defaultboard";
    private static final String JSON_EXT = "json";

    public static Board loadBoard(String boardname) {
        if (boardname == null) {
            boardname = DEFAULTBOARD;
        }

        ClassLoader classLoader = LoadSave.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(BOARDSFOLDER + "/" + boardname + "." + JSON_EXT);
        if (inputStream == null) {
            // TODO these constants should be defined somewhere
            return new Board(8,8);
        }

		// In simple cases, we can create a Gson object with new Gson():
        GsonBuilder simpleBuilder = new GsonBuilder().
                registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>());
        Gson gson = simpleBuilder.create();

		Board result;
		// FileReader fileReader = null;
        JsonReader reader = null;
		try {
			// fileReader = new FileReader(filename);
			reader = gson.newJsonReader(new InputStreamReader(inputStream));
			BoardTemplate template = gson.fromJson(reader, BoardTemplate.class);

			result = new Board(template.width, template.height);

            result.setAntenna(template.antennaX, template.antennaY, Heading.values()[template.antennaHeading]);
            result.setRebootStation(template.rebootStationX, template.rebootStationY, Heading.values()[template.rebootStationHeading]);

			for (SpaceTemplate spaceTemplate: template.spaces) {
			    Space space = result.getSpace(spaceTemplate.x, spaceTemplate.y);
			    if (space != null) {
                    spaceTemplate.actions.forEach(space::addAction);
                    spaceTemplate.walls.forEach(space::addWall);
                }
            }
			reader.close();
			return result;
		} catch (IOException e1) {
            if (reader != null) {
                try {
                    reader.close();
                    inputStream = null;
                } catch (IOException e2) {}
            }
            if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e2) {}
			}
		}
		return null;
    }

    public static void saveBoard(Board board, String fileName) {
        BoardTemplate template = createBoardTemplate(board);

        ClassLoader classLoader = LoadSave.class.getClassLoader();
        // TODO: this is not very defensive, and will result in a NullPointerException
        //       when the folder "resources" does not exist! But, it does not need
        //       the file "simpleCards.json" to exist!
        String filename =
                classLoader.getResource(BOARDSFOLDER).getPath() + "/" + fileName + "." + JSON_EXT;

        writeToFile(template, filename);
    }

    public static GameController loadGameState(String fileName) {

        ClassLoader classLoader = LoadSave.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(GAMESFOLDER + "/" + fileName + "." + JSON_EXT);

        if (inputStream == null) {
            return null;
        }

        // In simple cases, we can create a Gson object with new Gson():
        GsonBuilder simpleBuilder = new GsonBuilder().
                registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>());
        Gson gson = simpleBuilder.create();

        // FileReader fileReader = null;
        JsonReader reader = null;
        try {
            // fileReader = new FileReader(filename);
            reader = gson.newJsonReader(new InputStreamReader(inputStream));
            GameTemplate template = gson.fromJson(reader, GameTemplate.class);

            Board board = new Board(template.board.width, template.board.height);
            GameController gameController = new GameController(board);

            board.setGameId(template.gameId);

            board.setAntenna(template.board.antennaX, template.board.antennaY, Heading.values()[template.board.antennaHeading]);
            board.setRebootStation(template.board.rebootStationX, template.board.rebootStationY, Heading.values()[template.board.rebootStationHeading]);

            for (SpaceTemplate spaceTemplate: template.board.spaces) {
                Space space = board.getSpace(spaceTemplate.x, spaceTemplate.y);
                if (space != null) {
                    space.getActions().addAll(spaceTemplate.actions);
                    space.getWalls().addAll(spaceTemplate.walls);
                }
            }

            for (PlayerTemplate playerTemplate: template.players) {
                Player player = new Player(board, playerTemplate.color, playerTemplate.name, playerTemplate.id);
                player.setSpace(board.getSpace(playerTemplate.xPosition, playerTemplate.yPosition));
                player.setHeading(Heading.values()[playerTemplate.heading]);

                for (int card : playerTemplate.drawPile) {
                    player.getDrawPile().add(new CommandCard(Command.values()[card]));
                }
                for (int card : playerTemplate.discardPile) {
                    player.getDiscardPile().add(new CommandCard(Command.values()[card]));
                }
                for (int i=0; i<playerTemplate.program.length; i++) {
                    player.getProgram()[i] = new CommandCardField(player);
                    if (playerTemplate.program[i] != -1)
                        player.getProgram()[i].setCard(new CommandCard(Command.values()[playerTemplate.program[i]]));
                }
                for (int i=0; i<playerTemplate.hand.length; i++) {
                    player.getCards()[i] = new CommandCardField(player);
                    if (playerTemplate.hand[i] != -1)
                        player.getCards()[i].setCard(new CommandCard(Command.values()[playerTemplate.hand[i]]));
                }

                player.setCheckpoints(playerTemplate.checkpoints);
                player.setEnergyCubes(playerTemplate.energyBank);
                player.setRebooting(playerTemplate.rebooting);
                board.addPlayer(player);
            }

            board.setCurrentPlayer(board.getPlayer(template.currentPlayer));
            List<Player> playerOrder = new ArrayList<>();
            for (int i : template.playerOrder) {
                playerOrder.add(board.getPlayer(i));
            }
            gameController.setPlayerOrder(playerOrder);
            board.setPhase(Phase.values()[template.playPhase]);
            board.setStep(template.step);

            reader.close();
            return gameController;
        } catch (IOException e1) {
            if (reader != null) {
                try {
                    reader.close();
                    inputStream = null;
                } catch (IOException e2) {}
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {}
            }
        }
        return null;
    }

    public static void saveGameState(GameController gameController, String fileName) {
        GameTemplate gameTemplate = new GameTemplate();
        gameTemplate.gameId = gameController.board.getGameId();
        gameTemplate.board = createBoardTemplate(gameController.board);

        for (int i = 0; i < gameController.board.getPlayersNumber(); i++) {
            PlayerTemplate playerTemplate = new PlayerTemplate();
            Player player = gameController.board.getPlayer(i);
            playerTemplate.id = player.getId();
            playerTemplate.name = player.getName();
            playerTemplate.color = player.getColor();
            playerTemplate.xPosition = player.getSpace().x;
            playerTemplate.yPosition = player.getSpace().y;
            playerTemplate.heading = player.getHeading().ordinal();


            for (CommandCard card : player.getDrawPile()) {
                playerTemplate.drawPile.add(card.command.ordinal());
            }
            for (CommandCard card : player.getDiscardPile()) {
                playerTemplate.discardPile.add(card.command.ordinal());
            }
            for (int j = 0; j < player.getProgram().length; j++) {
                CommandCardField field = player.getProgram()[j];
                playerTemplate.program[j] = field.getCard() == null ? -1 : field.getCard().command.ordinal();
            }
            for (int j = 0; j < player.getCards().length; j++) {
                CommandCardField field = player.getCards()[j];
                playerTemplate.hand[j] = field.getCard() == null ? -1 : field.getCard().command.ordinal();
            }

            playerTemplate.checkpoints = player.getCheckpoints();
            playerTemplate.energyBank = player.getEnergyCubes();
            playerTemplate.rebooting = player.isRebooting();

            gameTemplate.players.add(playerTemplate);
        }

        gameTemplate.currentPlayer = gameController.board.getCurrentPlayer().getId();

        gameController.getPlayerOrder().forEach(player -> gameTemplate.playerOrder.add(player.getId()));

        gameTemplate.playPhase = gameController.board.getPhase().ordinal();
        gameTemplate.step = gameController.board.getStep();



        ClassLoader classLoader = LoadSave.class.getClassLoader();
        // TODO: this is not very defensive, and will result in a NullPointerException
        //       when the folder "resources" does not exist! But, it does not need
        //       the file "simpleCards.json" to exist!

        URL url = classLoader.getResource(GAMESFOLDER);
        if (url == null) {
            File folder = new File(classLoader.getResource("").getPath() + "/" + GAMESFOLDER);
            if (!folder.exists()) {
                folder.mkdir(); // Create the folder if it doesn't exist
            }
        }
        assert url != null;

        String filename =
                url.getPath() + "/" + fileName + "." + JSON_EXT;

        writeToFile(gameTemplate, filename);
    }

    private static BoardTemplate createBoardTemplate(Board board) {
        BoardTemplate template = new BoardTemplate();
        template.width = board.width;
        template.height = board.height;

        template.antennaX = board.getAntenna().x;
        template.antennaY = board.getAntenna().y;
        template.antennaHeading = board.getAntennaHeading().ordinal();
        template.rebootStationX = board.getRebootStation().x;
        template.rebootStationY = board.getRebootStation().y;
        template.rebootStationHeading = board.getRebootStationHeading().ordinal();

        for (int i=0; i<board.width; i++) {
            for (int j=0; j<board.height; j++) {
                Space space = board.getSpace(i,j);
                if (!space.getWalls().isEmpty() || !space.getActions().isEmpty()) {
                    SpaceTemplate spaceTemplate = new SpaceTemplate();
                    spaceTemplate.x = space.x;
                    spaceTemplate.y = space.y;
                    spaceTemplate.actions.addAll(space.getActions());
                    spaceTemplate.walls.addAll(space.getWalls());
                    template.spaces.add(spaceTemplate);
                }
            }
        }
        return template;
    }

    public static void writeToFile(Object template, String filename) {
        // In simple cases, we can create a Gson object with new:
        //
        //   Gson gson = new Gson();
        //
        // But, if you need to configure it, it is better to create it from
        // a builder (here, we want to configure the JSON serialisation with
        // a pretty printer):
        GsonBuilder simpleBuilder = new GsonBuilder().
                registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>()).
                setPrettyPrinting();
        Gson gson = simpleBuilder.create();

        FileWriter fileWriter = null;
        JsonWriter writer = null;
        try {
            fileWriter = new FileWriter(filename);
            writer = gson.newJsonWriter(fileWriter);
            gson.toJson(template, template.getClass(), writer);
            writer.close();
        } catch (IOException e1) {
            if (writer != null) {
                try {
                    writer.close();
                    fileWriter = null;
                } catch (IOException e2) {}
            }
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e2) {}
            }
        }
    }
}