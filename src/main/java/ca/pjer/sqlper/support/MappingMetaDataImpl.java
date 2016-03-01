package ca.pjer.sqlper.support;

import ca.pjer.sqlper.MappingMetaData;

public class MappingMetaDataImpl implements MappingMetaData {

    private final int count;
    private final String[] names;
    private final int[] sqlTypes;

    public MappingMetaDataImpl(int count, String[] names, int[] sqlTypes) {
        this.count = count;
        this.names = names;
        this.sqlTypes = sqlTypes;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public String[] getNames() {
        return names;
    }

    @Override
    public int[] getSqlTypes() {
        return sqlTypes;
    }
}
