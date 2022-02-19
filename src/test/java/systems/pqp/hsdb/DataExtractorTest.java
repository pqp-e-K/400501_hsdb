package systems.pqp.hsdb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DataExtractorTest {
    @ParameterizedTest
    @ValueSource(strings = {"Folge 1","folge 1", "Teil 1", "Hello World Folge 1", "Hello-Folge 1", "World (1)", "World ( 1 )", "Hello World (1/2)", "Staffel 2 Folge 1: Der Tod","Caiman Club III: Justice (1/4) | Neue Staffel des Polit-Thrillers","CAIMAN CLUB - Größtmögliche Zurückhaltung (St.2 Flg.1) (1/4)","Caiman Club (1. Folge: Test)","Papa, Kevin hat gesagt … (3. Staffel: 1. Folge: Karrieregeil)","Die Wahlverwandtschaften (1. Teil)","Johann Wolfgang von Goethe: Die Wahlverwandtschaften (1/2) | Roman","Die Wahlverwandtschaften (1/2) | Roman","Professor van Dusen ermittelt (1. Fall: Professor van Dusen treibt den Teufel aus)","Karl May: Sitara - Land der Sternenblumen (Teil I: Marah Durimeh)"}) // six numbers
    public void episode1(String title) {
        Assertions.assertEquals(1,DataExtractor.getEpisodeFromTitle(title));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Folge 21","folge 21", "Teil 21", "Hello World Folge 21", "Hello-Folge 21", "World (21)", "World ( 21 )", "Hello World (21/2)", "Staffel 2 Folge 21: Der Tod","Caiman Club III: Justice (21/4) | Neue Staffel des Polit-Thrillers","CAIMAN CLUB - Größtmögliche Zurückhaltung (St.2 Flg.21) (21/4)","Caiman Club (21. Folge: Test)","Papa, Kevin hat gesagt … (3. Staffel: 21. Folge: Karrieregeil)","Die Wahlverwandtschaften (21. Teil)","Johann Wolfgang von Goethe: Die Wahlverwandtschaften (21/22) | Roman","Die Wahlverwandtschaften (21/22) | Roman","Professor van Dusen ermittelt (21. Fall: Professor van Dusen treibt den Teufel aus)","Karl May: Sitara - Land der Sternenblumen (Teil XXI: Marah Durimeh)"}) // six numbers
    public void episode21(String title) {
        Assertions.assertEquals(21,DataExtractor.getEpisodeFromTitle(title));
    }

    @Test
    public void removeBracketsWithoutSeasonAndEpisodeInformation() {
        Assertions.assertEquals("Hallo Welt",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Hallo Welt[Eine Geschichte um die Welt]"));
        Assertions.assertEquals("Hallo Welt",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Hallo Welt{Eine Geschichte um die Welt}"));
        Assertions.assertEquals("Hallo Welt",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Hallo Welt(Eine Geschichte um die Welt)"));

        Assertions.assertEquals("Hallo Welt",DataExtractor.removeBracketsWithoutSeasonAndEpisode("[Eine Geschichte um die Welt]Hallo Welt"));
        Assertions.assertEquals("Hallo Welt",DataExtractor.removeBracketsWithoutSeasonAndEpisode("{Eine Geschichte um die Welt}Hallo Welt"));
        Assertions.assertEquals("Hallo Welt",DataExtractor.removeBracketsWithoutSeasonAndEpisode("(Eine Geschichte um die Welt)Hallo Welt"));

        Assertions.assertEquals("Hallo Welt",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Hallo [Eine Geschichte um die Welt] Welt"));
        Assertions.assertEquals("Hallo Welt",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Hallo {Eine Geschichte um die Welt} Welt"));
        Assertions.assertEquals("Hallo Welt",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Hallo (Eine Geschichte um die Welt) Welt"));

        //Nicht verändern
        Assertions.assertEquals("Erste Erde Epos (17. Teil: Erste Pflanzen)",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Erste Erde Epos (17. Teil: Erste Pflanzen)"));
        Assertions.assertEquals("Folge 21",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Folge 21"));
        Assertions.assertEquals("Teil 1",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Teil 1"));
        Assertions.assertEquals("Hello World Folge 1",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Hello World Folge 1"));
        Assertions.assertEquals("Hello-Folge 1",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Hello-Folge 1"));
        Assertions.assertEquals("World (1)",DataExtractor.removeBracketsWithoutSeasonAndEpisode("World (1)"));

        Assertions.assertEquals("Hello World (1/2)",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Hello World (1/2)"));
        Assertions.assertEquals("Staffel 2 Folge 1: Der Tod",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Staffel 2 Folge 1: Der Tod"));
        Assertions.assertEquals("Caiman Club III: Justice (1/4) | Neue Staffel des Polit-Thrillers",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Caiman Club III: Justice (1/4) | Neue Staffel des Polit-Thrillers"));
        Assertions.assertEquals("CAIMAN CLUB - Größtmögliche Zurückhaltung (St.2 Flg.1) (1/4)",DataExtractor.removeBracketsWithoutSeasonAndEpisode("CAIMAN CLUB - Größtmögliche Zurückhaltung (St.2 Flg.1) (1/4)"));
        Assertions.assertEquals("Caiman Club (1. Folge: Test)",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Caiman Club (1. Folge: Test)"));
        Assertions.assertEquals("Papa, Kevin hat gesagt … (3. Staffel: 1. Folge: Karrieregeil)",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Papa, Kevin hat gesagt … (3. Staffel: 1. Folge: Karrieregeil)"));
        Assertions.assertEquals("Die Wahlverwandtschaften (1. Teil)",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Die Wahlverwandtschaften (1. Teil)"));
        Assertions.assertEquals("Johann Wolfgang von Goethe: Die Wahlverwandtschaften (1/2) | Roman",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Johann Wolfgang von Goethe: Die Wahlverwandtschaften (1/2) | Roman"));
        Assertions.assertEquals("Die Wahlverwandtschaften (1/2) | Roman",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Die Wahlverwandtschaften (1/2) | Roman"));
        Assertions.assertEquals("Papa, Kevin hat gesagt … (3. Staffel: 10. Folge: Arabisch)",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Papa, Kevin hat gesagt … (3. Staffel: 10. Folge: Arabisch)"));

        Assertions.assertEquals("Madame Bovary, Folge 7",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Madame Bovary, Folge 7"));
        Assertions.assertEquals("Folge 4/8: Alice - Kulissen",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Folge 4/8: Alice - Kulissen"));
        Assertions.assertEquals("Alice (4. Folge: Kulissen)",DataExtractor.removeBracketsWithoutSeasonAndEpisode("Alice (4. Folge: Kulissen)"));

        Assertions.assertEquals("Jacobs Zimmer (Cambridge und das Cornwall)",DataExtractor.getTitleWithoutEpisodeOrSeason("Jacobs Zimmer (2. Teil: Cambridge und das Cornwall)"));
        Assertions.assertEquals("Die Faust vor der Sonne",DataExtractor.getTitleWithoutEpisodeOrSeason("Die Faust vor der Sonne (1. Teil)"));

        Assertions.assertEquals("Was dein Name verbirgt : KZ-Überlebender jagt NS-Verbrecher | Krimi",DataExtractor.getTitleWithoutEpisodeOrSeason("Was dein Name verbirgt (1/2): KZ-Überlebender jagt NS-Verbrecher | Krimi"));
    }


    @Test
    public void getTitleWithoutEpisodeOrSeason() {
        Assertions.assertEquals("",DataExtractor.getTitleWithoutEpisodeOrSeason("Folge 21"));
        Assertions.assertEquals("",DataExtractor.getTitleWithoutEpisodeOrSeason("Teil 1"));
        Assertions.assertEquals("Hello World",DataExtractor.getTitleWithoutEpisodeOrSeason("Hello World Folge 1"));
        Assertions.assertEquals("Hello-",DataExtractor.getTitleWithoutEpisodeOrSeason("Hello-Folge 1"));
        Assertions.assertEquals("World",DataExtractor.getTitleWithoutEpisodeOrSeason("World (1)"));

        Assertions.assertEquals("Hello World",DataExtractor.getTitleWithoutEpisodeOrSeason("Hello World (1/2)"));
        Assertions.assertEquals("Der Tod",DataExtractor.getTitleWithoutEpisodeOrSeason("Staffel 2 Folge 1: Der Tod"));
        Assertions.assertEquals("Caiman Club III: Justice | Neue Staffel des Polit-Thrillers",DataExtractor.getTitleWithoutEpisodeOrSeason("Caiman Club III: Justice (1/4) | Neue Staffel des Polit-Thrillers"));
        Assertions.assertEquals("CAIMAN CLUB - Größtmögliche Zurückhaltung",DataExtractor.getTitleWithoutEpisodeOrSeason("CAIMAN CLUB - Größtmögliche Zurückhaltung (St.2 Flg.1) (1/4)"));
        Assertions.assertEquals("Caiman Club (Test)",DataExtractor.getTitleWithoutEpisodeOrSeason("Caiman Club (1. Folge: Test)"));
        Assertions.assertEquals("Papa, Kevin hat gesagt … (Karrieregeil)",DataExtractor.getTitleWithoutEpisodeOrSeason("Papa, Kevin hat gesagt … (3. Staffel: 1. Folge: Karrieregeil)"));
        Assertions.assertEquals("Die Wahlverwandtschaften",DataExtractor.getTitleWithoutEpisodeOrSeason("Die Wahlverwandtschaften (1. Teil)"));
        Assertions.assertEquals("Johann Wolfgang von Goethe: Die Wahlverwandtschaften | Roman",DataExtractor.getTitleWithoutEpisodeOrSeason("Johann Wolfgang von Goethe: Die Wahlverwandtschaften (1/2) | Roman"));
        Assertions.assertEquals("Die Wahlverwandtschaften | Roman",DataExtractor.getTitleWithoutEpisodeOrSeason("Die Wahlverwandtschaften (1/2) | Roman"));
        Assertions.assertEquals("Papa, Kevin hat gesagt … (Arabisch)",DataExtractor.getTitleWithoutEpisodeOrSeason("Papa, Kevin hat gesagt … (3. Staffel: 10. Folge: Arabisch)"));

        Assertions.assertEquals("Madame Bovary",DataExtractor.getTitleWithoutEpisodeOrSeason("Madame Bovary, Folge 7"));
        Assertions.assertEquals("Alice - Kulissen",DataExtractor.getTitleWithoutEpisodeOrSeason("Folge 4/8: Alice - Kulissen"));
        Assertions.assertEquals("Alice (Kulissen)",DataExtractor.getTitleWithoutEpisodeOrSeason("Alice (4. Folge: Kulissen)"));

        Assertions.assertEquals("Jacobs Zimmer (Cambridge und das Cornwall)",DataExtractor.getTitleWithoutEpisodeOrSeason("Jacobs Zimmer (2. Teil: Cambridge und das Cornwall)"));
        Assertions.assertEquals("Die Faust vor der Sonne",DataExtractor.getTitleWithoutEpisodeOrSeason("Die Faust vor der Sonne (1. Teil)"));

        Assertions.assertEquals("Was dein Name verbirgt : KZ-Überlebender jagt NS-Verbrecher | Krimi",DataExtractor.getTitleWithoutEpisodeOrSeason("Was dein Name verbirgt (1/2): KZ-Überlebender jagt NS-Verbrecher | Krimi"));
    }

    @Test
    public void getEpisodeTitle() {

        Assertions.assertEquals("Der Tod",DataExtractor.getEpisodeTitle("Staffel 2 Folge 1: Der Tod"));
        //Assertions.assertEquals("Caiman Club III: Justice | Neue Staffel des Polit-Thrillers",DataExtractor.getEpisodeTitle("Caiman Club III: Justice (1/4) | Neue Staffel des Polit-Thrillers"));
        //Assertions.assertEquals("CAIMAN CLUB - Größtmögliche Zurückhaltung",DataExtractor.getEpisodeTitle("CAIMAN CLUB - Größtmögliche Zurückhaltung (St.2 Flg.1) (1/4)"));
        Assertions.assertEquals("Test",DataExtractor.getEpisodeTitle("Caiman Club (1. Folge: Test)"));
        Assertions.assertEquals("Karrieregeil",DataExtractor.getEpisodeTitle("Papa, Kevin hat gesagt … (3. Staffel: 1. Folge: Karrieregeil)"));
        //Assertions.assertEquals("Die Wahlverwandtschaften",DataExtractor.getEpisodeTitle("Die Wahlverwandtschaften (1. Teil)"));
        //Assertions.assertEquals("Johann Wolfgang von Goethe: Die Wahlverwandtschaften | Roman",DataExtractor.getEpisodeTitle("Johann Wolfgang von Goethe: Die Wahlverwandtschaften (1/2) | Roman"));
        //Assertions.assertEquals("Die Wahlverwandtschaften | Roman",DataExtractor.getEpisodeTitle("Die Wahlverwandtschaften (1/2) | Roman"));
        Assertions.assertEquals("Arabisch",DataExtractor.getEpisodeTitle("Papa, Kevin hat gesagt … (3. Staffel: 10. Folge: Arabisch)"));

        //Assertions.assertEquals("Madame Bovary",DataExtractor.getEpisodeTitle("Madame Bovary, Folge 7"));
        Assertions.assertEquals("Alice - Kulissen",DataExtractor.getEpisodeTitle("Folge 4/8: Alice - Kulissen"));
        Assertions.assertEquals("Kulissen",DataExtractor.getEpisodeTitle("Alice (4. Folge: Kulissen)"));

        Assertions.assertEquals("Cambridge und das Cornwall",DataExtractor.getEpisodeTitle("Jacobs Zimmer (2. Teil: Cambridge und das Cornwall)"));
        //Assertions.assertEquals("Die Faust vor der Sonne",DataExtractor.getEpisodeTitle("Die Faust vor der Sonne (1. Teil)"));

        Assertions.assertEquals("Kopftuch",DataExtractor.getEpisodeTitle("Papa, Kevin hat gesagt … (2. Staffel: 2. Folge: Kopftuch)"));
        Assertions.assertEquals("Kopftuch",DataExtractor.getEpisodeTitle("Papa, Kevin hat gesagt Staffel 2: Kopftuch (2/20)"));

        Assertions.assertEquals("Dschungelcamp",DataExtractor.getEpisodeTitle("Papa, Kevin hat gesagt … (3. Staffel: 12. Folge: Dschungelcamp)"));
        Assertions.assertEquals("Elitenschweine",DataExtractor.getEpisodeTitle("Papa, Kevin hat gesagt Staffel 3: Elitenschweine"));

        //Assertions.assertEquals("Was dein Name verbirgt : KZ-Überlebender jagt NS-Verbrecher | Krimi",DataExtractor.getEpisodeTitle("Was dein Name verbirgt (1/2): KZ-Überlebender jagt NS-Verbrecher | Krimi"));
    }
}
