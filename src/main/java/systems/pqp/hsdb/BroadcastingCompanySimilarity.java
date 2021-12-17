package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenricObjectType;

import java.util.ArrayList;
import java.util.List;

public class BroadcastingCompanySimilarity implements GenricObjectType.SimAlgorithm{

    public static List<List<String>> bcList = new ArrayList<>(20);

    public BroadcastingCompanySimilarity() {
        bcList.add(List.of("ABC","ABC RADIO NATIONAL","ABC SYDNEY - AUSTRALIAN BROADCASTING CORPORATION"));
        bcList.add(List.of("ARD","ARD.de"));
        bcList.add(List.of("BR","Bayerischer Rundfunk","BAYERISCHER RUNDFUNK","Bayern 2","Bayerischer Rundfunk 2012","Bayerischer Rundfunk 2020","BR Heimat","DEUTSCHE STUNDE IN BAYERN GMBH (MÜNCHEN)"));
        bcList.add(List.of("BBC","BBC DEUTSCHER DIENST"));
        bcList.add(List.of("BERLINER RUNDFUNK","Berliner Rundfunk"));
        bcList.add(List.of("CBS","COLUMBIA BROADCASTING SYSTEM (CBS)"));
        bcList.add(List.of("DLR","Deutschlandradio","DEUTSCHLANDRADIO"));
        bcList.add(List.of("DEUTSCHE WELLE","Deutsche Welle"));
        bcList.add(List.of("DEUTSCHLANDFUNK","Deutschlandfunk","Deutschlandfunk Kultur","DEUTSCHLANDSENDER","DEUTSCHLANDSENDER KULTUR","DS KULTUR","Dlf Nova"));
        bcList.add(List.of("HR","hr","hr-2","hr2-kultur","hr3","Hessischer Rundfunk","HESSISCHER RUNDFUNK"));
        bcList.add(List.of("MDR","MITTELDEUTSCHER RUNDFUNK","MITTELDEUTSCHER RUNDFUNK (1946-1952)","MIRAG - MITTELDEUTSCHE RUNDFUNK AG (LEIPZIG)","Mitteldeutscher Rundfunk","MDR KULTUR"));
        bcList.add(List.of("NDR","Norddeutscher Rundfunk","NORDDEUTSCHER RUNDFUNK","NDR Kultur","NDR 1 Welle Nord"));
        bcList.add(List.of("NWDR","Nordwestdeutscher Rundfunk","NORDWESTDEUTSCHER RUNDFUNK"));
        bcList.add(List.of("ÖRF","Österreichischer Rundfunk","ÖSTERREICHISCHER RUNDFUNK"));
        bcList.add(List.of("RB","Radio Bremen","Bremen Zwei","RadioBremen","RADIO BREMEN"));
        bcList.add(List.of("RBB","rbb","Rundfunk Berlin-Brandenburg","RUNDFUNK BERLIN-BRANDENBURG","SENDER FREIES BERLIN","rbbKultur","radioeins"));
        bcList.add(List.of("SR","SAARLÄNDISCHER RUNDFUNK","Saarländische Rundfunk","SR 2 KulturRadio"));
        bcList.add(List.of("SWF","Südwestfunk Rundfunk"));
        bcList.add(List.of("SWR","SWR1","SWR2","SWR3","SÜDDEUTSCHER RUNDFUNK","SÜDWESTRUNDFUNK","Südwestrundfunk","Südwestrundfunk 2021"));
        bcList.add(List.of("WDR","WDR 3","WDR 5","Westdeutscher Rundfunk","WESTDEUTSCHER RUNDFUNK","1LIVE"));
        bcList.add(List.of("YLEISRADIO","yleisradio"));
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
            for(List<String>bcNames:bcList) {
                for(String bcName:bcNames) {
                    if (element.contains(bcName)) {
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