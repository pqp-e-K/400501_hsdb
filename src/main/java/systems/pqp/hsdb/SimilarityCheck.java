package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import systems.pqp.hsdb.dao.HsdbDao;
import systems.pqp.hsdb.types.RadioPlayType;
import systems.pqp.hsdb.types.RadioPlaytypeSimilarity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimilarityCheck {

    private Config config = Config.Config();
    private static final Logger LOG = LogManager.getLogger(SimilarityCheck.class.getName());

    List<Similarity> foundSimilarities = null;
    List<GenericObject> audiothekObjectsWithoutMatch = null;
    Map<String, Integer> state = Collections.synchronizedMap(new HashMap<>());
    int total = 0;
    long startTime = System.currentTimeMillis();

    synchronized void updateState(String key) {
        if (state.containsKey(key)) {
            state.replace(key, state.get(key) + 1);
        } else {
            state.put(key, 1);
        }

        logStatus();
    }

    synchronized void logStatus() {
        float duration = (System.currentTimeMillis() - startTime) / 1000.0f;
        Integer totalProcessed = state.get("Total");

        StringBuilder message = new StringBuilder();
        message.append("\n##### Status #####\n");
        message.append("Total");
        message.append(": ");
        message.append(totalProcessed);
        message.append("/").append(total);
        message.append(" seit ");
        message.append(duration);
        message.append(" Sekunden bei ⌀ ");
        message.append((totalProcessed / (duration / 60.0f)));
        message.append(" pro Minute");
        message.append("\n");
        for (Map.Entry<String, Integer> s : state.entrySet()) {
            if (!s.getKey().equals("Total")) {
                message.append(s.getKey());
                message.append(": ");
                message.append(s.getValue());
                message.append("\n");
            }
        }
        LOG.info(message);
    }


    public SimilarityCheck() {}

    /**
     * Berechnet die Gleichheit zweier GenericObject-Objekte.
     *
     * @param hsdbObject      GenericObject, Objekt aus HSDB-Datenbank
     * @param audiothekObject GenericObject, Objekt aus Audiothek
     * @return boolean, true wenn gleich
     */
    public boolean checkSimilarity(RadioPlaytypeSimilarity similarityTest, GenericObject hsdbObject, GenericObject audiothekObject) {
        return Float.parseFloat(config.getProperty(Config.THRESHOLD)) <= similarityTest.calcSimilarity(hsdbObject, audiothekObject);
    }

    /**
     * @param hsdbObjects
     * @param audiothekObjects
     * @param numThreads
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void mapSimilarities(Map<String, GenericObject> hsdbObjects, Map<String, GenericObject> audiothekObjects, int numThreads) throws ExecutionException, InterruptedException {

        state.put("Total", 0);
        foundSimilarities = Collections.synchronizedList(new ArrayList<>());
        audiothekObjectsWithoutMatch = Collections.synchronizedList(new ArrayList<>());

        LOG.info("Starte Vergleich für {} Audiothek-Objekte", audiothekObjects.size());
        total = audiothekObjects.size();
        IntegerBucketingCache cache = new IntegerBucketingCache(hsdbObjects, RadioPlayType.DURATION);

        ForkJoinPool threads = new ForkJoinPool(numThreads);
        LOG.info("Initialisierte Threads: {}", threads.getParallelism());
        startTime = System.currentTimeMillis();
        try {
            threads.submit(
                    () -> audiothekObjects.values().parallelStream().forEach(
                            audiothekObject -> map(audiothekObject.getUniqIdwithinDomain(), audiothekObject, cache)
                    )
            ).get();
        } finally {
            threads.shutdown();
        }

        LOG.info("Similarities: {}", foundSimilarities.size());
        if (!foundSimilarities.isEmpty()) {
            //Matchings in Datenbank speichern
            HsdbDao dao = new HsdbDao();
            dao.upsertMany(foundSimilarities);
        }

        LOG.info("ARD Audiothek-Einträge ohne HSPDB Matching: {}", audiothekObjectsWithoutMatch.size());
        if (!audiothekObjectsWithoutMatch.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            try(BufferedWriter writer = new BufferedWriter(new FileWriter("noMatch_"+sdf.format(new Date())+".csv"))) {
                for(GenericObject go:audiothekObjectsWithoutMatch) {
                    if(go != null) {
                        try {

                            String title = go.getProperties(RadioPlayType.TITLE).get(0).getDescriptions().get(0).replaceAll("\\|", "");
                            String link = go.getProperties(RadioPlayType.LINK).get(0).getDescriptions().get(0);
                            writer.write(go.getUniqIdwithinDomain() + "|" + title + "|" + link);
                            writer.newLine();
                        }catch (Exception e){
                            LOG.error("Fehler beim Protokollieren eines Audiothek Eintrages mit der ID "+go.getUniqIdwithinDomain());
                        }
                    }else {
                        LOG.error("Fehler beim Protokollieren eines Audiothek Eintrages ohne HSPDB Matching");
                    }
                }
            }
            catch(IOException e){
                LOG.error("Fehler beim Protokollieren der Audiothek Einträge ohne HSPDB Matching");
            }
        }

    }

    /**
     *
     */
    public void map(String audiothekId, GenericObject audiothekObject, IntegerBucketingCache cache) {
        Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        RadioPlaytypeSimilarity similarityTest = new RadioPlaytypeSimilarity();

        List<GenericObject> hsdbBucket = cache.searchByNumeric(audiothekObject.getProperties(RadioPlayType.DURATION).get(0).getDescriptions().get(0), 0.12f);

        AtomicBoolean matchFound = new AtomicBoolean(false);

        hsdbBucket.forEach(
                hsdbGenericObject -> {
                    float similarity = similarityTest.calcSimilarity(hsdbGenericObject, audiothekObject);
                    if (Float.parseFloat(config.getProperty(Config.THRESHOLD)) <= similarity) {
                        Similarity similarityBean = new Similarity();
                        similarityBean.setDukey(hsdbGenericObject.getUniqIdwithinDomain());
                        similarityBean.setAudiothekId(audiothekId);
                        similarityBean.setScore(similarity);
                        similarityBean.setValidationDateTime(ldt);
                        String link = audiothekObject.getProperties(RadioPlayType.LINK).get(0).getDescriptions().get(0);
                        similarityBean.setAudiothekLink(link);
                        foundSimilarities.add(similarityBean);
                        matchFound.set(true);
                    }
                }
        );

        if(matchFound.get()==false)
            audiothekObjectsWithoutMatch.add(audiothekObject);

        updateState(Thread.currentThread().getName());
        updateState("Total");
    }
}
