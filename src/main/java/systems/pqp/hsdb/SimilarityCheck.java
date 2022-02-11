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

        audiothekIds.forEach(
                audiothekId -> {
                    hsdbObjects.keySet().forEach(
                            hsdbKey -> {
                                float similarity = similarityTest.calcSimilarity(hsdbObjects.get(hsdbKey), audiothekObjects.get(audiothekId));
                                if (Float.parseFloat(config.getProperty(Config.THRESHOLD)) <= similarity) {
                                    SimilarityBean similarityBean = new SimilarityBean();
                                    similarityBean.setDukey(hsdbKey);
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
                    if( (processed.get() / toProcess) == 1 || (processed.get() / toProcess) >= logFrequency && (processed.get() / toProcess) % logFrequency == 0 ) {
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
}
