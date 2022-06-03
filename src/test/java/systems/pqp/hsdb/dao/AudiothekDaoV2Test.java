package systems.pqp.hsdb.dao;

import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Map;

import systems.pqp.hsdb.ImportException;


public class AudiothekDaoV2Test {

    AudiothekDaoV2 dao = new AudiothekDaoV2();


    @Test
    public void testFetchGraphQL() throws InterruptedException, ImportException {
        Object result = dao.getRadioPlayShowsFromGraphQL();
        Assertions.assertNotNull(result);
        System.out.println(result);
    }


    @Test
    public void getRadioPlays() throws ImportException, InterruptedException {
        Map<String, GenericObject> plays = dao.getRadioPlays();
        Assertions.assertFalse(plays.isEmpty());
    }

}
