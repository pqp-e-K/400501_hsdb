package systems.pqp.hsdb.dao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;

import systems.pqp.hsdb.ImportException;
import systems.pqp.hsdb.dao.coreapi.V2ApiPage;


public class AudiothekDaoV2Test {

    Logger logger = LoggerFactory.getLogger(AudiothekDaoV2Test.class);
    AudiothekDaoV2 dao = new AudiothekDaoV2();

    @Test
    public void testFetchAll() throws ImportException, IOException, InterruptedException {
        String result = dao.fetchFromCoreV2Api();
        Assertions.assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void testFetchGraphQL() throws InterruptedException, ImportException {
        Object result = dao.getRadioPlayShowsFromGraphQL();
        Assertions.assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void getAllShowAssetPages() throws ImportException {
        List<V2ApiPage> shows = dao.getAllShowAssetPages();
        Assertions.assertNotNull(shows);
    }

    @Test
    public void getRadioPlays() throws ImportException, InterruptedException {
        dao.getRadioPlays();
    }

}
