package systems.pqp.hsdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataHarmonizer {

    private static final Logger LOGGER = LogManager.getLogger(DataHarmonizer.class);

    /**
     * Prüft input date auf format und wandelt es in einheitliches Format um.
     * @param date String
     * @return String
     * @throws DataHarmonizerException wenn format nicht erkannt
     */
    public String date(String date) throws DataHarmonizerException {

        /*Pattern circaYear = Pattern.compile("(circa|ca\\.?)\\s?[0-9]{4}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = circaYear.matcher(date);
        if( matcher.matches() ){
            LOGGER.debug("Circa Year");
            String[] parts = date.split("\\s");
            String year = parts[1];
            String month = "XX";
            String day = "XX";
            return year + "-" + month + "-" + day;
        }*/

        // Daten bereinigen, alles entfernen ("circa" usw), was keine Zahl oder ein Steuerzeichen ist
        date = date.replaceAll("(circa\\s?)|(ca\\.?\\s?)", "");

        Pattern germanDate = Pattern.compile("[0-9]{2}\\.[0-9]{2}\\.[0-9]{4}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = germanDate.matcher(date);
        if( matcher.matches() ){
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("German-Date");
            }
            String[] parts = date.split("\\.");
            String day = parts[0];
            String month = parts[1];
            String year = parts[2];
            return year + "-" + month + "-" + day;
        }

        Pattern shortGermanDate = Pattern.compile("([0-9]{2}\\.)?[0-9]{4}", Pattern.CASE_INSENSITIVE);
        matcher = shortGermanDate.matcher(date);
        if( matcher.matches() ){
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Short German Date");
            }
            String[] parts = date.split("\\.");
            String day = "XX";
            String month;
            String year;
            if( parts.length > 1 ) {
                month = parts[0];
                year = parts[1];
            } else {
                month = "XX";
                year = parts[0];
            }
            return year + "-" + month + "-" + day;
        }

        Pattern longBritishDate = Pattern.compile("([0-9]{4}[-/.]+[0-9]{2}[-/.]+[0-9]{2})", Pattern.CASE_INSENSITIVE);
        matcher = longBritishDate.matcher(date);
        List<String> res = matcher.results().map(MatchResult::group).collect(Collectors.toList());
        if( !res.isEmpty() ){
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("British Date");
            }
            date = res.get(0);
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug(date);
            }
            String[] parts = date.split("-");
            if(parts.length == 1){
               parts = date.split("\\.");
            }
            if(parts.length == 1){
                parts = date.split("/");
            }
            String day = parts[2];
            String month = parts[1];
            String year = parts[0];
            return year + "-" + month + "-" + day;
        }

        throw new DataHarmonizerException(String.format("Date format not supported: %s", date));
    }

}
