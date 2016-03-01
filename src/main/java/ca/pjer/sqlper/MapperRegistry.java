package ca.pjer.sqlper;

public interface MapperRegistry {

    <T> void register(Class<? extends T> type, Mapper<T> mapper);

    <T> Mapper<T> find(Class<T> type);

}
