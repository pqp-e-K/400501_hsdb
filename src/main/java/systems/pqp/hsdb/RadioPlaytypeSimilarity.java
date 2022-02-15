package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.compare.generic.GenericSimilarity;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import de.ard.sad.normdb.similarity.model.generic.GenericObjectProperty;

import java.util.List;

public class RadioPlaytypeSimilarity extends GenericSimilarity {

    public float calcSimilarity(GenericObject hsdbObject, GenericObject audiothekObject) {

        if(null != hsdbObject.getProperties(RadioPlayType.PERSON_INVOLVED)) {
            List<GenericObjectProperty> hspdbPersonInvolved = hsdbObject.getProperties(RadioPlayType.PERSON_INVOLVED);
            if(audiothekObject.getProperties(RadioPlayType.PERSON_INVOLVED)!=null)
                audiothekObject.getProperties(RadioPlayType.PERSON_INVOLVED).clear();
            for (GenericObjectProperty genericObjectProperty : hspdbPersonInvolved) {
                for(String hspdbPersonName:genericObjectProperty.getDescriptions()) {
                    if (hspdbPersonName == null || hspdbPersonName.trim().length() == 0)
                        continue;

                    //Wenn Name in Titel
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.TITLE)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            if (descrition.contains(hspdbPersonName)) {
                                audiothekObject.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, hspdbPersonName);
                                audiothekObject.addDescriptionProperty(RadioPlayType.TITLE,descrition.replaceAll("/*\\s*"+hspdbPersonName + "\\s*:?|\\s*von?\\s*" + hspdbPersonName+"\\s*/*", "").trim());
                            }
                        }
                    }

                    //Wenn Name in Description
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.DESCRIPTION)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            if (descrition.contains(hspdbPersonName)) {
                                audiothekObject.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, hspdbPersonName);
                            }
                        }
                    }

                    //Wenn Name in ProgrammSet-Titel
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.PROGRAMSET_TITLE)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            if (descrition.contains(hspdbPersonName)) {
                                audiothekObject.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, hspdbPersonName);
                                //audiothekObject.addDescriptionProperty(RadioPlayType.PROGRAMSET_TITLE, descrition.replaceAll("/*\\s*"+hspdbPersonName + "\\s*:?|\\s*von?\\s*" + hspdbPersonName+"\\s*/*", "").trim());
                            }
                        }
                    }

                    //Wenn Name in ProgrammSet-Titel
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.PROGRAMSET_DESCRIPTION)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            if (descrition.contains(hspdbPersonName)) {
                                audiothekObject.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, hspdbPersonName);
                                //audiothekObject.addDescriptionProperty(RadioPlayType.TITLE, descrition.replaceAll("/*\\s*"+hspdbPersonName + "\\s*:?|\\s*von?\\s*" + hspdbPersonName+"\\s*/*", "").trim());
                            }
                        }
                    }
                }
            }
        }

        //Kombination aus Sendungstitel + Episodentitel hinzufügen
        /*if(null != audiothekObject.getProperties(RadioPlayType.PROGRAM_SET_TITLE)) {
            for (GenericObjectProperty sendungGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.PROGRAM_SET_TITLE)) {
                for(String sendungsTitel:sendungGenericObjectProperty.getDescriptions()) {
                    for (GenericObjectProperty episodeGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.TITLE)) {
                        for (String episodeTitel : episodeGenericObjectProperty.getDescriptions()) {
                            audiothekObject.addDescriptionProperty(RadioPlayType.TITLE,sendungsTitel+" "+episodeTitel);
                        }
                    }
                }

            }
        }*/

        //Episode anreichern
        /*for (GenericObjectProperty genericObjectProperty : audiothekObject.getProperties(RadioPlayType.TITLE)) {
            for(String title:genericObjectProperty.getDescriptions()) {
                String episode = dataExtractor.getEpisodeFromTitle(title);
                if(episode != null)
                    audiothekObject.addDescriptionProperty(RadioPlayType.EPISODE,episode);
            }
        }

        for (GenericObjectProperty genericObjectProperty : hsdbObject.getProperties(RadioPlayType.TITLE)) {
            for(String title:genericObjectProperty.getDescriptions()) {
                String episode = dataExtractor.getEpisodeFromTitle(title);
                if(episode != null)
                    hsdbObject.addDescriptionProperty(RadioPlayType.EPISODE,episode);
            }
        }*/

        try {
            return super.calcSimilarity(hsdbObject, audiothekObject);
        }finally {
            audiothekObject.getProperties(RadioPlayType.PERSON_INVOLVED).clear();
        }
    }
}
