package ca.pjer.sqlper.support.mapper;

import ca.pjer.sqlper.MappingMetaData;
import ca.pjer.sqlper.SqlperException;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

public class ReflexionObjectMapper<T> extends ObjectMapper<T> {

    private final Comparator<String> nameComparator;
    private final Map<Class, Map<String, PropertyDescriptor>> propertyDescriptorCache;

    public ReflexionObjectMapper() {
        this(null);
    }

    public ReflexionObjectMapper(Comparator<String> nameComparator) {
        this.nameComparator = nameComparator;
        propertyDescriptorCache = Collections.synchronizedMap(new WeakHashMap<Class, Map<String, PropertyDescriptor>>());
    }

    @Override
    protected T newInstance(MappingMetaData metaData, Class<T> type) throws Exception {
        return type.newInstance();
    }

    @Override
    protected Class getPropertyType(MappingMetaData metaData, T object, String name, int index) throws Exception {
        return getPropertyDescriptor((Class<T>) object.getClass(), name).getPropertyType();
    }

    @Override
    protected Object getPropertyValue(MappingMetaData metaData, T object, String name, int index) throws Exception {
        PropertyDescriptor propertyDescriptor = getPropertyDescriptor((Class<T>) object.getClass(), name);
        Method method = propertyDescriptor.getReadMethod();
        return method.invoke(object);
    }

    @Override
    protected void setPropertyValue(MappingMetaData metaData, T object, String name, int index, Object value) throws Exception {
        PropertyDescriptor propertyDescriptor = getPropertyDescriptor((Class<T>) object.getClass(), name);
        Method method = propertyDescriptor.getWriteMethod();
        method.invoke(object, value);
    }

    protected PropertyDescriptor getPropertyDescriptor(Class<T> type, String name) throws Exception {
        Map<String, PropertyDescriptor> props = propertyDescriptorCache.get(type);
        if (props == null) {
            props = nameComparator != null ? new TreeMap<String, PropertyDescriptor>(nameComparator) : new HashMap<String, PropertyDescriptor>();
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(type).getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                props.put(propertyDescriptor.getName(), propertyDescriptor);
            }
            propertyDescriptorCache.put(type, props);
        }
        PropertyDescriptor propertyDescriptor = props.get(name);
        if (propertyDescriptor == null) {
            throw new SqlperException("Property " + name + " not found on " + type);
        }
        return propertyDescriptor;
    }
}
