package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.compare.basic.string.FuzzyStringVariantSimilarity;
import de.ard.sad.normdb.similarity.model.generic.GenricObjectType.SimAlgorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RadioPlayTypeTitelVariantSimilarity extends FuzzyStringVariantSimilarity {

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


    //Varianten eines Strings erzeugen
    public List<String> generateStringVariants(String text) {
        Set<String> results = new HashSet<>(super.generateStringVariants(text));
        results.addAll(super.generateStringVariants(DataExtractor.getTitleWithoutEpisodeOrSeason(text)));
        results.addAll(super.generateStringVariants(text.replaceAll("\\(.*\\)", "").replaceAll("\\s+", " ").trim()));

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

        return new ArrayList<String>(results);
    }

    protected float calcSimilarityIntern(String pattern, String target) {
        //return super.calcSimilarityIntern(pattern, target);
        if(pattern != null && target != null) {
            int patternLength= pattern.length();
            int targetLength = target.length();
            float minLength = Math.min(patternLength,targetLength);
            float maxLength = Math.max(patternLength,targetLength);

            if(minLength/maxLength < 0.5f) {
                //System.out.println(minLength+"/"+maxLength+" Filter Compare: "+pattern+" || "+target);
                return 0.0f;
            }else {
                return super.calcSimilarityIntern(pattern, target);
            }
        }else {
            return super.calcSimilarityIntern(pattern, target);
        }
    }
}