package com.lifedefense.utils;

import com.google.gson.*;
import com.lifedefense.core.GridManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Serialization/deserialization utilities for saving and loading grid states.
 */
public class SerializationUtils {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Save grid state to JSON file.
     */
    public static void saveGrid(GridManager grid, String filename) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("width", grid.getWidth());
        json.addProperty("height", grid.getHeight());
        json.addProperty("generation", grid.getGenerationCount());
        json.addProperty("livingCells", grid.getLivingCellCount());

        JsonArray cellsArray = new JsonArray();
        for (Vector2i cell : grid.getAllLivingCells()) {
            JsonObject cellObj = new JsonObject();
            cellObj.addProperty("x", cell.x());
            cellObj.addProperty("y", cell.y());
            cellsArray.add(cellObj);
        }
        json.add("cells", cellsArray);

        String jsonString = GSON.toJson(json);
        Files.writeString(Path.of(filename), jsonString);
    }

    /**
     * Load grid state from JSON file.
     */
    public static void loadGrid(GridManager grid, String filename) throws IOException {
        String jsonString = Files.readString(Path.of(filename));
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        int width = json.get("width").getAsInt();
        int height = json.get("height").getAsInt();

        if (width != grid.getWidth() || height != grid.getHeight()) {
            throw new IOException("Grid dimensions don't match: " +
                    width + "x" + height + " vs " +
                    grid.getWidth() + "x" + grid.getHeight());
        }

        grid.clear();

        JsonArray cellsArray = json.getAsJsonArray("cells");
        for (JsonElement element : cellsArray) {
            JsonObject cellObj = element.getAsJsonObject();
            int x = cellObj.get("x").getAsInt();
            int y = cellObj.get("y").getAsInt();
            grid.setCell(x, y, true);
        }
    }

    /**
     * Save a list of patterns (named designs).
     */
    public static void savePattern(String name, List<Vector2i> cells, String filename) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);

        JsonArray cellsArray = new JsonArray();
        for (Vector2i cell : cells) {
            JsonObject cellObj = new JsonObject();
            cellObj.addProperty("x", cell.x());
            cellObj.addProperty("y", cell.y());
            cellsArray.add(cellObj);
        }
        json.add("cells", cellsArray);

        String jsonString = GSON.toJson(json);
        Files.writeString(Path.of(filename), jsonString);
    }

    /**
     * Load a pattern from file.
     */
    public static List<Vector2i> loadPattern(String filename) throws IOException {
        String jsonString = Files.readString(Path.of(filename));
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        JsonArray cellsArray = json.getAsJsonArray("cells");
        List<Vector2i> cells = new java.util.ArrayList<>();

        for (JsonElement element : cellsArray) {
            JsonObject cellObj = element.getAsJsonObject();
            int x = cellObj.get("x").getAsInt();
            int y = cellObj.get("y").getAsInt();
            cells.add(new Vector2i(x, y));
        }

        return cells;
    }
}
