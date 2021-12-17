package systems.pqp.hsdb;

import com.google.gson.Gson;
import de.ard.sad.normdb.similarity.compare.generic.GenericSimilarity;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.junit.Assert;
import org.junit.Test;
import systems.pqp.hsdb.dao.AudiothekDao;
import systems.pqp.hsdb.dao.HsdbDao;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SimilarityCheckTest {

    SimilarityCheck similarityCheck = new SimilarityCheck();

    @Test
    public void testSimilarity() throws IOException {
        // Jules Verne Reise von der Erde zum Mond
        // GenericObject aus Database
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1444441"
                );
        Assert.assertEquals(1, databaseObjects.size());
        GenericObject dbObject = databaseObjects.get("1444441");

        // GenericObject aus Api (mocked aus Datei)
        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/jules-verne-95022544.json"));

        Assert.assertFalse( similarityCheck.checkSimilarity(dbObject, apiObject));  //Unterschiedliche Umsetzung
    }

    @Test
    public void testSimilarity2() throws IOException {
        /**
         * MariaDB [hsdb]> SELECT DUKEY,SUBSTRING(REPLACE(VOLLINFO,CHAR(10),''),50,200) FROM hs_du WHERE VOLLINFO like "%Christa Wolf%" AND VOLLINFO like "%Kassandra%";
         * +---------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
         * | DUKEY   | SUBSTRING(REPLACE(VOLLINFO,CHAR(10),''),50,200)                                                                                                                                                               |
         * +---------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
         * | 1372136 | garis</KOM>         <BEAW>Gerhard Wolf</BEAW>         <AUT>Christa Wolf</AUT>         <RHTI>Kassandra</RHTI>         <SPR>            <NAM rolle="Kassandra">Barbara Freier</NAM>            <NAM rolle=      |
         * | 1393951 | ert</KOM>         <BEAW>Jörg Jannings</BEAW>         <AUT>Christa Wolf</AUT>         <RHTI>[Medea. Stimmen] (3. Teil: Das Geräusch einstürzender Wände im Ohr)</RHTI>         <UNTI>Radiostück in drei T      |
         * | 1393952 | ert</KOM>         <BEAW>Jörg Jannings</BEAW>         <AUT>Christa Wolf</AUT>         <RHTI>[Medea. Stimmen] (2. Teil: Zeitwände)</RHTI>         <UNTI>Radiostück in drei Teilen</UNTI>         <LITV>Med      |
         * | 1393953 | ert</KOM>         <BEAW>Jörg Jannings</BEAW>         <AUT>Christa Wolf</AUT>         <RHTI>[Medea. Stimmen] (1. Teil: Wo die Zeiten sich treffen)</RHTI>         <UNTI>Radiostück in drei Teilen</UNTI>       |
         * | 1424348 | Wolff[307460]</AUT>         <RHTI>Im Stein</RHTI>         <LITV>Im Stein (Prosa)</LITV>         <SPR>            <NAM>Corinna Harfouch[202585]</NAM>         </SPR>         <TAL>Bernd Friebel, Sabine W      |
         * +---------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
         */
        // Christa Wolf: Kassandra
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1372136 OR DUKEY = 1393951 OR DUKEY = 1393952 OR DUKEY = 1393953 OR DUKEY = 1424348"
                );
        Assert.assertEquals(5, databaseObjects.size());

        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/christa-wolf-94736562.json"));

        GenericSimilarity gs = new GenericSimilarity();

        Assert.assertTrue(gs.calcSimilarity(databaseObjects.get("1372136"), apiObject) < 0.8f);
        Assert.assertTrue(gs.calcSimilarity(databaseObjects.get("1393951"), apiObject) < 0.8f);
        Assert.assertTrue(gs.calcSimilarity(databaseObjects.get("1393952"), apiObject) < 0.8f);
        Assert.assertTrue(gs.calcSimilarity(databaseObjects.get("1393953"), apiObject) < 0.8f);
        Assert.assertTrue(gs.calcSimilarity(databaseObjects.get("1424348"), apiObject) < 0.8f);

    }

    @Test
    public void testSimilarity3() throws IOException {
        /**
         * SELECT DUKEY,SUBSTRING(REPLACE(VOLLINFO,CHAR(10),''),50,200) FROM hs_du where VOLLINFO like "%Dostojewski%" and VOLLINFO like "%Der Doppelgänger%";
         * +---------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
         * | DUKEY   | SUBSTRING(REPLACE(VOLLINFO,CHAR(10),''),50,200)                                                                                                                                                             |
         * +---------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
         * | 1377607 |  Cramer</BEAW>         <AUT>Fjodor Michailowitsch Dostojewski[42649]</AUT>         <UEB>Hermann Röhl</UEB>         <RHTI>Die Abenteuer des Herrn Goljadkin</RHTI>         <LITV>Der Doppelgänger (Roman,    |
         * | 1444949 | er</KOM>         <AUT>Fjodor Michailowitsch Dostojewski[42649]</AUT>         <UEB>Georg Schwarz</UEB>         <RHTI>Der Doppelgänger</RHTI>         <RTI>Schatten - Spiegel - Klone</RTI>         <LITV>    |
         * | 1466678 | t[311866]</AUT>         <RHTI>[Ich bin ein literarischer Proletarier oder Dostojewskis wilde Jagd] (2. Teil: Der Doppelgänger)</RHTI>         <UNTI>Hörstück nach Briefen und Werken Fjodor Dostojewskis    |
         * | 1522315 | henska, Florian Hawemann, Karolin Killig, Johannes Leisen, Heiko Martens, Sophie Narr, Johannes Scherzer, Ulli Scuda, Antje Volkmann, Daniel Wild, Kai Theißen</AUT>         <RHTI>Ich ist ein anderer</    |
         * +---------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
         */
        // Fjodor Dostojewski: Der Doppelgänger
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1377607 OR DUKEY = 1444949 OR DUKEY = 1466678 OR DUKEY = 1522315"
                );
        Assert.assertEquals(4, databaseObjects.size());

        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/dostojewski-94663538.json"));

        GenericSimilarity gs = new GenericSimilarity();

        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("1444949"), apiObject));
        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("1377607"), apiObject));
        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("1466678"), apiObject));
        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("1522315"), apiObject));
    }

    @Test
    public void testSimilarity4() throws IOException {
        /**
         * SELECT DUKEY,SUBSTRING(REPLACE(VOLLINFO,CHAR(10),''),50,200) FROM hs_du WHERE VOLLINFO like "%Sodom und Gomorrha%";
         * +---------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
         * | DUKEY   | SUBSTRING(REPLACE(VOLLINFO,CHAR(10),''),50,200)                                                                                                                                                            |
         * +---------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
         * | 1373362 | e</KOM>         <AUT>Anonym, Beate Morgenstern</AUT>         <RHTI>Wie in Sodom und Gomorrha</RHTI>         <RTI>Bibelgeschichten</RTI>         <LITV>Biblische Erzählung (hebräisch)</LITV>         <SP   |
         * | 1411923 | ntjes</KOM>         <BEAW>Otto Kurth</BEAW>         <AUT>Jean Giraudoux[58332]</AUT>         <UEB>Gerda von Uslar, Wilhelm Michael Treichlinger</UEB>         <RHTI>Sodom und Gomorrha</RHTI>         <R   |
         * | 1477013 | [160316]</AUT>         <UEB>Ana Maria Brock[1456478]</UEB>         <BEAW>Heinz von Cramer[35131]</BEAW>         <RHTI>[Die Ahnungslosen im alten Europa] (2. Teil: Per Schiff quer durchs Mittelmeer)</R   |
         * | 1527012 | ova</AUT>         <UEB>Hans Skirecki[306023]</UEB>         <KOM>Wolfgang Schoor[257546]</KOM>         <RHTI>Das sechste Buch Mose </RHTI>         <SPR>            <NAM rolle="Mose">Christoph Engel</NA   |
         * | 1550580 | gner</AUT>         <RHTI>Auf der Suche nach den zehn Gerechten</RHTI>         <SPR>            <NAM rolle="Soldat">Edgar Ott</NAM>            <NAM rolle="Knabenstimme">Dieter Donner</NAM>            <   |
         * | 4949489 | ust[126474]</AUT>         <UEB>Bernd-Jürgen Fischer</UEB>         <BEAW>Manfred Hess, Hermann Kretzschmar[393061]</BEAW>         <KOM>Hermann Kretzschmar[393061]</KOM>         <ABRFA>SWR2</ABRFA>        |
         * | 4949491 | ust[126474]</AUT>         <UEB>Bernd-Jürgen Fischer</UEB>         <BEAW>Manfred Hess, Hermann Kretzschmar[393061]</BEAW>         <KOM>Hermann Kretzschmar[393061]</KOM>         <ABRFA>SWR2</ABRFA>        |
         * | 4949492 | ust[126474]</AUT>         <UEB>Bernd-Jürgen Fischer</UEB>         <BEAW>Manfred Hess, Hermann Kretzschmar[393061]</BEAW>         <KOM>Hermann Kretzschmar[393061]</KOM>         <ABRFA>SWR2</ABRFA>        |
         * | 4987009 | etzschmar[87874], Jean Racine[127412], Marcel Proust[126474]</AUT>         <UEB>Bernd-Jürgen Fischer</UEB>         <KOM>Hermann Kretzschmar[87874]</KOM>         <ABRFA>SWR2</ABRFA>         <RHTI>Phant   |
         * +---------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
         */
        // Sodom und Gomorrha
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1373362 OR DUKEY = 1411923 OR DUKEY = 1477013 OR DUKEY = 1527012 OR DUKEY = 1550580 OR DUKEY = 4949489 OR DUKEY = 4949491 OR DUKEY = 4949492 OR DUKEY = 4987009"
                );
        Assert.assertEquals(9, databaseObjects.size());

        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/sodom-und-gomorrha-94512976.json"));

        // Tipp: Mit command+option+shift + Mauszeiger kann man Blockauswahlen
        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("1411923"), apiObject));
        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("1373362"), apiObject));
        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("1477013"), apiObject));
        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("1527012"), apiObject));
        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("1550580"), apiObject));
        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("4987009"), apiObject));
        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("4949489"), apiObject));     //Teil 1
        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("4949491"), apiObject));     //Teil 2
        Assert.assertFalse(similarityCheck.checkSimilarity(databaseObjects.get("4949492"), apiObject));     //Teil 3
        //Teil 4 fehlt in HSDB
    }

    // -- Neue Tests mit Beispielen aus Mail -- //

    /**
     * Hörspiel, ARD-Audiothek weitere Metadaten vorhanden (Beispiel für gute Datenlage)
     * Once a Beauty
     * https://hoerspiele.dra.de/vollinfo.php?dukey=4987635&vi=1&SID
     * https://www.ardaudiothek.de/episode/hoerspiel/once-a-beauty-rechter-terror-hinter-buergerlicher-fassade/wdr-3/86736440
     * @throws IOException
     */
    @Test
    public void guteDatenlage() throws IOException {
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 4987635"
                );

        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/once-a-beauty-86736440.json"));
        GenericSimilarity gs = new GenericSimilarity();
        assertSimilarity("Hörspiel, ARD-Audiothek weitere Metadaten vorhanden (Beispiel für gute Datenlage)",
                gs.calcSimilarity(databaseObjects.get("4987635"), apiObject), 0.8f, true);
    }

    @Test
    public void mapPartitionMatch() throws IOException {
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 4987635"
                );
        Map<String, GenericObject> apiObjects = new HashMap<>();
        apiObjects.put("86736440", AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/once-a-beauty-86736440.json")));

        Assert.assertEquals(1, similarityCheck.mapPartition(new ArrayList<>(apiObjects.keySet()), databaseObjects, apiObjects));
    }

    /**
     * Hörspiel, in ARD-Audiothek schlechte Datenlage
     * De Rerum Natura
     * https://hoerspiele.dra.de/vollinfo.php?dukey=4913587&vi=1&SID
     * https://www.ardaudiothek.de/episode/hoerspiel/de-rerum-natura-dance-of-the-elements-oder-ueber-die-natur-der-dinge-nach-lukrez/deutschlandfunk-kultur/92212772
     * @throws IOException
     */
    @Test
    public void schlechteDatenlage() throws IOException {
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 4913587"
                );

        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/de-rerum-natura-92212772.json"));
        GenericSimilarity gs = new GenericSimilarity();
        assertSimilarity("Hörspiel, in ARD-Audiothek schlechte Datenlage",
                gs.calcSimilarity(databaseObjects.get("4913587"), apiObject), 0.8f, true);
    }

    /**
     * Hörspiel (Mehrteiler) in ARD-Audiothek Metadaten + Pressetext vorhanden, Titel unsauber:
     * https://hoerspiele.dra.de/vollinfo.php?dukey=1429898&vi=4&SID
     * https://www.ardaudiothek.de/episode/ndr-hoerspiel-box/johann-wolfgang-von-goethe-die-wahlverwandtschaften-1-2-oder-roman/ndr-kultur/86800910
     * @throws IOException
     */
    @Test
    public void verschachtelterTitel() throws IOException {
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1429898"
                );
        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/wahlverwandtschaften-86800910.json"));
        GenericSimilarity gs = new GenericSimilarity();
        assertSimilarity("Hörspiel (Mehrteiler) in ARD-Audiothek Metadaten + Pressetext vorhanden, Titel unsauber",
                gs.calcSimilarity(databaseObjects.get("1429898"), apiObject), 0.8f, true);
    }

    /**
     * Hörspielreihe, unterschiedliche Teilung (12 vs. 24 Teile)
     * - bei Hörspielreihen wäre es generell wünschenswert,
     * wenn auf die Sendungsseite der ARD-Audiothek, nicht eine einzelne Episodenseite, verlinkt würde.
     * Dies würde das Problem der unterschiedlichen Teilung elegant lösen.
     *
     * https://hoerspiele.dra.de/vollinfo.php?dukey=4988145&vi=11&SID
     *
     * https://www.ardaudiothek.de/sendung/saal-101-dokumentarhoerspiel-zum-nsu-prozess/85721498
     * @throws IOException
     */
    @Test
    public void unterschiedlicheTeilung() throws IOException {
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 4988145"
                );
        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/nsu-prozess-85721498.json"));
        GenericSimilarity gs = new GenericSimilarity();
        assertSimilarity("Hörspielreihe, unterschiedliche Teilung (12 vs. 24 Teile)",
                gs.calcSimilarity(databaseObjects.get("4988145"), apiObject), 0.8f, true);
    }

    /**
     * Hörspieltitel identisch, Untertitel unterschiedlich
     * https://hoerspiele.dra.de/vollinfo.php?dukey=4987715&vi=10&SID
     *
     * https://www.ardaudiothek.de/episode/hoerspiel-studio/r_crusoe-tm-oder-dystopie-einer-kuenstlich-intelligente-welt/swr2/90266522
     */
    @Test
    public void unterschiedlicheUntertitel() throws IOException {
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 4987715"
                );
        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/ki-90266522.json"));
        GenericSimilarity gs = new GenericSimilarity();
        assertSimilarity("Hörspieltitel identisch, Untertitel unterschiedlich",
                gs.calcSimilarity(databaseObjects.get("4987715"), apiObject), 0.8f, true);
    }

    /**
     * Hörspielreihe: ARD Radio Tatort
     * (eigenes Beispiel, das im Word funktioniert nicht, da die ID nicht im Dump vorkommt...)
     * https://www.ardaudiothek.de/episode/ard-radio-tatort/bankraub-und-gerechtigkeit-oder-die-muenchner-kommissare-ermitteln/ard-de/82720556/
     *
     * https://hoerspiele.dra.de/vollinfo.php?dukey=4981555&vi=5&SID
     *
     */
    @Test
    public void tatortFolgen() throws IOException {
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 4981555"
                );
        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/tatort-82720556.json"));
        GenericSimilarity gs = new GenericSimilarity();
        assertSimilarity("Hörspielreihe: ARD Radio Tatort",
                gs.calcSimilarity(databaseObjects.get("4981555"), apiObject), 0.8f, true);
    }

    /**
     * Hörspieltitel gleich, aber nicht identischer Datensatz:
     * https://hoerspiele.dra.de/vollinfo.php?dukey=1443307&vi=1&SID
     * https://www.ardaudiothek.de/episode/ndr-hoerspiel-box/johann-wolfgang-von-goethe-die-wahlverwandtschaften-1-2-oder-roman/ndr-kultur/86800910
     * @throws IOException
     */
    @Test
    public void gleichAberNichtIdentisch() throws IOException {
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1443307"
                );
        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/wahlverwandtschaften-86800910.json"));
        GenericSimilarity gs = new GenericSimilarity();
        assertSimilarity("Hörspieltitel gleich, aber nicht identischer Datensatz",
                gs.calcSimilarity(databaseObjects.get("1443307"), apiObject), 0.8f, false);
    }

    @Test
    public void mapPartitionsNoMatch() throws IOException {
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1443307"
                );
        Map<String, GenericObject> apiObjects = new HashMap<>();
        apiObjects.put("86800910", AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/wahlverwandtschaften-86800910.json")));

        Assert.assertEquals(0, similarityCheck.mapPartition(new ArrayList<>(apiObjects.keySet()), databaseObjects, apiObjects));
    }

    /**
     * Mehrteiler, in HSPDB zwei Fassungen (6 und 8 Teile), in ARD-Audiothek nur gekürzte Fassung (6 Teile) vorhanden
     * a)	https://hoerspiele.dra.de/vollinfo.php?dukey=1423911&vi=14&SID
     * b)	https://hoerspiele.dra.de/vollinfo.php?dukey=4993131&vi=6&SID
     * TODO 4993131 nicht in DUMP! Daher nur Test von 1423911 (8 Episoden) mit 92281450 (6 Episoden und neuer)
     *
     * https://www.ardaudiothek.de/episode/hoerspiel-pool/terra-incognita-1-6-oder-science-fiction-oeko-thriller/bayern-2/92281450
     */
    @Test
    public void falscheFassung() throws IOException {
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1423911"
                );
        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/terra-incognita-92281450.json"));
        GenericSimilarity gs = new GenericSimilarity();
        assertSimilarity("Mehrteiler, in HSPDB zwei Fassungen (6 und 8 Teile), in ARD-Audiothek nur gekürzte Fassung (6 Teile) vorhanden",
                gs.calcSimilarity(databaseObjects.get("1423911"), apiObject), 0.8f, false);
    }

    @Test
    public void dorfDisko() throws IOException {
        Map<String,GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1363831"
                );
        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/dorfdisco-57571284.json"));
        GenericSimilarity gs = new GenericSimilarity();
        assertSimilarity("Dorfdisko",
                gs.calcSimilarity(databaseObjects.get("1363831"), apiObject), 0.8f, false);
    }

    // --------------------------------------- //

    /**
     * Helfer-Methode zum Laden eines JSON-File
     * @param fileName Pfad als String
     * @return Map
     * @throws IOException wenn File not found
     */
    Map loadJsonFromFile(String fileName) throws IOException {
        Gson gson = new Gson();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(fileName);
        assert stream != null;
        Map json = gson.fromJson(new InputStreamReader(stream), Map.class);
        stream.close();
        return json;
    }

    void assertSimilarity(String message, Float value, Float compareValue, boolean greater){
        if( greater ) {
            Assert.assertTrue(message + " " + value, value > compareValue);
        } else {
            Assert.assertTrue(message + " " + value, value < compareValue);
        }
    }
}
