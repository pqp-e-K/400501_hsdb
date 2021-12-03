package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenricObjectType;

public class FloatPercentageSimilarity implements GenricObjectType.SimAlgorithm {
    @Override
    public float calcSimilarity(String s, String s1) {
        float a = Float.valueOf(s);
        float b = Float.valueOf(s1);

        if(a > b) {
            return (b / a);
        }else {
            return (a / b);
        }
    }
}
