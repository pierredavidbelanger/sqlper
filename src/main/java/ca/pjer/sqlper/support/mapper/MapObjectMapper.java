package ca.pjer.sqlper.support.mapper;

import ca.pjer.sqlper.MappingMetaData;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MapObjectMapper extends ObjectMapper<Map> {

    private final Comparator<String> keyComparator;

    public MapObjectMapper() {
        this(null);
    }

    public MapObjectMapper(Comparator<String> keyComparator) {
        this.keyComparator = keyComparator;
    }

    @Override
    protected Map newInstance(MappingMetaData metaData, Class<Map> type) throws Exception {
        if (type.isInterface()) {
            return keyComparator != null ? new TreeMap<>(keyComparator) : new HashMap(metaData.getCount());
        }
        // TODO: the requested concrete Map impl type may not have an initial capacity ctor here!
        return type.getConstructor(int.class).newInstance(metaData.getCount());
    }

    @Override
    protected Class getPropertyType(MappingMetaData metaData, Map object, String name, int index) throws Exception {
        Object value = object.get(name);
        return value == null ? null : value.getClass();
    }

    @Override
    protected Object getPropertyValue(MappingMetaData metaData, Map object, String name, int index) throws Exception {
        return object.get(name);
    }

    @Override
    protected void setPropertyValue(MappingMetaData metaData, Map object, String name, int index, Object value) throws Exception {
        object.put(name, value);
    }
}
