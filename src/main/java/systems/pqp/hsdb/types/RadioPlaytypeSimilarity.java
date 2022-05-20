package systems.pqp.hsdb.types;

import de.ard.sad.normdb.similarity.compare.generic.GenericSimilarity;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import de.ard.sad.normdb.similarity.model.generic.GenericObjectProperty;
import de.ard.sad.normdb.similarity.model.generic.GenricObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.pqp.hsdb.DataExtractor;

import java.util.*;

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
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("Interceptor - Sonderregel 1.1 -> Ignoriere Titelähnlichkeit von " + titleSim);
                    similarities.remove(RadioPlayType.TITLE);
                    return;   //aktuell wird nur der Titel durch die Regeln auf 0 gesetzt, entsprechend brauchen weitere Regeln nicht geprüft werden
                } else {
                    if (/*checkEpisodeIsPartOfTitle(pattern, target) ||*/
                            checkEpisodeIsPartOfTitle(target, pattern)) {
                        if(LOGGER.isDebugEnabled())
                            LOGGER.debug("Interceptor - Sonderregel 1.2 -> Ignoriere Titelähnlichkeit von " + titleSim);
                        similarities.remove(RadioPlayType.TITLE);
                        return;   //aktuell wird nur der Titel durch die Regeln auf 0 gesetzt, entsprechend brauchen weitere Regeln nicht geprüft werden
                    }
                }
            }

            //Interceptor - Sonderregel 2 (Wenn Programmtitel nicht vorhanden oder identisch und Episodentitel und -nummer übereinstimmen)
            if (isSameOrMissing(programSetTitleSim) && (isSame(episodeTitleSim) && isSame(episodeNumberSim))) {
                if (seasonNumberSim == null || (seasonNumberSim >= 0.99f || seasonNumberSim < 0.0f)) {
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("Interceptor - Sonderregel 2 -> Ignoriere Titelähnlichkeit von " + titleSim);
                    similarities.remove(RadioPlayType.TITLE);
                    return;   //aktuell wird nur der Titel durch die Regeln auf 0 gesetzt, entsprechend brauchen weitere Regeln nicht geprüft werden
                }
            }

            //Interceptor - Sonderregel 3 (Wenn Mitwirkende übereinstimmen, Episodennummer übereinstimmt und Episodentitel in Titel vorkommt
            if (isSame(personInvolvedSim) && isSame(episodeNumberSim)) {
                if (isSame(episodeTitleSim)/*checkEpisodeIsPartOfTitle(pattern, target)*/ ||
                            checkEpisodeIsPartOfTitle(target, pattern)) {
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("Interceptor - Sonderregel 3 -> Ignoriere Titelähnlichkeit von " + titleSim);
                    similarities.remove(RadioPlayType.TITLE);
                    return;   //aktuell wird nur der Titel durch die Regeln auf 0 gesetzt, entsprechend brauchen weitere Regeln nicht geprüft werden
                }
            }

            //Interceptor - Sonderregel 4 (Wenn Mitwirkende übereinstimmen, Episodennummer übereinstimmt und Episodentitel in Titel vorkommt
            if (isSame(personInvolvedSim) && isSame(episodeTitleSim) && isSame(programSetTitleSim)) {
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("Interceptor - Sonderregel 4 -> Ignoriere Titelähnlichkeit von " + titleSim);
                    similarities.remove(RadioPlayType.TITLE);
                    return;   //aktuell wird nur der Titel durch die Regeln auf 0 gesetzt, entsprechend brauchen weitere Regeln nicht geprüft werden
            }

            //Interceptor - Sonderregel 5 (Wenn (keine Staffelinformationen vorhanden oder gleich) und (Programmtitel gleich) und Episodennnummer gleich und Mitwirkende gleich
            if(isSame(seasonNumberSim) || (pattern.getProperties(RadioPlayType.SEASON).size()==0 && target.getProperties(RadioPlayType.SEASON).size()==0)) {
                if(isSame(programSetTitleSim) && isSame(episodeNumberSim) && isSame(personInvolvedSim)) {
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("Interceptor - Sonderregel 5 -> Ignoriere Titelähnlichkeit von " + titleSim);
                    similarities.remove(RadioPlayType.TITLE);
                    return;   //aktuell wird nur der Titel durch die Regeln auf 0 gesetzt, entsprechend brauchen weitere Regeln nicht geprüft werden
                }
            }

            //Interceptor - Sonderregel 6
            if(isSame(seasonNumberSim) || (pattern.getProperties(RadioPlayType.SEASON).size()==0 && target.getProperties(RadioPlayType.SEASON).size()==0)) {
                if(isSameOrMissing(programSetTitleSim) && isSame(episodeNumberSim) && isSameOrMissing(personInvolvedSim)) {
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("Interceptor - Sonderregel 6 -> Ignoriere Titelähnlichkeit von " + titleSim);
                    similarities.remove(RadioPlayType.TITLE);
                    return;   //aktuell wird nur der Titel durch die Regeln auf 0 gesetzt, entsprechend brauchen weitere Regeln nicht geprüft werden
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
        if(value != null && value >= 0.99f)
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
                episodeTitle = DataExtractor.getBasicString(episodeTitle); //Sonderzeichen entfernen für Vergleich
                for(GenericObjectProperty titleProperties:target.getProperties(RadioPlayType.TITLE)) {
                    for (String title : titleProperties.getDescriptions()) {
                        if(title != null && DataExtractor.getBasicString(title).contains(episodeTitle))            //Sonderzeichen entfernen aus Titel für Vergleich (TODO nicht ideal hier, da mehrfach aufgerufen) & prüfen, ob enthalten in Text
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public float calcSimilarity(GenericObject hsdbObject, GenericObject audiothekObject) {
        boolean cleanPersonInvolved = false;    //aktuell wird der Audiothekinhalt immer überschrieben
        boolean cleanEpisodeTitle = false;
        List<String> backupEpisodeTitleList = DataExtractor.getListOfPropertyDescriptions(audiothekObject,RadioPlayType.EPISODE_TITLE);
        boolean cleanProgramSetTitle = false;
        List<String> backupProgramSetTitleList = DataExtractor.getListOfPropertyDescriptions(audiothekObject,RadioPlayType.PROGRAMSET_TITLE);

        //Gefundene mitwirkende Personen aus HSDB in Audiothek Datensatz suchen
        if (null != hsdbObject.getProperties(RadioPlayType.PERSON_INVOLVED)) {
            List<GenericObjectProperty> hspdbPersonInvolved = hsdbObject.getProperties(RadioPlayType.PERSON_INVOLVED);
            if (audiothekObject.getProperties(RadioPlayType.PERSON_INVOLVED) != null)
                audiothekObject.cleanPropertíes(RadioPlayType.PERSON_INVOLVED);
            for (GenericObjectProperty genericObjectProperty : hspdbPersonInvolved) {
                loop:for (String hspdbPersonName : genericObjectProperty.getDescriptions()) {
                    if (hspdbPersonName == null || hspdbPersonName.trim().length() == 0)
                        continue;

                    //Wenn Name in Titel
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.TITLE)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            if (descrition.contains(hspdbPersonName)) {
                                cleanPersonInvolved = true;
                                audiothekObject.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, hspdbPersonName);
                                audiothekObject.addDescriptionProperty(RadioPlayType.TITLE, descrition.replaceAll("/*\\s*" + hspdbPersonName + "\\s*:?|\\s*von?\\s*" + hspdbPersonName + "\\s*/*", "").trim());
                                //continue loop hier nicht, da andernfalls nicht alle Titelvarianten generiert werden
                            }
                        }
                    }

                    //Wenn Name in Description
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.DESCRIPTION)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            if (descrition.contains(hspdbPersonName)) {
                                cleanPersonInvolved = true;
                                audiothekObject.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, hspdbPersonName);
                                continue loop;
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
                                continue loop;
                            }
                        }
                    }

                    //Wenn Name in ProgrammSet Beschreibung
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.PROGRAMSET_DESCRIPTION)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            if (descrition.contains(hspdbPersonName)) {
                                cleanPersonInvolved = true;
                                audiothekObject.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED, hspdbPersonName);
                                //audiothekObject.addDescriptionProperty(RadioPlayType.TITLE, descrition.replaceAll("/*\\s*"+hspdbPersonName + "\\s*:?|\\s*von?\\s*" + hspdbPersonName+"\\s*/*", "").trim());
                                continue loop;
                            }
                        }
                    }
                }
            }
        }

        //Gefundenen ProgramSet Title aus HSDB in Audiothek Datensatz suchen
        if (null != hsdbObject.getProperties(RadioPlayType.PROGRAMSET_TITLE)) {
            List<GenericObjectProperty> hspdbProgramSet = hsdbObject.getProperties(RadioPlayType.PROGRAMSET_TITLE);
            for (GenericObjectProperty genericObjectProperty : hspdbProgramSet) {
                loop:for (String hspdbProgramSetTitle : genericObjectProperty.getDescriptions()) {
                    if (hspdbProgramSetTitle == null || hspdbProgramSetTitle.trim().length() == 0)
                        continue;

                    String hspdbProgramSetTitleBasic = DataExtractor.getBasicString(hspdbProgramSetTitle);

                    //Wenn Programmset-Titlel aus hspdb ebenfalls im Audiotheks Titel vorkommt -> ProgramSet Title übernehemen
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.TITLE)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            descrition = DataExtractor.getBasicString(descrition);
                            if (backupProgramSetTitleList.contains(hspdbProgramSetTitle) == false && descrition.contains(hspdbProgramSetTitleBasic)) {
                                cleanProgramSetTitle = true;
                                audiothekObject.addDescriptionProperty(RadioPlayType.PROGRAMSET_TITLE, hspdbProgramSetTitle);
                                continue loop;
                            }
                        }
                    }
                }
            }
        }


        //Gefundenen Episodentitel aus HSDB in Audiothek Datensatz suchen
        if (null != hsdbObject.getProperties(RadioPlayType.EPISODE_TITLE)) {
            List<String> hspdbProgramSet = DataExtractor.getListOfPropertyDescriptions(hsdbObject,RadioPlayType.PROGRAMSET_TITLE);
            List<GenericObjectProperty> hspdbEpisode = hsdbObject.getProperties(RadioPlayType.EPISODE_TITLE);
            for (GenericObjectProperty genericObjectProperty : hspdbEpisode) {
                loop:for (String hspdbEpisodeTitle : genericObjectProperty.getDescriptions()) {
                    if (hspdbEpisodeTitle == null || hspdbEpisodeTitle.trim().length() == 0)
                        continue;
                    String hspdbEpisodeTitleBasic = DataExtractor.getBasicString(hspdbEpisodeTitle);
                    boolean specialCondition = false;
                    for(String programSet:hspdbProgramSet) {
                        if (programSet.equals(hspdbEpisodeTitle) == false && programSet.contains(hspdbEpisodeTitle)) {
                            specialCondition = true;
                            break;
                        }
                    }
                    //Wenn Programmset-Titlel aus hspdb ebenfalls im Audiotheks Titel vorkommt -> ProgramSet Title übernehemen
                    for (GenericObjectProperty audiothekGenericObjectProperty : audiothekObject.getProperties(RadioPlayType.TITLE)) {
                        for (String descrition : audiothekGenericObjectProperty.getDescriptions()) {
                            //Sonderbedingung: Wenn gesuchter Episodentitel ebenfalls Bestandteil des Programmtitels ist, dann vorder aus zu prüfendem Audiotheks Titel den Programmtitel entfernen
                            descrition = DataExtractor.getBasicString(descrition);
                            if(specialCondition == true) {
                                for(String programSetTitel:hspdbProgramSet) {
                                    if(programSetTitel != null && programSetTitel.trim().length()>0)
                                        descrition = descrition.replaceAll(DataExtractor.getBasicString(programSetTitel)," ");
                                }
                                descrition = descrition.replaceAll("\\s+", " ").trim();
                            }
                            if (backupEpisodeTitleList.contains(hspdbEpisodeTitle) == false && descrition.contains(hspdbEpisodeTitleBasic)) {
                                cleanEpisodeTitle = true;
                                audiothekObject.addDescriptionProperty(RadioPlayType.EPISODE_TITLE, hspdbEpisodeTitle);
                                continue loop;
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
            if( cleanEpisodeTitle) {
                audiothekObject.cleanPropertíes(RadioPlayType.EPISODE_TITLE);
                audiothekObject.addDescriptionProperty(RadioPlayType.EPISODE_TITLE,new ArrayList<>(backupEpisodeTitleList));
            }
            if( cleanProgramSetTitle) {
                audiothekObject.cleanPropertíes(RadioPlayType.PROGRAMSET_TITLE);
                audiothekObject.addDescriptionProperty(RadioPlayType.PROGRAMSET_TITLE,new ArrayList<>(backupProgramSetTitleList));
            }

        }
    }


}
