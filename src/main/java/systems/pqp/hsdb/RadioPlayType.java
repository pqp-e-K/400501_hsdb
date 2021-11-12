package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenricObjectType;
import de.ard.sad.normdb.similarity.model.generic.types.BasicType;

public class RadioPlayType extends BasicType {

    public static final GenricObjectType ID = new GenricObjectType("string.id",null);
    public static final GenricObjectType TITLE = new GenricObjectType("string.title", null);
    public static final GenricObjectType DESCRIPTION = new GenricObjectType("string.description", null);
    public static final GenricObjectType DURATION = new GenricObjectType("float.duration",null);
    public static final GenricObjectType PUBLICATION_DT = new GenricObjectType("string.publication_dt",null);
    public static final GenricObjectType LINK_BEST_QUALITY = new GenricObjectType("string.link.best_quality",null);
    public static final GenricObjectType LINK_AUDIOTHEK = new GenricObjectType("string.link.audiothek", null);
}
