package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.compare.generic.GenericSimilarity;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import de.ard.sad.normdb.similarity.model.generic.GenericObjectProperty;
import de.ard.sad.normdb.similarity.model.generic.GenricObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RadioPlaytypeSimilarity extends GenericSimilarity {
    public static Logger LOGGER = LoggerFactory.getLogger(RadioPlaytypeSimilarity.class);

    /**
     * Methode wird aufgerufen nachdem die einzelnen Properties vergleichen wurden. Durch Überschreiben dieser Methode kann nachträglich darauf reagiert werden
     * @param pattern Quellobjekt
     * @param target Zielobjekt
     * @param propertyTypes Property Typen, welche verglichen werden
     * @param similarities Berechnete Ähnlichkeiten zu den einzelnen Property Typen
     */
    @Override
    protected void interceptorAfterPropertyCalculation(GenericObject pattern, GenericObject target, ArrayList<GenricObjectType> propertyTypes, HashMap<GenricObjectType,Float> similarities) {
        Float programSetTitleSim = similarities.get(RadioPlayType.PROGRAMSET_TITLE);
        Float episodeTitleSim = similarities.get(RadioPlayType.EPISODE_TITLE);
        Float titleSim = similarities.get(RadioPlayType.TITLE);
        Float episodeNumberSim = similarities.get(RadioPlayType.EPISODE);
        Float seasonNumberSim = similarities.get(RadioPlayType.SEASON);
        Float personInvolvedSim = similarities.get(RadioPlayType.PERSON_INVOLVED);


        if(isDifferent(titleSim)) {
            //Interceptor - Sonderregel 1 (Wenn Programmtitel identisch und Episodentitel in normalem Titel vorhanden)
            if (isSame(programSetTitleSim)) {
                if (isSame(episodeTitleSim)) {
                    LOGGER.info("Interceptor - Sonderregel 1.1 -> Ignoriere Titelähnlichkeit von " + titleSim);
                    similarities.remove(RadioPlayType.TITLE);
                } else {
                    if (checkEpisodeIsPartOfTitle(pattern, target) ||
                            checkEpisodeIsPartOfTitle(target, pattern)) {
                        LOGGER.info("Interceptor - Sonderregel 1.2 -> Ignoriere Titelähnlichkeit von " + titleSim);
                        similarities.remove(RadioPlayType.TITLE);
                    }
                }
            }

            //Interceptor - Sonderregel 2 (Wenn Programmtitel nicht vorhanden oder identisch und Episodentitel und -nummer übereinstimmen)
            if (isSameOrMissing(programSetTitleSim) && (isSame(episodeTitleSim) && isSame(episodeNumberSim))) {
                if (seasonNumberSim == null || (seasonNumberSim >= 0.99f || seasonNumberSim < 0.0f)) {
                    LOGGER.info("Interceptor - Sonderregel 2 -> Ignoriere Titelähnlichkeit von " + titleSim);
                    similarities.remove(RadioPlayType.TITLE);
                }
            }

            //Interceptor - Sonderregel 3 (Wenn Mitwirkende übereinstimmen, Episodennummer übereinstimmt und Episodentitel in Titel vorkommt
            if (isSame(personInvolvedSim) && isSame(episodeNumberSim)) {
                if (checkEpisodeIsPartOfTitle(pattern, target) ||
                            checkEpisodeIsPartOfTitle(target, pattern)) {
                    LOGGER.info("Interceptor - Sonderregel 3 -> Ignoriere Titelähnlichkeit von " + titleSim);
                    similarities.remove(RadioPlayType.TITLE);
                }
            }

            //Interceptor - Sonderregel 4 (Wenn (keine Staffelinformationen vorhanden oder gleich) und (Programmtitel gleich oder fehlt) und Episodennnummer gelich und Mitwirkende gleich
            if(isSame(seasonNumberSim) || (pattern.getProperties(RadioPlayType.SEASON).size()==0 && target.getProperties(RadioPlayType.SEASON).size()==0)) {
                if(/*isSameOrMissing(programSetTitleSim) &&*/ isSame(episodeNumberSim) && isSame(personInvolvedSim)) {
                    LOGGER.info("Interceptor - Sonderregel 4 -> Ignoriere Titelähnlichkeit von " + titleSim);
                    similarities.remove(RadioPlayType.TITLE);
                }

            }
        }
    }

    private boolean isDifferent(Float value) {
        if(value != null && value < 0.99f)
            return true;
        else
            return false;
    }

    private boolean isSame(Float value) {
        if(value != null && value > 0.99f)
            return true;
        else
            return false;
    }

    private boolean isSameOrMissing(Float value) {
        if(value == null || (value >= 0.99 || value < 0))
            return true;
        else
            return false;
    }

    private boolean checkEpisodeIsPartOfTitle(GenericObject pattern, GenericObject target) {
        for(GenericObjectProperty episodeTitleProperties:pattern.getProperties(RadioPlayType.EPISODE_TITLE)) {
            for(String episodeTitle:episodeTitleProperties.getDescriptions()) {
                for(GenericObjectProperty titleProperties:target.getProperties(RadioPlayType.TITLE)) {
                    for (String title : titleProperties.getDescriptions()) {
                        if(title.contains(episodeTitle))
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public float calcSimilarity(GenericObject hsdbObject, GenericObject audiothekObject) {
        boolean cleanPersonInvolved = false;
        if (null != hsdbObject.getProperties(RadioPlayType.PERSON_INVOLVED)) {
            List<GenericObjectProperty> hspdbPersonInvolved = hsdbObject.getProperties(RadioPlayType.PERSON_INVOLVED);
            if (audiothekObject.getProperties(RadioPlayType.PERSON_INVOLVED) != null)
                audiothekObject.cleanPropertíes(RadioPlayType.PERSON_INVOLVED);
            for (GenericObjectProperty genericObjectProperty : hspdbPersonInvolved) {
                for (String hspdbPersonName : genericObjectProperty.getDescriptions()) {
                    if (hspdbPersonName == null || hspdbPersonName.trim().length() == 0)
                        continue;

                    //Wenn Name in Titel
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.TITLE)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            if (descrition.contains(hspdbPersonName)) {
                                cleanPersonInvolved = true;
                                audiothekObject.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, hspdbPersonName);
                                audiothekObject.addDescriptionProperty(RadioPlayType.TITLE, descrition.replaceAll("/*\\s*" + hspdbPersonName + "\\s*:?|\\s*von?\\s*" + hspdbPersonName + "\\s*/*", "").trim());
                            }
                        }
                    }

                    //Wenn Name in Description
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.DESCRIPTION)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            if (descrition.contains(hspdbPersonName)) {
                                cleanPersonInvolved = true;
                                audiothekObject.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, hspdbPersonName);
                            }
                        }
                    }

                    //Wenn Name in ProgrammSet-Titel
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.PROGRAMSET_TITLE)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            if (descrition.contains(hspdbPersonName)) {
                                cleanPersonInvolved = true;
                                audiothekObject.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, hspdbPersonName);
                                //audiothekObject.addDescriptionProperty(RadioPlayType.PROGRAMSET_TITLE, descrition.replaceAll("/*\\s*"+hspdbPersonName + "\\s*:?|\\s*von?\\s*" + hspdbPersonName+"\\s*/*", "").trim());
                            }
                        }
                    }

                    //Wenn Name in ProgrammSet-Titel
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.PROGRAMSET_DESCRIPTION)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            if (descrition.contains(hspdbPersonName)) {
                                cleanPersonInvolved = true;
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
        } finally {
            if (cleanPersonInvolved) {
                audiothekObject.cleanPropertíes(RadioPlayType.PERSON_INVOLVED);
            }
        }
    }
}
