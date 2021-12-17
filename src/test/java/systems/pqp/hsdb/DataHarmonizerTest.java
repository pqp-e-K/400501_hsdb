package systems.pqp.hsdb;

import org.junit.Assert;
import org.junit.Test;

public class DataHarmonizerTest {

    @Test
    public void germanDate() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "05.03.2021";
        String output = harmonizer.date(input);
        Assert.assertEquals("German-Date matches","2021-03-05", output);
    }

    @Test
    public void shortGermanDate() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "03.2021";
        String output = harmonizer.date(input);
        Assert.assertEquals("Short German-Date matches","2021-03-XX", output);
    }

    @Test
    public void britishDate() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "2021-02-18T18:30:00.000+0100";
        String output = harmonizer.date(input);
        Assert.assertEquals("British Date matches","2021-02-18", output);
    }

    @Test
    public void britishDateWithDots() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "2021.02.18T18:30:00.000+0100";
        String output = harmonizer.date(input);
        Assert.assertEquals("British Date with dots matches","2021-02-18", output);
    }

    @Test
    public void britishDateWithSlashes() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "2021/02/18 10pm";
        String output = harmonizer.date(input);
        Assert.assertEquals("British Date with slashes matches","2021-02-18", output);
    }

    @Test(expected = DataHarmonizerException.class)
    public void badFormat() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "2021 02 18 10:21:21.21321";
        harmonizer.date(input);
    }
}
