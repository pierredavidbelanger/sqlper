package ca.pjer.sqlper.support.mapper;

import ca.pjer.sqlper.SqlperException;

public class CharacterConverterMapper extends ConverterMapper<Character, String> {

    public CharacterConverterMapper() {
        super(String.class);
    }

    @Override
    protected String convertToNative(Class<Character> fromType, Character object, Class<String> toType) throws Exception {
        return String.valueOf(object);
    }

    @Override
    protected Character convertFromNative(Class<String> fromType, String object, Class<Character> toType) throws Exception {
        if (object.length() != 1) {
            throw new SqlperException("Found a Character with length != 1: '" + object + "'");
        }
        return object.charAt(0);
    }
}
