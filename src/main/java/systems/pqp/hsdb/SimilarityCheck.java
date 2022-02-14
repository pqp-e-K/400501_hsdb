package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import de.ard.sad.normdb.similarity.model.generic.types.RadioPlayType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import systems.pqp.hsdb.dao.HsdbDao;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;

public class SimilarityCheck {

    private Config config = Config.Config();
    private static final Logger LOG = LogManager.getLogger(SimilarityCheck.class.getName());

    public SimilarityCheck(){}

    /**
     * Berechnet die Gleichheit zweier GenericObject-Objekte.
     * @param hsdbObject GenericObject, Objekt aus HSDB-Datenbank
     * @param audiothekObject GenericObject, Objekt aus Audiothek
     * @return boolean, true wenn gleich
     */
    public boolean checkSimilarity(RadioPlaytypeSimilarity similarityTest, GenericObject hsdbObject, GenericObject audiothekObject){
        return Float.parseFloat(config.getProperty(Config.THRESHOLD)) <= similarityTest.calcSimilarity(hsdbObject, audiothekObject);
    }

    /**
     *
     * @param hsdbObjects
     * @param audiothekObjects
     * @param numThreads
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void mapSimilarities(Map<String, GenericObject> hsdbObjects, Map<String, GenericObject> audiothekObjects, int numThreads) throws ExecutionException, InterruptedException {

        LOG.info("Prozessiere {} Audiothek-Objekte", audiothekObjects.size());

        List<List<String>> partitions = Lists.partition(new ArrayList<>(audiothekObjects.keySet()), audiothekObjects.keySet().size()/numThreads);

        ForkJoinPool threads = new ForkJoinPool();

        try {
            threads.submit(
                    () -> partitions.parallelStream().forEach(
                            partitionKeys -> mapPartition(partitionKeys, hsdbObjects, audiothekObjects)
                    )
            ).get();
        } finally {
            threads.shutdown();
        }

    }

    /**
     *
     * @param audiothekIds
     * @param hsdbObjects
     * @param audiothekObjects
     * @return
     */
    public int mapPartition(List<String> audiothekIds, Map<String, GenericObject> hsdbObjects, Map<String, GenericObject> audiothekObjects){
        LOG.info("Starte worker für Partition[{}]", audiothekIds.hashCode());
        final int logFrequency = 10;
        Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        List<SimilarityBean> foundSimilarities = new ArrayList<>();
        RadioPlaytypeSimilarity similarityTest = new RadioPlaytypeSimilarity();
        int toProcess = audiothekIds.size();
        AtomicInteger processed = new AtomicInteger(0);
        AtomicInteger similiaritiesInPartition = new AtomicInteger(0);
        HsdbDao dao = new HsdbDao();

        IntegerBucketingCache cache = new IntegerBucketingCache(hsdbObjects,RadioPlayType.DURATION);

        audiothekIds.forEach(
                audiothekId -> {
                    List<GenericObject> hsdbBucket = cache.searchByNumeric(audiothekObjects.get(audiothekId).getProperties(RadioPlayType.DURATION).get(0).getDescriptions().get(0),0.2f);
                    LOG.info("Partition[{}]: Starte {}/{} mit {} HSDB Vergleichen", audiothekIds.hashCode(), processed.get()+1, toProcess,hsdbBucket.size());
                    hsdbBucket.forEach(
                    //hsdbObjects.keySet().forEach(
                            hsdbGenericObject -> {
                                float similarity = similarityTest.calcSimilarity(hsdbGenericObject, audiothekObjects.get(audiothekId));
                                if (Float.parseFloat(config.getProperty(Config.THRESHOLD)) <= similarity) {
                                    SimilarityBean similarityBean = new SimilarityBean();
                                    similarityBean.setDukey(hsdbGenericObject.getUniqIdwithinDomain());
                                    similarityBean.setAudiothekId(audiothekId);
                                    similarityBean.setScore(similarity);
                                    similarityBean.setValidationDateTime(ldt);
                                    String link = audiothekObjects.get(audiothekId).getProperties(RadioPlayType.LINK).get(0).getDescriptions().get(0);
                                    similarityBean.setAudiothekLink(link);
                                    foundSimilarities.add(similarityBean);
                                }
                            }
                    );
                    processed.addAndGet(1);
                    if (logStatus(processed.get(), toProcess, logFrequency)) {
                        if(foundSimilarities.isEmpty()){
                            LOG.info("Partition[{}]: Finished {}/{}. Keine Gemeinsamkeiten gefunden! Fahre fort.", audiothekIds.hashCode(), processed.get(), toProcess);
                        } else {
                            LOG.info("Partition[{}]: Finished {}/{}.", audiothekIds.hashCode(), processed.get(), toProcess);
                            LOG.info("Partition[{}]: Aktualisiere {} Gemeinsamkeiten in Datenbank...", audiothekIds.hashCode(), foundSimilarities.size());
                            dao.upsertMany(foundSimilarities);
                            similiaritiesInPartition.getAndAdd(foundSimilarities.size());
                            foundSimilarities.clear();
                            LOG.info("Partition[{}]: Update erfolgreich. Fahre fort.", audiothekIds.hashCode());
                        }
                    }
                }
        );
        if(foundSimilarities.isEmpty()){
            LOG.info("Partition[{}]: Keine Gemeinsamkeiten gefunden! Ende.", audiothekIds.hashCode());
            return 0;
        }

        LOG.info("Partition[{}]: Aktualisiere {} Gemeinsamkeiten in Datenbank...", audiothekIds.hashCode(), foundSimilarities);
        dao.upsertMany(foundSimilarities);
        LOG.info("Partition[{}]: Update erfolgreich. Ende.", audiothekIds.hashCode());
        return similiaritiesInPartition.get() + foundSimilarities.size();
    }

    boolean logStatus(int processed, int toProcess, int logFrequency ){

        if(LOG.isDebugEnabled()) {
            LOG.debug("{} <= 1 == {}", processed, processed <= 1);
            LOG.debug("(({} / {}) * 100 ) = {}", processed, toProcess, ((processed / (float) toProcess) * 100.0));
        }


        return ( processed <= 1 || ((processed / (float)toProcess) * 100.0 ) % logFrequency == 0 );
    }
}
