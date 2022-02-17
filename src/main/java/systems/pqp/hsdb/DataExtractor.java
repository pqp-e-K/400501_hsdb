package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenericModel;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import de.ard.sad.normdb.similarity.model.generic.GenericObjectProperty;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataExtractor {

    static final Pattern numericPattern = Pattern.compile("\\d+");

    static final String episodeRegexSearch = "\\s*Folge\\s*:?\\s*(\\d+).*" +
            "|\\s*Flg\\.?\\s*:?\\s*(\\d+).*"+
            "|\\s*Teil\\.?\\s*:?\\s*(\\d+).*"+
            "|\\s*Teil\\.?\\s*:?\\s*([IVX]+).*"+
            "|\\s*\\(\\s*(\\d+)\\s*\\).*"+
            "|\\s*\\(\\s*(\\d+)\\s*/\\s*\\d+\\s*\\).*"+
            "|(\\d+)\\s*\\.\\s*Folge.*"+
            "|(\\d+)+\\s*\\.\\s*Teil.*"+
            "|(\\d+)\\s*\\.\\s*Fall.*";

    static final String episodeTitleRegexSearch = ".*\\s*Folge\\s*.?\\s*\\d*[/\\d*]*\\s*:\\s*([^()\\[\\]{}]+)[()\\[\\]{}]*"+
            "|.*\\s*Teil\\s*.?\\s*\\d*[/\\d*]*\\s*:\\s*([^()\\[\\]{}]+)[()\\[\\]{}]*"+
            "|.*\\s*Flg\\s*.?\\s*\\d*[/\\d*]*\\s*:\\s*([^()\\[\\]{}]+)[()\\[\\]{}]*"+
            "|.*\\s*Staffel\\s*.?\\s*\\d*[/\\d*]*\\s*:\\s*([^()\\[\\]{}]+)[()\\[\\]{}]*";
         //   "|\\s*Flg\\.?\\s*:?\\s*(\\d+).*"+
        //    "|\\s*Teil\\.?\\s*:?\\s*(\\d+).*"+
        //    "|\\s*Teil\\.?\\s*:?\\s*([IVX]+).*"+
        //    "|\\s*\\(\\s*(\\d+)\\s*\\).*"+
        //    "|\\s*\\(\\s*(\\d+)\\s*/\\s*\\d+\\s*\\).*"+
        //    "|(\\d+)\\s*\\.\\s*Folge.*"+
        //    "|(\\d+)+\\s*\\.\\s*Teil.*"+
        //    "|(\\d+)\\s*\\.\\s*Fall.*";


    static final String episodeRegexRemove = "[,;.]*\\s*Folge\\s*:?\\s*(\\d+)(\\/\\d+)*\\s*:?\\s*" +
            "|[,;.]*\\s*Flg\\.?\\s*:?\\s*(\\d+)"+
            "|[,;.]*\\s*Teil\\.?\\s*:?\\s*(\\d+)"+
            "|[,;.]*\\s*\\(\\s*(\\d+)\\s*\\)"+
            "|[,;.]*\\s*\\(\\s*(\\d+)\\s*/\\s*\\d+\\s*\\)"+
            "|(\\d+)+\\s*\\.\\s*Folge\\s*:?\\s*"+
            "|(\\d+)+\\s*\\.\\s*Teil\\s*:?\\s*";

    static final String seasonRegexSearch = "\\s*Staffel\\s*:?\\s*(\\d+).*" +
            "|\\s*St\\.?\\s*:?\\s*(\\d+).*"+
            "|(\\d+)+\\s*\\.\\s*Staffel.*";

    static final String seasonRegexRemove = "\\s*Staffel\\s*:?\\s*(\\d+)" +
            "|\\s*St\\.?\\s*:?\\s*(\\d+)"+
            "|(\\d+)+\\s*\\.\\s*Staffel\\s*:?\\s*";

    static final Pattern episodePattern = Pattern.compile(episodeRegexSearch.toLowerCase());
    static final Pattern episodeTitlePattern = Pattern.compile(episodeTitleRegexSearch);
    static final Pattern seasonPattern = Pattern.compile(seasonRegexSearch.toLowerCase());

    public static Integer getEpisodeFromTitle(String title) {
        Matcher matcher = episodePattern.matcher(title.toLowerCase());
        while (matcher.find()) {
            for(int group=1;group<=matcher.groupCount();group++) {
                String number = matcher.group(group);
                if(number != null) {
                    if(numericPattern.matcher(number).matches()) {
                        return Integer.valueOf(number);
                    } else {
                        Integer result = 0;
                        for(int i=0;i<number.length();i++) {
                            if('i'==number.charAt(i))
                                result++;
                            else if('v'==number.charAt(i)) {
                                result = result + 5;
                            }else if('x'==number.charAt(i)) {
                                result = result + 10;
                            }
                        }
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public static Integer getSeasonFromTitle(String title) {
        Matcher matcher = seasonPattern.matcher(title.toLowerCase());
        while (matcher.find()) {
            for(int group=1;group<=matcher.groupCount();group++) {
                if(matcher.group(group) != null) {
                    return Integer.valueOf(matcher.group(group));
                }
            }
        }
        return null;
    }

    public static String getEpisodeTitle(String title) {
       //System.out.println(title);
        Matcher matcher = episodeTitlePattern.matcher(title);
        String group;
        while (matcher.find()) {
            for(int i=1;i<=matcher.groupCount();i++) {
                group = matcher.group(i);
               // System.out.println("group:"+group);
                if(group != null) {
                    return group.replaceAll("\\s+", " ").trim();
                }
            }

        }
        return null;
    }

    public static String getTitleWithoutEpisodeOrSeason(String title) {
        String result = title.replaceAll(seasonRegexRemove," ");
        result = result.replaceAll(episodeRegexRemove," ");
        result = result.replaceAll("\\(\\s*\\)", " ");
        result = result.replaceAll("\\(\\s+", "(");
        result = result.replaceAll("\\s+\\)", "(");
    //    result = result.replaceAll("\\(|\\)", " ");
      //  result = result.replaceAll("\\s+:\\s+"," ");
        return result.replaceAll("\\s+", " ").trim();
    }

    public static Map<String, GenericObject> createVirtualRadioPlayOnProgramSet(Map<String, GenericObject> radioPlays) {
        Map<String, GenericObject> results = new HashMap<>();

        Map<String, List<GenericObject>> programSets = new HashMap<>();
        GenericModel parentModel = new GenericModel(RadioPlayType.class);
        //Suche alle zusammengehörigen Hörspiele
        for(GenericObject radioPlay:radioPlays.values()) {
            if(radioPlay.getProperties(RadioPlayType.PROGRAMSET_LINK).size()>0) {
                parentModel = radioPlay.getParentModel();
                String programSetId = radioPlay.getProperties(RadioPlayType.PROGRAMSET_ID).get(0).getDescriptions().get(0);
                List<GenericObject> tmp = programSets.get(programSetId);
                if (tmp == null) {
                    tmp = new ArrayList<>();
                }
                tmp.add(radioPlay);
                programSets.put(programSetId, tmp);
            }
        }

        //erstelle virtuelle Sendungen
        for(String programsetId:programSets.keySet()) {
            List<GenericObject> programSetRadioPlays = programSets.get(programsetId);
            GenericObject virtualRadioPlay = new GenericObject(parentModel,programsetId);
            float duration = 0f;
            for(GenericObject programSetRadioPlay:programSetRadioPlays) {
                //Titel
                /*for(GenericObjectProperty p:programSetRadioPlay.getProperties(RadioPlayType.TITLE)) {
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.TITLE,p.getDescriptions());
                }*/
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
                /*for(GenericObjectProperty p:programSetRadioPlay.getProperties(RadioPlayType.DESCRIPTION)) {
                    virtualRadioPlay.addDescriptionProperty(RadioPlayType.DESCRIPTION,p.getDescriptions());
                }*/
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

    private static boolean checkFilter(String text) {
        text = text.toLowerCase();
        if(text.contains("lesung") ||
                text.contains("es liest")||
                text.contains("es lesen")||
                text.contains("gelesen von")){
            return true;
        }else{
            return false;
        }

    }

    public static Map<String, GenericObject> removeReadings(Map<String, GenericObject> map) {
        HashSet<String> removeList = new HashSet();
        for(String id : map.keySet()) {
            GenericObject object = map.get(id);
            boolean remove = false;
            for(GenericObjectProperty property: object.getProperties(RadioPlayType.TITLE)) {
                for(String description:property.getDescriptions()) {
                    if(checkFilter(description)){
                        remove = true;
                        break;
                    }
                }
                if (remove) break;
            }

            for(GenericObjectProperty property: object.getProperties(RadioPlayType.PROGRAMSET_TITLE)) {
                for(String description:property.getDescriptions()) {
                    if(checkFilter(description)){
                        remove = true;
                        break;
                    }
                }
                if (remove) break;
            }

            for(GenericObjectProperty property: object.getProperties(RadioPlayType.PROGRAMSET_DESCRIPTION)) {
                for(String description:property.getDescriptions()) {
                    if(checkFilter(description)){
                        remove = true;
                        break;
                    }
                }
                if (remove) break;
            }

            for(GenericObjectProperty property: object.getProperties(RadioPlayType.DESCRIPTION)) {
                for(String description:property.getDescriptions()) {
                    if(checkFilter(description)){
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
