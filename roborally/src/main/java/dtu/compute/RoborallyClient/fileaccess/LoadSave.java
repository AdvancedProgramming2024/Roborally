package dtu.compute.RoborallyClient.fileaccess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dtu.compute.RoborallyClient.fileaccess.model.GameTemplate;
import dtu.compute.RoborallyClient.fields.FieldAction;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;

public class LoadSave {
    private static final String JSON_EXT = "json";
    public static final String GAMESFOLDER = "games";

    /**
     * Get the file path for the given file name and resource folder. If the folder does not exist, it is created.
     * @author Jonathan (s235115)
     * @param fileName  File name
     * @param rFolder   Resource folder
     * @return File path
     */
    @NotNull
    public static String getFilePath(String fileName, String rFolder) {
        ClassLoader classLoader = LoadSave.class.getClassLoader();

        URL url = classLoader.getResource(rFolder);
        if (url == null) {
            File folder = new File(classLoader.getResource("").getPath() + "/" + GAMESFOLDER);
            if (!folder.exists()) {
                folder.mkdir(); // Create the folder if it doesn't exist
            }
        }
        url = classLoader.getResource(rFolder);

        String filename =
                url.getPath() + "/" + fileName + "." + JSON_EXT;
        return filename;
    }

    public static GameTemplate readGameStateFromFile(String fileName) {
        ClassLoader classLoader = LoadSave.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(GAMESFOLDER + "/" + fileName + "." + JSON_EXT);

        if (inputStream == null) {
            return null;
        }

        // In simple cases, we can create a Gson object with new Gson():
        GsonBuilder simpleBuilder = new GsonBuilder().
                registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>());
        Gson gson = simpleBuilder.create();

        JsonReader reader = null;

        try {
            reader = gson.newJsonReader(new InputStreamReader(inputStream));
            GameTemplate gameState = gson.fromJson(reader, GameTemplate.class);
            reader.close();
            return gameState;
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

    public static void writeToFile(Object template, String filename) {
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
            fileWriter.close();
            writer = null;
            fileWriter = null;
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
