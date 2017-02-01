package backend;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.str.backend.GameOfLifeState;
import org.str.backend.LifParser;

public class LifParserTest {

    private LifParser underTest = new LifParser();

    @Test
    public void testParser() {
        // GIVEN
        final String content = "#Life 1.05\n" + "#D desc 1\n" + "#D desc 2\n" + "#N\n" + "#P -2 2\n" + "***\n" + "*..\n"
                + ".*.\n" + "#P 10 10\n" + "*\n";
        // WHEN
        InputStream stream = IOUtils.toInputStream(content, StandardCharsets.US_ASCII);
        GameOfLifeState r = underTest.parse(stream);
        // THEN
        Assert.assertNotNull(r);
        Assert.assertEquals(14, r.getWidth());
        Assert.assertEquals(10, r.getHeight());
        Assert.assertTrue(r.get(0, 0));
        Assert.assertTrue(r.get(1, 0));
        Assert.assertTrue(r.get(2, 0));
        Assert.assertTrue(r.get(0, 1));
        Assert.assertFalse(r.get(1, 1));
        Assert.assertFalse(r.get(2, 1));
        Assert.assertFalse(r.get(0, 2));
        Assert.assertTrue(r.get(1, 2));
        Assert.assertFalse(r.get(2, 2));
        Assert.assertTrue(r.get(12, 8));
    }
}
