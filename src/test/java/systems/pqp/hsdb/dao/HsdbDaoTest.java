package systems.pqp.hsdb.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.junit.Assert;
import org.junit.Test;
import systems.pqp.hsdb.SimilarityBean;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HsdbDaoTest {

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
        HsdbDao.VollinfoBean bean = new HsdbDao.VollinfoBean();
        bean.setDuration("10"); // 10 minutes
        Assert.assertEquals("Only Minutes in Seconds", 600F, bean.getDurationInSeconds(),0F);
        bean.setDuration("5'40"); // 5 minutes 40 seconds -> 340s
        Assert.assertEquals("Minutes + Seconds", 340F, bean.getDurationInSeconds(),0F);
        bean.setDuration("Ca. 45"); // ca 45 minutes
        Assert.assertEquals("Weird duration", 2700F, bean.getDurationInSeconds(),0F);
    }

    @Test
    public void beanFromXmlString() throws JsonProcessingException {
        HsdbDao hsdbDao = new HsdbDao();
        HsdbDao.VollinfoBean bean = hsdbDao.beanFromXmlString(xml);
        Assert.assertNotNull(bean);
        System.out.println(bean);
    }

    @Test
    public void getRadioPlays() {
        HsdbDao hsdbDao = new HsdbDao();
        Map<String,GenericObject> result = hsdbDao.getRadioPlays();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void genericObjectFromBean() throws JsonProcessingException {
        String id = "123";

        HsdbDao hsdbDao = new HsdbDao();
        HsdbDao.VollinfoBean bean = hsdbDao.beanFromXmlString(xml);

        GenericObject radioPlay = hsdbDao.genericObjectFromBean(id, bean);
        Assert.assertNotNull(radioPlay);
        System.out.println(radioPlay);
    }

    @Test
    public void upsertMany(){
        HsdbDao dao = new HsdbDao();
        SimilarityBean bean1 = new SimilarityBean();
        bean1.setAudiothekId("autid 1");
        bean1.setAudiothekLink("link 1");
        bean1.setDukey("1");
        Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        bean1.setValidationDateTime(ldt);

        SimilarityBean bean2 = new SimilarityBean();
        bean2.setAudiothekId("autid 2");
        bean2.setAudiothekLink("link 2");
        bean2.setDukey("2");
        instant = Instant.ofEpochMilli(System.currentTimeMillis());
        ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        bean2.setValidationDateTime(ldt);

        SimilarityBean bean3 = new SimilarityBean();
        bean3.setAudiothekId("autid 3");
        bean3.setAudiothekLink("link 3");
        bean3.setDukey("3");
        instant = Instant.ofEpochMilli(System.currentTimeMillis());
        ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        bean3.setValidationDateTime(ldt);

        List<SimilarityBean> similarityBeans = List.of(
                bean1, bean2, bean3
        );

        dao.upsertMany(similarityBeans);
    }
}
