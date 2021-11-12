package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import de.ard.sad.normdb.similarity.model.generic.GenricObjectType;
import de.ard.sad.normdb.similarity.model.generic.types.BasicType;

public class RadioPlayType extends BasicType {

    public static final GenricObjectType ID = new GenricObjectType("string.id",null);
    public static final GenricObjectType TITLE = new GenricObjectType("string.title", null);
    public static final GenricObjectType DESCRIPTION = new GenricObjectType("string.description", null);
    public static final GenricObjectType LINK_SELF = new GenricObjectType("string.link.self",null);
    public static final GenricObjectType LINK_AUDIOTHEK = new GenricObjectType("string.link.audiothek", null);

    public static final GenricObjectType PUBLICATION_SERVICE_ID = new GenricObjectType("string.pub.id", null);
    public static final GenricObjectType PUBLICATION_SERVICE_GENRE = new GenricObjectType("string.pub.genre", null);
    public static final GenricObjectType PUBLICATION_SERVICE_ORGANIZATION = new GenricObjectType("string.pub.organization", null);
    public static final GenricObjectType PUBLICATION_SERVICE_TITLE = new GenricObjectType("string.pub.title", null);
    public static final GenricObjectType PUBLICATION_SERVICE_LINK_SELF = new GenricObjectType("string.pub.link.self", null);
    public static final GenricObjectType PUBLICATION_SERVICE_LINK_ONLINE = new GenricObjectType("string.pub.link.online", null);
}
