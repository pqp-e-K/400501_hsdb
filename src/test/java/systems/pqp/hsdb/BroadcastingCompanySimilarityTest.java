package systems.pqp.hsdb;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class BroadcastingCompanySimilarityTest {

    @Test
    public void testEqualsWDR() throws IOException {
        BroadcastingCompanySimilarity bcs = new BroadcastingCompanySimilarity();

        float sim = bcs.calcSimilarity("WDR","WDR");
        Assert.assertEquals(1.0f,sim,0f);
    }

    @Test
    public void testEqualsWDR2() throws IOException {
        BroadcastingCompanySimilarity bcs = new BroadcastingCompanySimilarity();

        float sim = bcs.calcSimilarity("WDR","WDR 3");
        Assert.assertEquals(1.0f,sim,0f);
    }

    @Test
    public void testUnequalsNWDRWDR() throws IOException {
        BroadcastingCompanySimilarity bcs = new BroadcastingCompanySimilarity();

        float sim = bcs.calcSimilarity("Nordwestdeutscher Rundfunk","WDR 3");
        Assert.assertEquals(0.0f,sim,0f);
    }
}
