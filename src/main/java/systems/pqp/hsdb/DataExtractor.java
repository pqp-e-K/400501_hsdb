package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenericModel;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import de.ard.sad.normdb.similarity.model.generic.GenericObjectProperty;
import de.ard.sad.normdb.similarity.model.generic.types.RadioPlayType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataExtractor {

    String episodeRegexSearch = "\\s*Folge\\s*:?\\s*(\\d+).*" +
            "|\\s*Flg\\.?\\s*:?\\s*(\\d+).*"+
            "|\\s*Teil\\.?\\s*:?\\s*(\\d+).*"+
            "|\\s*\\(\\s*(\\d+)\\s*\\).*"+
            "|\\s*\\(\\s*(\\d+)\\s*/\\s*\\d+\\s*\\).*"+
            "|(\\d+)\\s*\\.\\s*Folge.*"+
            "|(\\d+)+\\s*\\.\\s*Teil.*";

    String episodeRegexRemove = "\\s*Folge\\s*:?\\s*(\\d+)\\s*:?\\s*" +
            "|\\s*Flg\\.?\\s*:?\\s*(\\d+)"+
            "|\\s*Teil\\.?\\s*:?\\s*(\\d+)"+
            "|\\s*\\(\\s*(\\d+)\\s*\\)"+
            "|\\s*\\(\\s*(\\d+)\\s*/\\s*\\d+\\s*\\)"+
            "|(\\d+)+\\s*\\.\\s*Folge\\s*:?\\s*"+
            "|(\\d+)+\\s*\\.\\s*Teil\\s*:?\\s*";

    String seasonRegexSearch = "\\s*Staffel\\s*:?\\s*(\\d+).*" +
            "|\\s*St\\.?\\s*:?\\s*(\\d+).*"+
            "|(\\d+)+\\s*\\.\\s*Staffel.*";

    String seasonRegexRemove = "\\s*Staffel\\s*:?\\s*(\\d+)" +
            "|\\s*St\\.?\\s*:?\\s*(\\d+)"+
            "|(\\d+)+\\s*\\.\\s*Staffel\\s*:?\\s*";

    Pattern episodePattern = Pattern.compile(episodeRegexSearch.toLowerCase());
    Pattern seasonPattern = Pattern.compile(seasonRegexSearch.toLowerCase());

    public String getEpisodeFromTitle(String title) {
        Matcher matcher = episodePattern.matcher(title.toLowerCase());
        while (matcher.find()) {
            for(int group=1;group<=matcher.groupCount();group++) {
                if(matcher.group(group) != null) {
                    return matcher.group(group);
                }
            }
        }
        return null;
    }

    public String getSeasonFromTitle(String title) {
        Matcher matcher = episodePattern.matcher(title.toLowerCase());
        while (matcher.find()) {
            for(int group=1;group<=matcher.groupCount();group++) {
                if(matcher.group(group) != null) {
                    return matcher.group(group);
                }
            }
        }
        return null;
    }

    public String getTitleWithoutEpisodeOrSeason(String title) {
        String result = title.replaceAll(seasonRegexRemove," ");
        result = result.replaceAll(episodeRegexRemove," ");
        result = result.replaceAll("\\(\\s*\\)", " ");
        result = result.replaceAll("\\(|\\)", " ");
        result = result.replaceAll("\\s+:\\s+"," ");
        return result.replaceAll("\\s+", " ").trim();
    }

    public static Map<String, GenericObject> createVirtualRadioPlayOnProgramSet(Map<String, GenericObject> radioPlays) {
        Map<String, GenericObject> results = new HashMap<>();

        Map<String, List<GenericObject>> programSets = new HashMap<>();
        GenericModel parentModel = new GenericModel(RadioPlayType.class);
        //Suche alle zusammengehörigen Hörspiele
        for(GenericObject radioPlay:radioPlays.values()) {
            parentModel = radioPlay.getParentModel();
            String programSetId = radioPlay.getProperties(RadioPlayType.PROGRAMSET_LINK).get(0).getDescriptions().get(0);
            List<GenericObject> tmp = programSets.get(programSetId);
            if(tmp == null) {
                tmp = new ArrayList<GenericObject>();
            }
            tmp.add(radioPlay);
            programSets.put(programSetId,tmp);
        }

        //erstelle virtuelle Sendungen
        for(String programsetId:programSets.keySet()) {
            List<GenericObject> programSetRadioPlays = programSets.get(programsetId);
            GenericObject virtualRadioPlay = new GenericObject(parentModel,programsetId);
            float duration = 0f;
            for(GenericObject programSetRadioPlay:programSetRadioPlays) {
                //Titel
                for(GenericObjectProperty p:programSetRadioPlay.getProperties(RadioPlayType.TITLE)) {
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.TITLE,p.getDescriptions());
                }
                for(GenericObjectProperty p:programSetRadioPlay.getProperties(RadioPlayType.PROGRAMSET_TITLE)) {
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.TITLE,p.getDescriptions());
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.PROGRAMSET_TITLE,p.getDescriptions());
                }

                //Publisher
                for(GenericObjectProperty p:programSetRadioPlay.getProperties(RadioPlayType.PUBLISHER)) {
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.PUBLISHER,p.getDescriptions());
                }

                //PERSON_INVOLVED
                for(GenericObjectProperty p:programSetRadioPlay.getProperties(RadioPlayType.PERSON_INVOLVED)) {
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.PERSON_INVOLVED,p.getDescriptions());
                }

                //PERSON_INVOLVED
                for(GenericObjectProperty p:programSetRadioPlay.getProperties(RadioPlayType.PERSON_ROLE)) {
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.PERSON_ROLE,p.getDescriptions());
                }

                //DESCRIPTION
                for(GenericObjectProperty p:programSetRadioPlay.getProperties(RadioPlayType.DESCRIPTION)) {
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION,p.getDescriptions());
                }
                for(GenericObjectProperty p:programSetRadioPlay.getProperties(RadioPlayType.PROGRAMSET_DESCRIPTION)) {
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION,p.getDescriptions());
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.PROGRAMSET_DESCRIPTION,p.getDescriptions());
                }

                //SEASON
                for(GenericObjectProperty p:programSetRadioPlay.getProperties(RadioPlayType.SEASON)) {
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.SEASON,p.getDescriptions());
                }

                //Link
                for(GenericObjectProperty p:programSetRadioPlay.getProperties(RadioPlayType.PROGRAMSET_LINK)) {
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.LINK,p.getDescriptions());
                }

                duration = duration + Float.valueOf(programSetRadioPlay.getProperties(RadioPlayType.DURATION).get(0).getDescriptions().get(0));
            }

            //duration
            virtualRadioPlay.addDescriptionProperty(RadioPlayType.DURATION, String.valueOf(duration));

            results.put(programsetId,virtualRadioPlay);
        }
        return results;
    }

    public static Map<String, GenericObject> removeReadings(Map<String, GenericObject> map) {
        HashSet<String> removeList = new HashSet();
        for(String id : map.keySet()) {
            GenericObject object = map.get(id);
            boolean remove = false;
            for(GenericObjectProperty property: object.getProperties(RadioPlayType.TITLE)) {
                for(String description:property.getDescriptions()) {
                    if(description.toLowerCase().contains("lesung") || description.toLowerCase().contains("gelesen")){
                        remove = true;
                        break;
                    }
                }
                if (remove) break;
            }

            for(GenericObjectProperty property: object.getProperties(RadioPlayType.PROGRAMSET_TITLE)) {
                for(String description:property.getDescriptions()) {
                    if(description.toLowerCase().contains("lesung") || description.toLowerCase().contains("gelesen")){
                        remove = true;
                        break;
                    }
                }
                if (remove) break;
            }

            for(GenericObjectProperty property: object.getProperties(RadioPlayType.PROGRAMSET_DESCRIPTION)) {
                for(String description:property.getDescriptions()) {
                    if(description.toLowerCase().contains("lesung") || description.toLowerCase().contains("gelesen")){
                        remove = true;
                        break;
                    }
                }
                if (remove) break;
            }

            for(GenericObjectProperty property: object.getProperties(RadioPlayType.DESCRIPTION)) {
                for(String description:property.getDescriptions()) {
                    if(description.toLowerCase().contains("lesung") || description.toLowerCase().contains("gelesen")){
                        remove = true;
                        break;
                    }
                }
                if (remove) break;
            }
            if(remove==true) removeList.add(id);
        }

        for(String id:removeList) {
            map.remove(id);
        }

        return map;
    }
}