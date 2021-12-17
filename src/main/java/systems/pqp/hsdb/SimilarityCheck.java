package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.compare.generic.GenericSimilarity;
import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.pqp.hsdb.dao.HsdbDao;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import com.google.common.collect.Lists;

public class SimilarityCheck {

    private static GenericSimilarity similarityTest = new GenericSimilarity();
    private Config config = Config.Config();
    private static final Logger LOG = LoggerFactory.getLogger(SimilarityCheck.class.getName());

    public SimilarityCheck(){}

    /**
     * Berechnet die Gleichheit zweier GenericObject-Objekte.
     * @param hsdbObject GenericObject, Objekt aus HSDB-Datenbank
     * @param audiothekObject GenericObject, Objekt aus Audiothek
     * @return boolean, true wenn gleich
     */
    public boolean checkSimilarity(GenericObject hsdbObject, GenericObject audiothekObject){
        float similarity = similarityTest.calcSimilarity(hsdbObject, audiothekObject);
        return Float.parseFloat(config.getProperty(Config.THRESHOLD)) <= similarity;
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
        Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        List<SimilarityBean> foundSimilarities = new ArrayList<>();
        audiothekIds.forEach(
                audiothekId -> {
                    hsdbObjects.keySet().forEach(
                            hsdbKey -> {
                                if (checkSimilarity(hsdbObjects.get(hsdbKey), audiothekObjects.get(audiothekId))) {
                                    SimilarityBean similarityBean = new SimilarityBean();
                                    similarityBean.setDukey(hsdbKey);
                                    similarityBean.setAudiothekId(audiothekId);
                                    similarityBean.setValidationDateTime(ldt);
                                    String link = audiothekObjects.get(audiothekId).getProperties(RadioPlayType.LINK_AUDIOTHEK).get(0).getDescriptions().get(0);
                                    similarityBean.setAudiothekLink(link);
                                    foundSimilarities.add(similarityBean);
                                }
                            }
                    );
                    LOG.info("Partition[{}]: {} fertig", audiothekIds.hashCode(), audiothekId);
                }
        );
        if(foundSimilarities.isEmpty()){
            LOG.info("Partition[{}]: Keine Gemeinsamkeiten gefunden! Ende.", audiothekIds.hashCode());
            return 0;
        }

        LOG.info("Partition[{}]: Aktualisiere {} Gemeinsamkeiten in Datenbank...", audiothekIds.hashCode(), foundSimilarities);
        HsdbDao dao = new HsdbDao();
        dao.upsertMany(foundSimilarities);
        LOG.info("Partition[{}]: Update erfolgreich. Ende.", audiothekIds.hashCode());
        return foundSimilarities.size();
    }

}
