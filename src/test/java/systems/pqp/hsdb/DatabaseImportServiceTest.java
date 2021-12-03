package systems.pqp.hsdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class DatabaseImportServiceTest {

    String xml = "<VOLLINFO><KAT>Buch</KAT>\n" +
            "         <AUT>Nicola Manzari</AUT>\n" +
            "         <UEB>Elfriede Mechnig</UEB>\n" +
            "         <RHTI>Partie zu viert</RHTI>\n" +
            "         <LITV>Partie zu viert (Schauspiel, italienisch)</LITV>\n" +
            "         <SPR>\n" +
            "            <NAM rolle=\"Marco Vetti\">Otto Eduard \"O. E.\" Hasse</NAM>\n" +
            "            <NAM rolle=\"Richard\">Ottokar Runze</NAM>\n" +
            "            <NAM rolle=\"Mathilde\">Friedel Schuster</NAM>\n" +
            "            <NAM rolle=\"Maria\">Sigrid Lagemann</NAM>\n" +
            "            <NAM rolle=\"Claretta\">Ilse Kiewiet</NAM>\n" +
            "         </SPR>\n" +
            "         <REG>Rolf von Sydow</REG>\n" +
            "         <GAT>Hörspielbearbeitung</GAT>\n" +
            "         <ESD>04.06.1952</ESD>\n" +
            "         <DAU>69'33</DAU>\n" +
            "         <PROD>RIAS Berlin 1952</PROD>\n" +
            "         <KOLL>Hörspiele nach 1945</KOLL></VOLLINFO>";

    @Test
    public void getDurationInSeconds() {
        /*
         * Test für VollInfoBean-Methode getDurationInSeconds()
         */
        DatabaseImportService.VollinfoBean bean = new DatabaseImportService.VollinfoBean();
        bean.setDuration("10"); // 10 minutes
        Assert.assertEquals("Only Minutes in Seconds", 600F, bean.getDurationInSeconds(),0F);
        bean.setDuration("5'40"); // 5 minutes 40 seconds -> 340s
        Assert.assertEquals("Minutes + Seconds", 340F, bean.getDurationInSeconds(),0F);
        bean.setDuration("Ca. 45"); // ca 45 minutes
        Assert.assertEquals("Weird duration", 2700F, bean.getDurationInSeconds(),0F);
    }

    @Test
    public void beanFromXmlString() throws JsonProcessingException {
        DatabaseImportService databaseImportService = new DatabaseImportService();
        DatabaseImportService.VollinfoBean bean = databaseImportService.beanFromXmlString(xml);
        Assert.assertNotNull(bean);
        System.out.println(bean);
    }

    @Test
    public void getRadioPlays() {
        DatabaseImportService databaseImportService = new DatabaseImportService();
        Map<String,GenericObject> result = databaseImportService.getRadioPlays();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void genericObjectFromBean() throws JsonProcessingException {
        String id = "123";
        String uniqueId = "1234";

        DatabaseImportService databaseImportService = new DatabaseImportService();
        DatabaseImportService.VollinfoBean bean = databaseImportService.beanFromXmlString(xml);

        GenericObject radioPlay = databaseImportService.genericObjectFromBean(id, bean);
        Assert.assertNotNull(radioPlay);
        System.out.println(radioPlay);
    }
}
