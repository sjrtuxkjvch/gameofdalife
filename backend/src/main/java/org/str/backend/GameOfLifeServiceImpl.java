package org.str.backend;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameOfLifeServiceImpl implements GameOfLifeService {

    private static final Logger logger = LoggerFactory.getLogger(GameOfLifeServiceImpl.class);

    @Override
    public GameOfLifeState calculateNextState(GameOfLifeState state) {
        final int w = state.getWidth(), h = state.getHeight();
        final GameOfLifeState r = new GameOfLifeState(w, h);
        try {
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    // @formatter:off
                    // 1. Any live cell with fewer than two live neighbours dies, as if caused by underpopulation.
                    // 2. Any live cell with two or three live neighbours lives on to the next generation.
                    // 3. Any live cell with more than three live neighbours dies, as if by overpopulation.
                    // 4. Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
                    // @formatter:on
                    int liveNeightbourCount = countLiveNeighbours(state, x, y, w, h);
                    if (state.get(x, y)) {
                        // Live cell
                        switch (liveNeightbourCount) {
                        case 0:
                        case 1: {
                            // Rule 1
                            break;
                        }
                        case 2:
                        case 3: {
                            // Rule 2
                            r.set(x, y);
                            break;
                        }
                        default: {
                            // Rule 3
                        }
                        }
                    } else {
                        if (liveNeightbourCount == 3) {
                            // Rule 4
                            r.set(x, y);
                        }
                    }
                }
            }
            return r;
        } catch (IndexOutOfBoundsException e) {
            // inconsistent input data
            throw new RuntimeException("Inconsistent input data");
        }
    }

    private static int countLiveNeighbours(GameOfLifeState state, final int x, final int y, final int w, final int h) {
        int count = 0;
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if ((i >= 0) && (i < w) && (j >= 0) && (j < h)) {
                    if (!((i == x) && (j == y))) {
                        if (state.get(i, j)) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    @Override
    public GameOfLifeState getBuiltInState(String identifier) {
        if (identifier.matches("[a-zA-Z0-9]+")) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(identifier + ".lif")) {
                return new LifParser().parse(input);
            } catch (IOException e) {
                logger.error("Requested unknown built-in: {}", identifier);
                throw new RuntimeException(e);
            }
        } else {
            logger.error("Bad identifier");
            throw new RuntimeException("Invalid identifier");
        }
    }
}
