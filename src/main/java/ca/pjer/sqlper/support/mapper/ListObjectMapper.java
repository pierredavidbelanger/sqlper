package ca.pjer.sqlper.support.mapper;

import ca.pjer.sqlper.MappingMetaData;

import java.util.ArrayList;
import java.util.List;

public class ListObjectMapper extends ObjectMapper<List> {

    @Override
    protected List newInstance(MappingMetaData metaData, Class<List> type) throws Exception {
        List object;
        if (type.isInterface()) {
            object = new ArrayList(metaData.getCount());
        } else {
            object = type.getConstructor(int.class).newInstance(metaData.getCount());
        }
        for (int i = 0; i < metaData.getCount(); i++) {
            object.add(null);
        }
        return object;
    }

    @Override
    protected Class getPropertyType(MappingMetaData metaData, List object, String name, int index) throws Exception {
        Object value = object.get(index);
        return value == null ? null : value.getClass();
    }

    @Override
    protected Object getPropertyValue(MappingMetaData metaData, List object, String name, int index) throws Exception {
        return object.get(index);
    }

    @Override
    protected void setPropertyValue(MappingMetaData metaData, List object, String name, int index, Object value) throws Exception {
        object.set(index, value);
    }
}
