package org.str.backend;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("applicationDescription")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameOfLifeState {

    /**
     * First index is Y, second index is X
     */
    @JsonProperty("array")
    private boolean[][] array;
    @JsonProperty("height")
    private int width;
    @JsonProperty("width")
    private int height;

    public GameOfLifeState() {
    }

    public GameOfLifeState(int x, int y) {
        width = x;
        height = y;
        array = new boolean[y][];
        for (int i = 0; i < y; i++) {
            array[i] = new boolean[x];
        }
    }

    public boolean get(int x, int y) {
        return array[y][x];
    }

    public void set(int x, int y, boolean value) {
        array[y][x] = value;
    }

    public void set(int x, int y) {
        array[y][x] = true;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
