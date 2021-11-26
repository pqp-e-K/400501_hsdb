package systems.pqp.hsdb;

public class ImportException extends Throwable {

    public ImportException(String message){
        super(message);
    }

    public ImportException(String message, Throwable throwable){
        super(message, throwable);
    }
}
