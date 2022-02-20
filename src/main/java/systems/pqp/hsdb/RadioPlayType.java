package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.compare.basic.date.FuzzyDateSimilarity;
import de.ard.sad.normdb.similarity.compare.basic.numeric.FloatPercentageSimilarity;
import de.ard.sad.normdb.similarity.compare.basic.string.ContainsSimilarity;
import de.ard.sad.normdb.similarity.compare.basic.string.EqualSimilarity;
import de.ard.sad.normdb.similarity.compare.basic.string.FuzzyStringVariantSimilarity;
import de.ard.sad.normdb.similarity.compare.basic.string.ScaledLevensteinWithPartTest;
import de.ard.sad.normdb.similarity.compare.ndb.institution.BroadcastingCompanySimilarity;
import de.ard.sad.normdb.similarity.compare.ndb.name.InstitutionNameSimilarity;
import de.ard.sad.normdb.similarity.model.generic.GenricObjectType;
import de.ard.sad.normdb.similarity.model.generic.types.BasicType;

public class RadioPlayType extends BasicType {
    public static final GenricObjectType TITLE = new GenricObjectType("string.title", 0.9f, 0.0f, true, true, true, 0.80f,false,20f,new RadioPlayTypeTitelVariantSimilarity(new InstitutionNameSimilarity(new ScaledLevensteinWithPartTest(), 2.0d, 0.5d, true, 0.0f), RadioPlayTypeTitelBasicStringVariantSimilarity.OutputSetting.MAX,true,true));

    public static final GenricObjectType PROGRAMSET_ID = new GenricObjectType("string.id.programset", 1.0f, 0.0f, false, false, false, new EqualSimilarity());
    public static final GenricObjectType PROGRAMSET_LINK = new GenricObjectType("string.link.programset", 1.0f, 0.0f, false, false, false, new EqualSimilarity());
    public static final GenricObjectType PROGRAMSET_TITLE =  new GenricObjectType("string.title.programset", 0.9f, 0.0f, false, false, false, 0.99f,false,20f,new RadioPlayTypeTitelVariantSimilarity(new InstitutionNameSimilarity(new ScaledLevensteinWithPartTest(), 2.0d, 0.5d, true, 0.0f), RadioPlayTypeTitelBasicStringVariantSimilarity.OutputSetting.MAX,true,true)).setForceSimCalc(true);
    public static final GenricObjectType PROGRAMSET_DESCRIPTION =  new GenricObjectType("string.description.programset", 0.9f, 0.0f, false, false, false, 0.7f,false,20f,new EqualSimilarity());
    public static final GenricObjectType BIO = new GenricObjectType("string.bio", 1.0f, 0.0f, false, false, false, new EqualSimilarity());
    public static final GenricObjectType DESCRIPTION = new GenricObjectType("string.description", 1.0f, 0.0f, false, false, false, new EqualSimilarity());
    public static final GenricObjectType DURATION = new GenricObjectType("number.duration", 0.9f, 0.89999f, false, true, true, 0.9f,true,20f,new FloatPercentageSimilarity());
    public static final GenricObjectType PUBLICATION_DT = new GenricObjectType("string.publication_dt", 0.9f, 0.0f, false, true, false,0.9f, new FuzzyDateSimilarity());
    public static final GenricObjectType PUBLISHER = new GenricObjectType("string.publisher", 1.0f, 0.0f, false, true, true, new BroadcastingCompanySimilarity());
    public static final GenricObjectType LINK = new GenricObjectType("string.link", 1.0f, 0.0f, false, false, false, new EqualSimilarity());
    public static final GenricObjectType PERSON_INVOLVED = new GenricObjectType("string.person_name.involved", 1.0f, 0.0f, false, true, false, new EqualSimilarity());
    public static final GenricObjectType PERSON_ROLE = new GenricObjectType("string.person_name.role", 1.0f, 0.0f, false, false, false, new EqualSimilarity());

    public static final GenricObjectType SEASON = new GenricObjectType("number.season", 1.0f, 0.0f, false, false, true,0.9f,true,20f, new EqualSimilarity());
    public static final GenricObjectType EPISODE = new GenricObjectType("number.episode", 1.0f, 0.0f, false, false, true, 0.9f,true,20f,new EqualSimilarity());
    public static final GenricObjectType EPISODE_TITLE = new GenricObjectType("string.title.episode", 0.9f, 0.0f, false, false, true, 0.9f,true,20f,new ContainsSimilarity());

}
