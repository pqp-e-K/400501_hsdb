package systems.pqp.hsdb;

import com.google.gson.Gson;
import de.ard.sad.normdb.similarity.compare.generic.GenericSimilarity;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class SimilarityCheckTest {

    @Test
    public void testSimilarity() throws IOException {
        // Jules Verne Reise von der Erde zum Mond
        // GenericObject aus Database
        List<GenericObject> databaseObjects =
                new DatabaseImportService().getRadioPlays(
                        "WHERE DUKEY = 1444441"
                );
        Assert.assertEquals(1, databaseObjects.size());
        GenericObject dbObject = databaseObjects.get(0);

        // GenericObject aus Api (mocked aus Datei)
        GenericObject apiObject = ApiImportService.genericObjectFromJson(loadJsonFromFile("api-examples/jules-verne-95022544.json"));

        GenericSimilarity gs = new GenericSimilarity();

        Assert.assertEquals(1.0f, gs.calcSimilarity(apiObject, dbObject),0.0f);

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
        List<GenericObject> databaseObjects =
                new DatabaseImportService().getRadioPlays(
                        "WHERE DUKEY = 1372136 OR DUKEY = 1393951 OR DUKEY = 1393952 OR DUKEY = 1393953 OR DUKEY = 1424348"
                );
        Assert.assertEquals(5, databaseObjects.size());

        GenericObject apiObject = ApiImportService.genericObjectFromJson(loadJsonFromFile("api-examples/christa-wolf-94736562.json"));

        GenericSimilarity gs = new GenericSimilarity();

        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(0), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(1), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(2), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(3), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(4), apiObject), 0.0f);

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
        List<GenericObject> databaseObjects =
                new DatabaseImportService().getRadioPlays(
                        "WHERE DUKEY = 1377607 OR DUKEY = 1444949 OR DUKEY = 1466678 OR DUKEY = 1522315"
                );
        Assert.assertEquals(4, databaseObjects.size());

        GenericObject apiObject = ApiImportService.genericObjectFromJson(loadJsonFromFile("api-examples/dostojewski-94663538.json"));

        GenericSimilarity gs = new GenericSimilarity();
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(0), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(1), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(2), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(3), apiObject), 0.0f);
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
        List<GenericObject> databaseObjects =
                new DatabaseImportService().getRadioPlays(
                        "WHERE DUKEY = 1373362 OR DUKEY = 1411923 OR DUKEY = 1477013 OR DUKEY = 1527012 OR DUKEY = 1550580 OR DUKEY = 4949489 OR DUKEY = 4949491 OR DUKEY = 4949492 OR DUKEY = 4987009"
                );
        Assert.assertEquals(9, databaseObjects.size());

        GenericObject apiObject = ApiImportService.genericObjectFromJson(loadJsonFromFile("api-examples/sodom-und-gomorrha-94512976.json"));

        GenericSimilarity gs = new GenericSimilarity();
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(0), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(1), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(2), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(3), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(4), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(5), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(6), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(7), apiObject), 0.0f);
        Assert.assertEquals(1.0f, gs.calcSimilarity(databaseObjects.get(8), apiObject), 0.0f);
    }

    Map loadJsonFromFile(String fileName) throws IOException {
        Gson gson = new Gson();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(fileName);
        assert stream != null;
        Map json = gson.fromJson(new InputStreamReader(stream), Map.class);
        stream.close();
        return json;
    }
}
