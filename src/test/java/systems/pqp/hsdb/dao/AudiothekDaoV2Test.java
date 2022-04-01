package systems.pqp.hsdb.dao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import systems.pqp.hsdb.ImportException;


public class AudiothekDaoV2Test {

    Logger logger = LoggerFactory.getLogger(AudiothekDaoV2Test.class);
    AudiothekDaoV2 dao = new AudiothekDaoV2();

    @Test
    public void testFetchAll() throws ImportException, IOException {
        String result = dao.fetchAll();
        Assertions.assertNotNull(result);
        System.out.println(result);
    }

}
