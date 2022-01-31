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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
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
        RadioPlaytypeSimilarity similarityTest = new RadioPlaytypeSimilarity();
        int toProcess = audiothekIds.size();
        AtomicInteger processed = new AtomicInteger(0);

        printProgress(System.currentTimeMillis(),toProcess,processed.get());
        audiothekIds.forEach(
                audiothekId -> {
                    hsdbObjects.keySet().forEach(
                            hsdbKey -> {
                                if (checkSimilarity(similarityTest, hsdbObjects.get(hsdbKey), audiothekObjects.get(audiothekId))) {
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
                    processed.addAndGet(1);
                    System.out.println("Finished " + processed.get() + "/" + toProcess);
                    //LOG.info("Partition[{}]: {} fertig", audiothekIds.hashCode(), audiothekId);

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

    /**
     *
     * @param startTime
     * @param total
     * @param current
     */
    private void printProgress(long startTime, long total, long current) {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / current;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies(current == 0 ? (int) (Math.log10(total)) : (int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
                .append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

        System.out.print(string);
    }
}
