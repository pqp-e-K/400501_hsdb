package systems.pqp.hsdb.types;

import de.ard.sad.normdb.similarity.model.generic.GenricObjectType.SimAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.pqp.hsdb.DataExtractor;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RadioPlayTypeTitelVariantSimilarity extends RadioPlayTypeTitelBasicStringVariantSimilarity {   //TODO Klassen könnten mittlerweile zusammengeführt werden
    public static Logger LOGGER = LoggerFactory.getLogger(RadioPlayTypeTitelVariantSimilarity.class);
    Pattern p = Pattern.compile("\"([^\"]*)\"");

    public RadioPlayTypeTitelVariantSimilarity(SimAlgorithm basicSimAlgorithm, OutputSetting output) {
        super(basicSimAlgorithm, output);
    }

    public RadioPlayTypeTitelVariantSimilarity(SimAlgorithm basicSimAlgorithm, OutputSetting output, boolean extractLeftSidedPart) {
        super(basicSimAlgorithm, output, extractLeftSidedPart);
    }

    public RadioPlayTypeTitelVariantSimilarity(SimAlgorithm basicSimAlgorithm, OutputSetting output, boolean extractLeftSidedPart, boolean compareVariantsAgainstEachOther) {
        super(basicSimAlgorithm, output, extractLeftSidedPart, compareVariantsAgainstEachOther);
    }

    public Set<String> generateStringVariantsExtended(String text) {
        String tileWithoutEpisodeOrSeason = DataExtractor.getTitleWithoutEpisodeOrSeason(text);
        Set<String> results = new HashSet<>();
        results.add(tileWithoutEpisodeOrSeason);
        results.addAll(super.generateStringVariantsExtended(tileWithoutEpisodeOrSeason));
        //results.addAll(super.generateStringVariantsExtended(text));
        return results;
    }


    //Varianten eines Strings erzeugen
    public Set<String> generateStringVariants(String text) {
        String tileWithoutEpisodeOrSeason = DataExtractor.getTitleWithoutEpisodeOrSeason(text);
        Set<String> results = new HashSet<>();
        //Set<String> results = new HashSet<>(super.generateStringVariants(text));
        results.add(tileWithoutEpisodeOrSeason);
        results.addAll(super.generateStringVariants(tileWithoutEpisodeOrSeason));

        if (text != null) {
            Matcher m = p.matcher(text);
            String tmp;
            while (m.find()) {
                tmp = m.group(1);
                if(text.equals(tmp)==false && results.contains(tmp)==false) {
                    results.add(tmp);
                }
            }
        }

        return results;
    }

    protected float calcSimilarityIntern(String pattern, String target, boolean allowContainCheck) {
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("Filter Compare: "+pattern+" || "+target +" || allowContainCheck:"+allowContainCheck);

        if(pattern != null && target != null) {
            int patternLength= pattern.length();
            int targetLength = target.length();
            float minLength = Math.min(patternLength,targetLength);
            float maxLength = Math.max(patternLength,targetLength);

            if(minLength/maxLength < 0.5f) {
                if(allowContainCheck && checkContains(pattern, target)) {
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("Filter Compare 0 return 0.9f");
                    return 0.9f;
                }else {
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("Filter Compare 1 return 0.0f");
                    return 0.0f;
                }
            }else {
                float result = super.calcSimilarityIntern(pattern, target, allowContainCheck);
                //System.out.println("result="+result);
                if(result<0.90f) {
                    if(allowContainCheck && checkContains(pattern, target)) {
                        if(LOGGER.isDebugEnabled())
                            LOGGER.debug("Filter Compare 3 return 0.9f");
                        return 0.9f;
                    }
                }
                if(LOGGER.isDebugEnabled())
                    LOGGER.debug("No Filter -> return "+result);
                return result;
            }
        }else {
            float result = super.calcSimilarityIntern(pattern, target,allowContainCheck);
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("No Filter -> return "+result);
            return result;
        }
    }

    private boolean checkContains(String pattern, String target){
        if(pattern != null
                && target != null
                && pattern.split(" ").length>2
                && target.split(" ").length>2
                && (pattern.contains(target) || target.contains(pattern))) {
            return true;
        }else {
            return false;
        }
    }
}