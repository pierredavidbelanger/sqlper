package ca.pjer.sqlper.support;

import ca.pjer.sqlper.ParsedSql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternParsedSql implements ParsedSql {

    private String sql;
    private String[] parameterNames;

    public PatternParsedSql(Pattern pattern, String sql) {

        StringBuilder sqlBuilder = new StringBuilder();
        int i = 0;
        List<String> names = new ArrayList<>(10);

        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {

            int paramGroup = Math.max(matcher.groupCount() - 1, 0);
            int nameGroup = Math.max(matcher.groupCount(), 0);

            String name = matcher.group(nameGroup);
            names.add(name);

            sqlBuilder.append(sql.substring(i, matcher.start(paramGroup)));
            sqlBuilder.append("?");
            i = matcher.end(paramGroup);
        }

        sqlBuilder.append(sql.substring(i, sql.length()));

        this.sql = sqlBuilder.toString();
        parameterNames = names.toArray(new String[names.size()]);
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public String[] getParameterNames() {
        return parameterNames;
    }
}
