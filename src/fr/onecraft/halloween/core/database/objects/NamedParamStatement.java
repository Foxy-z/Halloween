package fr.onecraft.halloween.core.database.objects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author https://gist.github.com/ruseel/e10bd3fee3c2b165044317f5378c7446
 */
public class NamedParamStatement {
    private final PreparedStatement prepStmt;
    private final List<String> fields = new ArrayList<>();

    public NamedParamStatement(Connection conn, String statementWithNames) throws SQLException {
        Pattern findParametersPattern = Pattern.compile("(?<!')(:[\\w]*)(?!')");
        Matcher matcher = findParametersPattern.matcher(statementWithNames);
        while (matcher.find()) {
            fields.add(matcher.group().substring(1));
        }
        prepStmt = conn.prepareStatement(statementWithNames.replaceAll(findParametersPattern.pattern(), "?"));
    }

    public NamedParamStatement(Connection conn, String statementWithNames, int... resultParameters) throws SQLException {
        Pattern findParametersPattern = Pattern.compile("(?<!')(:[\\w]*)(?!')");
        Matcher matcher = findParametersPattern.matcher(statementWithNames);
        while (matcher.find()) {
            fields.add(matcher.group().substring(1));
        }

        prepStmt = conn.prepareStatement(statementWithNames.replaceAll(findParametersPattern.pattern(), "?"), resultParameters);
    }

    public void execute() throws SQLException {
        prepStmt.execute();
    }

    public ResultSet executeQuery() throws SQLException {
        return prepStmt.executeQuery();
    }

    public int executeUpdate() throws SQLException {
        return prepStmt.executeUpdate();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return prepStmt.getGeneratedKeys();
    }

    public void close() throws SQLException {
        prepStmt.close();
    }

    public void bind(String name, String value) throws SQLException {
        if (name.startsWith(":")) name = name.substring(1);
        prepStmt.setString(getIndex(name), value);
    }

    private int getIndex(String name) {
        return fields.indexOf(name) + 1;
    }
}
