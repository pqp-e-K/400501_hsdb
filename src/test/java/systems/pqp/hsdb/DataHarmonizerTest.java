package systems.pqp.hsdb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataHarmonizerTest {

    @Test
    public void germanDate() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "05.03.2021";
        String output = harmonizer.date(input);
        Assertions.assertEquals("German-Date matches","2021-03-05", output);
    }

    @Test
    public void shortGermanDate() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "03.2021";
        String output = harmonizer.date(input);
        Assertions.assertEquals("Short German-Date matches","2021-03-XX", output);
    }

    @Test
    public void britishDate() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "2021-02-18T18:30:00.000+0100";
        String output = harmonizer.date(input);
        Assertions.assertEquals("British Date matches","2021-02-18", output);
    }

    @Test
    public void britishDateWithDots() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "2021.02.18T18:30:00.000+0100";
        String output = harmonizer.date(input);
        Assertions.assertEquals("British Date with dots matches","2021-02-18", output);
    }

    @Test
    public void britishDateWithSlashes() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "2021/02/18 10pm";
        String output = harmonizer.date(input);
        Assertions.assertEquals("British Date with slashes matches","2021-02-18", output);
    }

    @Test
    public void badFormat() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "2021 02 18 10:21:21.21321";
        harmonizer.date(input);
    }

    @Test
    public void circaYear() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "circa 1970";
        Assertions.assertEquals("1970-XX-XX", harmonizer.date(input));
        input = "ca 1970";
        Assertions.assertEquals("1970-XX-XX", harmonizer.date(input));
        input = "ca. 1970";
        Assertions.assertEquals("1970-XX-XX", harmonizer.date(input));
        input = "circa 01.1976";
        Assertions.assertEquals("1976-01-XX", harmonizer.date(input));
        input = "12.04.1975";
        Assertions.assertEquals("1975-04-12", harmonizer.date(input));
    }

    @Test
    public void onlyYear() throws DataHarmonizerException {
        DataHarmonizer harmonizer = new DataHarmonizer();
        String input = "1970";
        Assertions.assertEquals("1970-XX-XX", harmonizer.date(input));
    }
}
