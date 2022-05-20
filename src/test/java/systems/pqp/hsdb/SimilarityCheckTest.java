package systems.pqp.hsdb;

import com.google.gson.Gson;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import systems.pqp.hsdb.dao.AudiothekDao;
import systems.pqp.hsdb.dao.HsdbDao;
import systems.pqp.hsdb.types.RadioPlaytypeSimilarity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

//@Execution(ExecutionMode.CONCURRENT)
@Deprecated
class SimilarityCheckTest {

    SimilarityCheck similarityCheck = new SimilarityCheck();

    Map<String, GenericObject> audiothekObjects;

    final static float compareValue = 0.9f;

    public SimilarityCheckTest() throws IOException {
        audiothekObjects = AudiothekDao.genericObjectsFromDisk("api-examples/api.json.zip");
    }

    @Test
    void testSimilarity2() throws IOException {
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
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1372136 OR DUKEY = 1393951 OR DUKEY = 1393952 OR DUKEY = 1393953 OR DUKEY = 1424348"
                );
        Assertions.assertEquals(5, databaseObjects.size());

        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/christa-wolf-94736562.json"));

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        Assertions.assertTrue(gs.calcSimilarity(databaseObjects.get("1372136"), apiObject) < compareValue);
        Assertions.assertTrue(gs.calcSimilarity(databaseObjects.get("1393951"), apiObject) < compareValue);
        Assertions.assertTrue(gs.calcSimilarity(databaseObjects.get("1393952"), apiObject) < compareValue);
        Assertions.assertTrue(gs.calcSimilarity(databaseObjects.get("1393953"), apiObject) < compareValue);
        Assertions.assertTrue(gs.calcSimilarity(databaseObjects.get("1424348"), apiObject) < compareValue);
    }

    @Test
    void testSimilarity3() throws IOException {
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
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1377607 OR DUKEY = 1444949 OR DUKEY = 1466678 OR DUKEY = 1522315"
                );
        Assertions.assertEquals(4, databaseObjects.size());

        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/dostojewski-94663538.json"));

        RadioPlaytypeSimilarity similarityTest = new RadioPlaytypeSimilarity();
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("1444949"), apiObject));
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("1377607"), apiObject));
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("1466678"), apiObject));
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("1522315"), apiObject));
    }

    @Test
    void testSimilarity4() throws IOException {
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
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1373362 OR DUKEY = 1411923 OR DUKEY = 1477013 OR DUKEY = 1527012 OR DUKEY = 1550580 OR DUKEY = 4949489 OR DUKEY = 4949491 OR DUKEY = 4949492 OR DUKEY = 4987009"
                );
        Assertions.assertEquals(9, databaseObjects.size());

        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/sodom-und-gomorrha-94512976.json"));
        RadioPlaytypeSimilarity similarityTest = new RadioPlaytypeSimilarity();
        // Tipp: Mit command+option+shift + Mauszeiger kann man Blockauswahlen
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("1411923"), apiObject));
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("1373362"), apiObject));
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("1477013"), apiObject));
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("1527012"), apiObject));
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("1550580"), apiObject));
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("4987009"), apiObject));
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("4949489"), apiObject));     //Teil 1
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("4949491"), apiObject));     //Teil 2
        Assertions.assertFalse(similarityCheck.checkSimilarity(similarityTest, databaseObjects.get("4949492"), apiObject));     //Teil 3
        //Teil 4 fehlt in HSDB
    }

    // -- Neue Tests mit Beispielen aus Mail -- //

    /**
     * Hörspiel, ARD-Audiothek weitere Metadaten vorhanden (Beispiel für gute Datenlage)
     * Once a Beauty
     * https://hoerspiele.dra.de/vollinfo.php?dukey=4987635&vi=1&SID
     * https://www.ardaudiothek.de/episode/hoerspiel/once-a-beauty-rechter-terror-hinter-buergerlicher-fassade/wdr-3/86736440
     *
     * @throws IOException
     */
    @Test
    void guteDatenlage() throws IOException {
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 4987635"
                );

        GenericObject apiObject = audiothekObjects.get("86736440");
        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();
        assertSimilarity("Hörspiel, ARD-Audiothek weitere Metadaten vorhanden (Beispiel für gute Datenlage)",
                gs.calcSimilarity(databaseObjects.get("4987635"), apiObject), compareValue, true);
    }

    /**
     * Hörspiel, in ARD-Audiothek schlechte Datenlage
     * De Rerum Natura
     * https://hoerspiele.dra.de/vollinfo.php?dukey=4913587&vi=1&SID
     * https://www.ardaudiothek.de/episode/hoerspiel/de-rerum-natura-dance-of-the-elements-oder-ueber-die-natur-der-dinge-nach-lukrez/deutschlandfunk-kultur/92212772
     *
     * @throws IOException
     */
    @Test
    void schlechteDatenlage() throws IOException {
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 4913587"
                );

        GenericObject apiObject = audiothekObjects.get("92212772");
        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();
        assertSimilarity("Hörspiel, in ARD-Audiothek schlechte Datenlage",
                gs.calcSimilarity(databaseObjects.get("4913587"), apiObject), compareValue, true);
    }

    /**
     * Hörspiel (Mehrteiler) in ARD-Audiothek Metadaten + Pressetext vorhanden, Titel unsauber:
     * https://hoerspiele.dra.de/vollinfo.php?dukey=1429898&vi=4&SID
     * https://www.ardaudiothek.de/episode/ndr-hoerspiel-box/johann-wolfgang-von-goethe-die-wahlverwandtschaften-1-2-oder-roman/ndr-kultur/86800910
     *
     * @throws IOException
     */
    @Test
    void verschachtelterTitel() throws IOException {
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1429898"
                );
        GenericObject apiObject = audiothekObjects.get("86800910");
        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();
        assertSimilarity("Hörspiel (Mehrteiler) in ARD-Audiothek Metadaten + Pressetext vorhanden, Titel unsauber",
                gs.calcSimilarity(databaseObjects.get("1429898"), apiObject), compareValue, true);
    }

    /**
     * Hörspieltitel identisch, Untertitel unterschiedlich
     * https://hoerspiele.dra.de/vollinfo.php?dukey=4987715&vi=10&SID
     * <p>
     * https://www.ardaudiothek.de/episode/hoerspiel-studio/r_crusoe-tm-oder-dystopie-einer-kuenstlich-intelligente-welt/swr2/90266522
     */
    @Test
    void unterschiedlicheUntertitel() throws IOException {
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 4987715"
                );
        GenericObject apiObject = audiothekObjects.get("90266522");
        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();
        assertSimilarity("Hörspieltitel identisch, Untertitel unterschiedlich",
                gs.calcSimilarity(databaseObjects.get("4987715"), apiObject), compareValue, true);
    }

    /**
     * Hörspielreihe: ARD Radio Tatort
     * (eigenes Beispiel, das im Word funktioniert nicht, da die ID nicht im Dump vorkommt...)
     * https://www.ardaudiothek.de/episode/ard-radio-tatort/bankraub-und-gerechtigkeit-oder-die-muenchner-kommissare-ermitteln/ard-de/82720556/
     * <p>
     * https://hoerspiele.dra.de/vollinfo.php?dukey=4981555&vi=5&SID
     */
    @Test
    void tatortFolgen() throws IOException {
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 4981555"
                );
        GenericObject apiObject = AudiothekDao.genericObjectFromJson(loadJsonFromFile("api-examples/tatort-82720556.json"));
        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();
        assertSimilarity("Hörspielreihe: ARD Radio Tatort",
                gs.calcSimilarity(databaseObjects.get("4981555"), apiObject), compareValue, true);
    }

    /**
     * Hörspieltitel gleich, aber nicht identischer Datensatz:
     * https://hoerspiele.dra.de/vollinfo.php?dukey=1443307&vi=1&SID
     * https://www.ardaudiothek.de/episode/ndr-hoerspiel-box/johann-wolfgang-von-goethe-die-wahlverwandtschaften-1-2-oder-roman/ndr-kultur/86800910
     *
     * @throws IOException
     */
    @Test
    void gleichAberNichtIdentisch() throws IOException {
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1443307"
                );
        GenericObject apiObject = audiothekObjects.get("86800910");
        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();
        assertSimilarity("Hörspieltitel gleich, aber nicht identischer Datensatz",
                gs.calcSimilarity(databaseObjects.get("1443307"), apiObject), compareValue, false);
    }
    /**
     * Mehrteiler, in HSPDB zwei Fassungen (6 und 8 Teile), in ARD-Audiothek nur gekürzte Fassung (6 Teile) vorhanden
     * a)	https://hoerspiele.dra.de/vollinfo.php?dukey=1423911&vi=14&SID
     * b)	https://hoerspiele.dra.de/vollinfo.php?dukey=4993131&vi=6&SID
     * TODO 4993131 nicht in DUMP! Daher nur Test von 1423911 (8 Episoden) mit 92281450 (6 Episoden und neuer)
     * <p>
     * https://www.ardaudiothek.de/episode/hoerspiel-pool/terra-incognita-1-6-oder-science-fiction-oeko-thriller/bayern-2/92281450
     */
    @Test
    void falscheFassung() throws IOException {
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1423911"
                );
        GenericObject apiObject = audiothekObjects.get("92281450");
        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();
        assertSimilarity("Mehrteiler, in HSPDB zwei Fassungen (6 und 8 Teile), in ARD-Audiothek nur gekürzte Fassung (6 Teile) vorhanden",
                gs.calcSimilarity(databaseObjects.get("1423911"), apiObject), compareValue, false);
    }

    @Test
    void dorfDisko() throws IOException {
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 1363831"
                );
        GenericObject apiObject = audiothekObjects.get("57571284");
        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();
        assertSimilarity("Dorfdisko",
                gs.calcSimilarity(databaseObjects.get("1363831"), apiObject), compareValue, false);
    }

    @Test
    void PapaKevinHatGesagtStaffel3Karrieregeil() throws IOException {
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = 4974295"
                );
        GenericObject apiObject = audiothekObjects.get("85393100");
        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();
        assertSimilarity("PapaKevinhatgesagtStaffel3Karrieregeil",
                gs.calcSimilarity(databaseObjects.get("4974295"), apiObject), compareValue, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"92266518", "92266530", "92266542", "92266554", "92266566", "92266574", "92266586"})
    void duKey1356987_nichtVerlinken(String ardAudiothekId) throws IOException {
        String duKey = "1356987";
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(ardAudiothekId);

        if(apiObject != null) {
            assertSimilarity("duKey1356987_nichtVerlinken",
                    gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, false);
        }else {
            Assertions.assertNull(apiObject);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"78840808","78840816","78840820","78840824","78840840","93409876","93825032","93825038"})
    void duKey1553947_nichtVerlinken(String ardAudiothekId) throws IOException {
        String duKey = "1553947";
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(ardAudiothekId);

        assertSimilarity("duKey1553947_nichtVerlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"90494588-1349320","89624170-1357093", "82363874-1363517", "72471742-1370378", "78744810-1371854", "91230022-4945026", "78746662-4938892", "91058568-1371868","72322734-1372119","78744434-1372225","78746824-1372469","77851200-1376206","85420534-1418565","91404194-1418587","85605534-1419731","77241150-1424608"})
    void duKey_nichtVerlinken(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_nichtVerlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"95534096-1542630","92895184-1548810","92385884-1543062","92895184-1552617","90998666-1447653","95606184-1543083","78673562-1369295","78744320-1387972","96360142-1423653","96360142-1443196","86800914-1420533","89793002-1411608","86891918-1427603","60616750-4919175","89590686-1448611"})
    void duKey_nichtVerlinken_20220203(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_nichtVerlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"64388650-4955555","64388666-4955567","63419012-4955567","64388574-4955567","64388634-4955567","64388650-4955567","94556768-4955567","94555942-4955567","59258356-4955567","59258372-4955567","94555912-4955567","59258338-4955567"})
    void duKey_nichtVerlinken_hwe_20220207(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_nichtVerlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"59258400-4955567"})
    void duKey_verlinken_hwe_20220207(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_nichtVerlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"68469394-4990470","88339158-4989146","85721498-4988145","93761572-4949819","78746862-1540056","78745322-1516658","82634396-4958901","67252498-4973381","90406454-4974332","90406454-4975621"})
    @Disabled
    void duKey_linken_nicht_moeglich_laufzeit(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_linken_nicht_moeglich_laufzeit",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"95488054-4995425"})
    void duKey_verlinken_unterschiedliche_stueckelung(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_nichtVerlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, true);
    }


    /*@ParameterizedTest
    @ValueSource(strings = {"https://audiothek.ardmediathek.de/programsets/67182050-4973380"})
    void duKey_programsets_verlinken(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_nichtVerlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, true);
    }*/

    @ParameterizedTest
    @ValueSource(strings = {"96017378-1372271","87556106-1435783","88512710-1538809","93550116-1538925","78744090-1539867","47395954-1549240","95956564-3124714","95956564-3124715","95956570-3124715","95956558-3124716","95956564-3124716","95956534-3124718","95956564-3124718","78746120-4914194","95956546-4924989","95956552-4924992","95956576-4924994","96342036-4949491","96341938-4949492","78745906-4954730","78745902-4954731","78743750-4954734","78743742-4954735","80268196-4954737","80268200-4954740","80602562-4954741","80602566-4954742","80942630-4954744","81117418-4954745","81117422-4954746","82715620-4983912","82715620-4987156","88097316-4987209","88097322-4987211","88097326-4987212","88097330-4987216","88097346-4987218","96029414-4992608","96029464-4992611","96029720-4992614","94989774-4995435","94989760-4995445"})
    void duKey_vermeintlich_linken_ggf_untertitel_relevant(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_nichtVerlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"94989748-4996364","94989806-4996365"})
    void duKey_linken_ggf_untertitel_relevant(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_nichtVerlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, true);
    }


    @ParameterizedTest
    @ValueSource(strings = {"78744996-1470833"})
    void duKey_linken(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_nichtVerlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, true);
    }



    @ParameterizedTest
    @ValueSource(strings = {})
    @Disabled
    void duKey_develtop(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_nichtVerlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, false);
    }



    @ParameterizedTest
    @ValueSource(strings = {"83980404-4957447","93759784-4957447","78743988-1530634","86601692-1377490","88208696-1377490","89281614-1377490","90417498-1377490","91996704-1377490","92773166-1377490","93639958-1377490","78744996-1470835"})
    void duKey_nicht_linken(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_nichtVerlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"93018806-4994490","93018874-4994492","84531242-4913292","78744566-1539181","78746008-1530757","78746012-1530750","78745446-4245679","89793350-4936803","89792800-1552354","78745846-1516622","78745838-1516643","78744548-1470833","83840406-4986979","78744402-1529969","78744462-1529970","78796726-1529970","59258338-4955555","93555608-4995537","89209626-4989406","89208536-4989407","89209340-4989408","78744280-1530000","68469466-4990468","68469700-4990459","68469200-4990461","68469254-4990464","68469218-4990466","68469236-4990467","68469412-4990471","68469448-4990473","68469480-4990476","89792616-4980723","89792892-4980724","75480478-4980725","75480470-4980726","75480474-4980727","67633244-4949819","85393028-4974285","85393094-4950036","85393218-4950033","78744426-4924940","85393106-4974292","59258338-4955555","73485834-4977726","73485850-4977728","47344118-1551960","75494126-4961557","83980420-4964325","83980436-4964326","83980456-4964327","78746116-3044544","89793298-4605830","95692196-4922799","78746100-1470887","92731810-4205631","96383366-4999213"})
    void duKey_linken_todo(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_linken_todo",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {})
    @Disabled
    void do_delete_develop_tmp(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_linken_todo",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"89321734-4989679","89322064-4989679","89322272-4989680","92893926-1363472","78744646-1363472","78745562-1371733","78745834-1363472","78744114-4806374","78746060-4806374","78744180-1529969","78744276-1529969","78744280-1529969","78744390-1529969","78744398-1529969","78744406-1529969","78744462-1529969","78745000-1529969","78745008-1529969","94555374-4955555","90765562-1372038","90765214-1372047","90765072-1372051","95339324-4995537","78744850-1530634","78745060-1530634","78743988-1530634","85393150-4974285","78744422-4924940","78744596-4924940","78745446-4924940","78745954-4924940","78746912-4924940","85393150-4974292","94555942-4955566","59258338-4995660","78744280-1529970","78744280-1529996","78744280-1529997","78744280-1529998","78744280-1529999","78744280-1530001","85392912-1377490","73485850-4977726","89927678-1423640","95898698-1369974"})
    void duKey_nicht_linken_todo(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_nicht_linken_todo",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"78742910-1470833","78742910-1470836"})
    void duKey_nicht_linken_todo_artmixgalerie(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);
        if(apiObject==null) {
            Assertions.assertNull(apiObject);
        }else{
            assertSimilarity("duKey_nicht_linken_todo_",
                    gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, false);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"93018772-4994489","85393094-4950036","85393028-4974285","95115614-4995687","82636600-2984044","94662856-1411659","91714158-4992556","91453266-4996006","94989790-4995437","85393224-4974280","96271614-4970483","91332794-4988367","89793344-4205980","85454902-4986671","91404194-1440603","95956576-4924994","95956552-4924992","96394444-1444110","78744726-4975756","92893346-3084582"})
    void duKey_verlinken_solved(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        assertSimilarity("duKey_verlinken",
                gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"78744422-4245679","78744426-4245679","78744596-4245679","78745450-4245679","78745954-4245679","78746912-4245679","84781930-4913292","78744566-1530750","78746008-1530750","78746012-1530757","78744566-1530757","78746008-1539181","78746012-1539181","95868450-1369974","92266518-1356987", "92266530-1356987", "92266542-1356987", "92266554-1356987", "92266566-1356987", "92266574-1356987", "92266586-1356987", "85393100-4974294","89793114-4955599","89793114-4955615","89793114-4925026","89793114-4925919","89793114-4925978","89793114-4926078","89793114-4931403","89793114-4932584","89793114-4933836","89793114-4934798","89793114-4936808","89793114-4938987","89793114-4938998","89793114-4941368","89793114-4943518","89793114-4943548","89793114-4914901","89793114-4915641","89793114-4916051","89793114-4918628","89793114-3605558","89793114-3605564","89793114-3605577","89793114-3605581","89793114-3605595","89793114-4045567","89793114-4045627","89793114-4045640","89793114-4045709","89793114-4045730","89793114-4065704","89793114-4065924","89793114-4065953","89793114-4085722","89793114-4085763","89793114-4125302","89793114-4146107","89793114-4165161","89793114-4165550","89793114-4205589","89793114-4205603","89793114-4205704","89793114-4205724","89793114-4205897","89793114-4205995","89793114-4425723","89793114-4425759","89793114-4565806","89793114-4565812","89793114-4605816","89793114-4625997","89793114-4626002","89793114-4626005","89793114-4686121","89793114-4706016","89793114-4746099","89793114-4826514","89793114-4826531","89793114-4866629","89793114-4907703","89793114-4909474","89793114-4910695","89793114-4913278"})
    void duKey_nicht_linken_solved(String ids) {
        String[] splittedIds = ids.split("-");
        String duKey = splittedIds[1];
        Map<String, GenericObject> databaseObjects =
                new HsdbDao().getRadioPlays(
                        "WHERE DUKEY = " + duKey
                );

        RadioPlaytypeSimilarity gs = new RadioPlaytypeSimilarity();

        GenericObject apiObject = audiothekObjects.get(splittedIds[0]);

        if(apiObject != null) {
            assertSimilarity("duKey_nichtVerlinken",
                    gs.calcSimilarity(databaseObjects.get(duKey), apiObject), compareValue, false);
        }else{
            Assertions.assertNull(apiObject);
        }
    }

    // --------------------------------------- //

    /**
     * Helfer-Methode zum Laden eines JSON-File
     *
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

    void assertSimilarity(String message, Float value, Float compareValue, boolean greater) {
        if (greater) {
            Assertions.assertTrue(value > compareValue, message + " " + value);
        } else {
            Assertions.assertTrue(value < compareValue, message + " " + value);
        }
    }
}
