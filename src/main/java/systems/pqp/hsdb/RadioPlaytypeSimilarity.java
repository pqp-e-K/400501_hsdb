package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.compare.generic.GenericSimilarity;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import de.ard.sad.normdb.similarity.model.generic.GenericObjectProperty;
import de.ard.sad.normdb.similarity.model.generic.types.RadioPlayType;

import java.util.List;

public class RadioPlaytypeSimilarity extends GenericSimilarity {

    public float calcSimilarity(GenericObject hsdbObject, GenericObject audiothekObject) {


        List<GenericObjectProperty> hspdbPersonInvolved = hsdbObject.getProperties(RadioPlayType.PERSON_INVOLVED);

        audiothekObject.getProperties(RadioPlayType.PERSON_INVOLVED).clear();
        for(GenericObjectProperty genericObjectProperty:hspdbPersonInvolved){
            String hspdbPersonName = genericObjectProperty.getDescriptions().get(0);

            //Wenn Name in Titel
            for(GenericObjectProperty audiothekGenericObjectProperty:audiothekObject.getProperties(RadioPlayType.TITLE)) {
                for(String descrition:audiothekGenericObjectProperty.getDescriptions()) {
                    if (descrition.contains(hspdbPersonName)) {
                        audiothekObject.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, hspdbPersonName);
                        audiothekObject.addDescriptionProperty(RadioPlayType.TITLE,descrition.replaceAll(hspdbPersonName+":?| von "+hspdbPersonName,"").trim());
                    }
                }
            }

            //Wenn Name in Description
            for(GenericObjectProperty audiothekGenericObjectProperty:audiothekObject.getProperties(RadioPlayType.DESCRIPTION)) {
                for(String descrition:audiothekGenericObjectProperty.getDescriptions()) {
                    if (descrition.contains(hspdbPersonName)) {
                        audiothekObject.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, hspdbPersonName);
                    }
                }
            }

        }

        try {
            return super.calcSimilarity(hsdbObject, audiothekObject);
        }finally {
            audiothekObject.getProperties(RadioPlayType.PERSON_INVOLVED).clear();
            System.out.println(audiothekObject.getProperties(RadioPlayType.PERSON_INVOLVED).size());
        }
    }
}
