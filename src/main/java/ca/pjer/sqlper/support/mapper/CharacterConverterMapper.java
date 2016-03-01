package ca.pjer.sqlper.support.mapper;

import ca.pjer.sqlper.SqlperException;

public class CharacterConverterMapper extends ConverterMapper<Character, String> {

    public CharacterConverterMapper() {
        super(String.class);
    }

    @Override
    protected String convert(Class<Character> type, Character object) throws Exception {
        return String.valueOf(object);
    }

    @Override
    protected Character convert(String object) throws Exception {
        if (object.length() != 1) {
            throw new SqlperException("Found a Character with length != 1: '" + object + "'");
        }
        return object.charAt(0);
    }
}
