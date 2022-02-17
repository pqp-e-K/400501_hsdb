package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import systems.pqp.hsdb.dao.HsdbDao;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class SimilarityCheck {

    private Config config = Config.Config();
    private static final Logger LOG = LogManager.getLogger(SimilarityCheck.class.getName());

    List<SimilarityBean> foundSimilarities = null;
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
            HsdbDao dao = new HsdbDao();
            dao.upsertMany(foundSimilarities);
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

        hsdbBucket.forEach(
                hsdbGenericObject -> {
                    float similarity = similarityTest.calcSimilarity(hsdbGenericObject, audiothekObject);
                    if (Float.parseFloat(config.getProperty(Config.THRESHOLD)) <= similarity) {
                        SimilarityBean similarityBean = new SimilarityBean();
                        similarityBean.setDukey(hsdbGenericObject.getUniqIdwithinDomain());
                        similarityBean.setAudiothekId(audiothekId);
                        similarityBean.setScore(similarity);
                        similarityBean.setValidationDateTime(ldt);
                        String link = audiothekObject.getProperties(RadioPlayType.LINK).get(0).getDescriptions().get(0);
                        similarityBean.setAudiothekLink(link);
                        foundSimilarities.add(similarityBean);
                    }
                }
        );

        updateState(Thread.currentThread().getName());
        updateState("Total");
    }
}
