package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.compare.generic.GenericSimilarity;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import systems.pqp.hsdb.dao.HsdbDao;

import java.util.List;

public class SimilarityCheck {

    private static GenericSimilarity similarityTest = new GenericSimilarity();
    private Config config = Config.Config();

    public SimilarityCheck(){}

    /**
     * Berechnet die Gleichheit zweier GenericObject-Objekte.
     * @param hsdbObject GenericObject, Objekt aus HSDB-Datenbank
     * @param audiothekObject GenericObject, Objekt aus Audiothek
     * @return boolean, true wenn gleich
     */
    public boolean checkSimilarity(GenericObject hsdbObject, GenericObject audiothekObject){
        float similarity = similarityTest.calcSimilarity(hsdbObject, audiothekObject);
        return Float.parseFloat(config.getProperty(Config.THRESHOLD)) <= similarity;
    }

    public void mapSimilarities(List<GenericObject> hsdbObjects, List<GenericObject> audiothekObjects){
        HsdbDao dao = new HsdbDao();
    }

}
