package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.compare.basic.date.FuzzyDateSimilarity;
import de.ard.sad.normdb.similarity.compare.basic.string.EqualSimilarity;
import de.ard.sad.normdb.similarity.compare.basic.string.FuzzyStringVariantSimilarity;
import de.ard.sad.normdb.similarity.compare.basic.string.ScaledLevensteinWithPartTest;
import de.ard.sad.normdb.similarity.compare.ndb.name.InstitutionNameSimilarity;
import de.ard.sad.normdb.similarity.compare.ndb.name.PersonNameSimilarity;
import de.ard.sad.normdb.similarity.model.generic.GenricObjectType;
import de.ard.sad.normdb.similarity.model.generic.types.BasicType;

public class RadioPlayType extends BasicType {
    public static final GenricObjectType TITLE = new GenricObjectType("string.title", 0.9f, 0.0f, true, true, true, new FuzzyStringVariantSimilarity(new InstitutionNameSimilarity(new ScaledLevensteinWithPartTest(), 2.0d, 0.5d, true, 0.0f), FuzzyStringVariantSimilarity.OutputSetting.MAX));
    public static final GenricObjectType SHOW_TITLE = new GenricObjectType("string.title.show", 0.9f, 0.0f, false, false, false, new FuzzyStringVariantSimilarity(new InstitutionNameSimilarity(new ScaledLevensteinWithPartTest(), 2.0d, 0.5d, true, 0.0f), FuzzyStringVariantSimilarity.OutputSetting.MAX));
    public static final GenricObjectType LONG_TITLE = new GenricObjectType("string.title.long", 1.0f, 0.0f, false, false, false, new FuzzyStringVariantSimilarity(new InstitutionNameSimilarity(new ScaledLevensteinWithPartTest(), 2.0d, 0.5d, true, 0.0f), FuzzyStringVariantSimilarity.OutputSetting.MAX));
    public static final GenricObjectType BIO = new GenricObjectType("string.bio", 1.0f, 0.0f, false, false, false, new EqualSimilarity());
    public static final GenricObjectType DESCRIPTION = new GenricObjectType("string.description", 1.0f, 0.0f, false, false, false, new EqualSimilarity());
    public static final GenricObjectType DURATION = new GenricObjectType("float.duration", 0.9f, 0.89999f, false, true, true, new FloatPercentageSimilarity());
    public static final GenricObjectType PUBLICATION_DT = new GenricObjectType("string.publication_dt", 0.9f, 0.0f, false, true, false, new FuzzyDateSimilarity());
    public static final GenricObjectType PUBLISHER = new GenricObjectType("string.publisher", 1.0f, 0.0f, false, true, true, new BroadcastingCompanySimilarity());
    public static final GenricObjectType LINK_AUDIOTHEK = new GenricObjectType("string.link.audiothek", 1.0f, 0.0f, false, false, false, new EqualSimilarity());
    public static final GenricObjectType PERSON_INVOLVED = new GenricObjectType("string.person_name.involved", 1.0f, 0.0f, false, true, false, new PersonNameSimilarity());
    public static final GenricObjectType PERSON_ROLE = new GenricObjectType("string.person_name.role", 1.0f, 0.0f, false, false, false, new EqualSimilarity());
}
