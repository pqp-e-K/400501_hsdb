package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenricObjectType;

import java.util.ArrayList;
import java.util.List;

public class BroadcastingCompanySimilarity implements GenricObjectType.SimAlgorithm{

    public static List<List<String>> bcList = new ArrayList<>(20);

    public BroadcastingCompanySimilarity() {
        bcList.add(List.of("SR","Saarländische Rundfunk","SR 2 KulturRadio"));
        bcList.add(List.of("WDR","Westdeutscher Rundfunk"));
        bcList.add(List.of("SWF","Südwestfunk Rundfunk"));
        bcList.add(List.of("NDR","Norddeutscher Rundfunk"));
        bcList.add(List.of("DLR","Deutschlandradio"));

        bcList.add(List.of("ÖRF","Österreichischer Rundfunk"));

        bcList.add(List.of("HR","hr","Hessischer Rundfunk","hr2-kultur"));


        bcList.add(List.of("RBB","rbb","Rundfunk Berlin-Brandenburg","rbbKultur"));

        bcList.add(List.of("MDR","Mitteldeutscher Rundfunk","MDR KULTUR"));

        bcList.add(List.of("BR","Bayerischer Rundfunk"));





    }

    public float calcSimilarity(String pattern, String target) {
        List<String> patternSynonyms = createSynonyms(pattern);
        List<String> targetSynonyms = createSynonyms(target);

        float maxSim=-1.0f;

        for(String patternSynonym: patternSynonyms) {
            if(targetSynonyms.contains(patternSynonym)==true) {
                return 1.0f;
            } else if (targetSynonyms.size() == 0) {
                return -1.0f;
            } else if (maxSim == -1.0d)
                maxSim = 0.0f;
        }

        return maxSim;
    }

    private List<String> createSynonyms(String element) {
        ArrayList<String> synonyms= new ArrayList<String>();

        if(element != null) {
            element = element.toLowerCase();
            for(List<String>bcNames:bcList) {
                for(String bcName:bcNames) {
                    if (element.contains(bcName.toLowerCase())) {
                        addAllWithoutDuplicates(bcNames,synonyms);
                    }
                }
            }
        }
        return synonyms;
    }

    private void addAllWithoutDuplicates(List<String> sourceList, List<String> targetList) {
        for(String sourceItem:sourceList){
            if(targetList.contains(sourceItem)==false){
                targetList.add(sourceItem);
            }
        }
    }


}