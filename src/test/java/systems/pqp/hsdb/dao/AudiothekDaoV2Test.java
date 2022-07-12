package systems.pqp.hsdb.dao;

import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Map;

import systems.pqp.hsdb.Config;
import systems.pqp.hsdb.ImportException;


public class AudiothekDaoV2Test {




    @Test
    public void testFetchGraphQL() throws InterruptedException, ImportException {
        AudiothekDaoV2 dao = new AudiothekDaoV2();
        Object result = dao.getRadioPlayShowsFromGraphQL();
        Assertions.assertNotNull(result);
        System.out.println(result);
    }


    @Test
    public void getRadioPlays() throws ImportException, InterruptedException {
        AudiothekDaoV2 dao = new AudiothekDaoV2();
        Map<String, GenericObject> plays = dao.getRadioPlays();
        Assertions.assertFalse(plays.isEmpty());
    }

    @Test
    public void testBadGraphQLUrl() throws ImportException, InterruptedException {

        systems.pqp.hsdb.ImportException thrown = Assertions.assertThrows(systems.pqp.hsdb.ImportException.class, () -> {
            Config config = Config.Config();
            config.setProperty(Config.AUDIOTHEK_GRAPHQL_URL,"https://bad-url.com");
            AudiothekDaoV2 dao = new AudiothekDaoV2();
            Object result = dao.getRadioPlayShowsFromGraphQL();
        });

    }
}
