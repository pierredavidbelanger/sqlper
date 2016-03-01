package ca.pjer.sqlper;

public interface ParsedSql {

    String getSql();

    String[] getParameterNames();

}
