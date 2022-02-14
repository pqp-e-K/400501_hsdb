package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenericObject;
import de.ard.sad.normdb.similarity.model.generic.GenericObjectProperty;
import de.ard.sad.normdb.similarity.model.generic.GenricObjectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntegerBucketingCache {

    final HashMap<Integer, List<GenericObject>> cache;

    IntegerBucketingCache(Map<String, GenericObject> genericObjectMap, GenricObjectType objectTypeToIndex) {
        this.cache = new HashMap<>(genericObjectMap.size());
        Integer idx;
        boolean exists;

        for(GenericObject genericObject: genericObjectMap.values()){
            exists=false;
            for(GenericObjectProperty properties: genericObject.getProperties(objectTypeToIndex)) {
                for(String index:properties.getDescriptions()) {
                    idx = Math.round(Float.parseFloat(index));
                    List<GenericObject> entries = cache.get(idx);
                    if(entries == null)
                        entries = new ArrayList<>();
                    entries.add(genericObject);

                    cache.put(idx,entries);
                    exists = true;
                }
            }
            //Wenn es nicht existiert, dann unter null aufführen
            if(exists==false) {
                List<GenericObject> entries = cache.get(null);
                if(entries == null)
                    entries = new ArrayList<>();
                entries.add(genericObject);

                cache.put(null,entries);
            }

        }
    }

    public List<GenericObject> searchByNumeric(String searchValue, float variance){
        float searchFloat = Float.parseFloat(searchValue);
        List<GenericObject> result = new ArrayList<>();
        result.addAll(cache.get(null));
        Integer min= Math.round(searchFloat * (1.0f-variance));
        Integer max= Math.round(searchFloat * (1.0f+variance));

        for(Integer i=min;i<=max;i++){
            List<GenericObject> tmp = cache.get(i);
            if(tmp != null)
                result.addAll(tmp);
        }
        return result;
    }
}
