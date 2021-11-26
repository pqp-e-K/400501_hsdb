package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ApiImportServiceTest {

    @Test
    public void getRadioPlaysTest() throws ImportException {
        List<GenericObject> result = new ApiImportService().getRadioPlays();
        Assert.assertTrue("Ergebnismenge ist > 0",result.size() > 0);
        System.out.println(result);
    }
}
