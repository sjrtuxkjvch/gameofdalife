package backend;

import org.junit.Assert;
import org.junit.Test;
import org.str.backend.GameOfLifeService;
import org.str.backend.GameOfLifeServiceImpl;
import org.str.backend.GameOfLifeState;

public class GameOfLifeServiceTest {

    private GameOfLifeService underTest = new GameOfLifeServiceImpl();

    @Test
    public void testRule1a() {
        // GIVEN
        GameOfLifeState in = new GameOfLifeState(3, 3);
        // 1,1 has no live neighbours
        in.set(1, 1);
        // WHEN
        GameOfLifeState out = underTest.calculateNextState(in);
        // THEN
        Assert.assertFalse(out.get(1, 1));
    }

    @Test
    public void testRule1b() {
        // GIVEN
        GameOfLifeState in = new GameOfLifeState(3, 3);
        // 1,1 and 1,2 have one live neighbour
        in.set(1, 1);
        in.set(1, 2);
        // WHEN
        GameOfLifeState out = underTest.calculateNextState(in);
        // THEN
        Assert.assertFalse(out.get(1, 1));
        Assert.assertFalse(out.get(1, 2));
    }

    @Test
    public void testRule2a() {
        // GIVEN
        GameOfLifeState in = new GameOfLifeState(3, 3);
        // 2,2 has two live neighbours
        in.set(1, 2);
        in.set(2, 1);
        in.set(2, 2);
        // WHEN
        GameOfLifeState out = underTest.calculateNextState(in);
        // THEN
        Assert.assertTrue(out.get(2, 2));
    }

    @Test
    public void testRule2b() {
        // GIVEN
        GameOfLifeState in = new GameOfLifeState(3, 3);
        // 2,2 has 3 live neighbours
        in.set(1, 1);
        in.set(1, 2);
        in.set(2, 1);
        in.set(2, 2);
        // WHEN
        GameOfLifeState out = underTest.calculateNextState(in);
        // THEN
        Assert.assertTrue(out.get(2, 2));
    }

    @Test
    public void testRule3() {
        // GIVEN
        GameOfLifeState in = new GameOfLifeState(4, 4);
        // 2,2 has 4 live neighbours
        in.set(1, 1);
        in.set(1, 2);
        in.set(2, 1);
        in.set(2, 2);
        in.set(3, 2);
        // WHEN
        GameOfLifeState out = underTest.calculateNextState(in);
        // THEN
        Assert.assertFalse(out.get(2, 2));
    }

    @Test
    public void testRule4() {
        // GIVEN
        GameOfLifeState in = new GameOfLifeState(3, 3);
        // 1,1 has 3 live neighbours
        in.set(1, 2);
        in.set(2, 1);
        in.set(2, 2);
        // WHEN
        GameOfLifeState out = underTest.calculateNextState(in);
        // THEN
        Assert.assertTrue(out.get(1, 1));
    }

    @Test
    public void testBuiltins() {
        // WHEN
        GameOfLifeState out = underTest.getBuiltInState("breeder2");
        // THEN
        Assert.assertNotNull(out);
        Assert.assertTrue(out.getWidth() > 2);
        Assert.assertTrue(out.getHeight() > 2);
    }
}
