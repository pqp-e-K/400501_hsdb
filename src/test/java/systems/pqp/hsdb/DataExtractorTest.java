package systems.pqp.hsdb;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DataExtractorTest {
    DataExtractor dataExtractor = new DataExtractor();

    @ParameterizedTest
    @ValueSource(strings = {"Folge 1","folge 1", "Teil 1", "Hello World Folge 1", "Hello-Folge 1", "World (1)", "World ( 1 )", "Hello World (1/2)", "Staffel 2 Folge 1: Der Tod","Caiman Club III: Justice (1/4) | Neue Staffel des Polit-Thrillers","CAIMAN CLUB - Größtmögliche Zurückhaltung (St.2 Flg.1) (1/4)","Caiman Club (1. Folge: Test)","Papa, Kevin hat gesagt … (3. Staffel: 1. Folge: Karrieregeil)","Die Wahlverwandtschaften (1. Teil)","Johann Wolfgang von Goethe: Die Wahlverwandtschaften (1/2) | Roman","Die Wahlverwandtschaften (1/2) | Roman"}) // six numbers
    public void episode1(String title) {
        Assertions.assertEquals("1",dataExtractor.getEpisodeFromTitle(title));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Folge 21","folge 21", "Teil 21", "Hello World Folge 21", "Hello-Folge 21", "World (21)", "World ( 21 )", "Hello World (21/2)", "Staffel 2 Folge 21: Der Tod","Caiman Club III: Justice (21/4) | Neue Staffel des Polit-Thrillers","CAIMAN CLUB - Größtmögliche Zurückhaltung (St.2 Flg.21) (21/4)","Caiman Club (21. Folge: Test)","Papa, Kevin hat gesagt … (3. Staffel: 21. Folge: Karrieregeil)","Die Wahlverwandtschaften (21. Teil)","Johann Wolfgang von Goethe: Die Wahlverwandtschaften (21/22) | Roman","Die Wahlverwandtschaften (21/22) | Roman"}) // six numbers
    public void episode21(String title) {
        Assertions.assertEquals("21",dataExtractor.getEpisodeFromTitle(title));
    }

    @Test
    public void getTitleWithoutEpisodeOrSeason() {
        Assertions.assertEquals("",dataExtractor.getTitleWithoutEpisodeOrSeason("Folge 21"));
//        Assert.assertEquals("",dataExtractor.getTitleWithoutEpisode("folge 21"));
        Assertions.assertEquals("",dataExtractor.getTitleWithoutEpisodeOrSeason("Teil 1"));
        Assertions.assertEquals("Hello World",dataExtractor.getTitleWithoutEpisodeOrSeason("Hello World Folge 1"));
        Assertions.assertEquals("Hello-",dataExtractor.getTitleWithoutEpisodeOrSeason("Hello-Folge 1"));
        Assertions.assertEquals("World",dataExtractor.getTitleWithoutEpisodeOrSeason("World (1)"));

        Assertions.assertEquals("Hello World",dataExtractor.getTitleWithoutEpisodeOrSeason("Hello World (1/2)"));
        Assertions.assertEquals("Der Tod",dataExtractor.getTitleWithoutEpisodeOrSeason("Staffel 2 Folge 1: Der Tod"));
        Assertions.assertEquals("Caiman Club III: Justice | Neue Staffel des Polit-Thrillers",dataExtractor.getTitleWithoutEpisodeOrSeason("Caiman Club III: Justice (1/4) | Neue Staffel des Polit-Thrillers"));
        Assertions.assertEquals("CAIMAN CLUB Größtmögliche Zurückhaltung",dataExtractor.getTitleWithoutEpisodeOrSeason("CAIMAN CLUB - Größtmögliche Zurückhaltung (St.2 Flg.1) (1/4)"));
        Assertions.assertEquals("Caiman Club Test",dataExtractor.getTitleWithoutEpisodeOrSeason("Caiman Club (1. Folge: Test)"));
        Assertions.assertEquals("Papa, Kevin hat gesagt Karrieregeil",dataExtractor.getTitleWithoutEpisodeOrSeason("Papa, Kevin hat gesagt … (3. Staffel: 1. Folge: Karrieregeil)"));
        Assertions.assertEquals("Die Wahlverwandtschaften",dataExtractor.getTitleWithoutEpisodeOrSeason("Die Wahlverwandtschaften (1. Teil)"));
        Assertions.assertEquals("Johann Wolfgang von Goethe Die Wahlverwandtschaften | Roman",dataExtractor.getTitleWithoutEpisodeOrSeason("Johann Wolfgang von Goethe: Die Wahlverwandtschaften (1/2) | Roman"));
        Assertions.assertEquals("Die Wahlverwandtschaften | Roman",dataExtractor.getTitleWithoutEpisodeOrSeason("Die Wahlverwandtschaften (1/2) | Roman"));
        Assertions.assertEquals("Papa, Kevin hat gesagt … Arabisch",dataExtractor.getTitleWithoutEpisodeOrSeason("Papa, Kevin hat gesagt … (3. Staffel: 10. Folge: Arabisch)"));
    }
}
