package fr.onecraft.halloween.core.database.objects;

import fr.onecraft.halloween.core.database.enums.SQLCondition;
import fr.onecraft.halloween.core.database.enums.SQLJoin;
import fr.onecraft.halloween.core.database.enums.SQLOrder;
import fr.onecraft.halloween.core.database.exceptions.DatabaseConnectionException;
import fr.onecraft.halloween.core.database.exceptions.DatabaseQueryException;
import fr.onecraft.halloween.utils.StringUtils;

import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Query extends BaseQuery {
    private final Connection connection;

    public Query(Connection connection) throws DatabaseConnectionException {
        try {
            if (connection == null || connection.isClosed()) {
                throw new DatabaseConnectionException("An error occurred while connecting to database: connection is null or closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.connection = connection;
        this.paramIndex = 0;
    }

    /* FROM */

    public Query from(String table) {
        this.table = checkSafe(table);
        return this;
    }

    /* SELECT */

    public Query select() {
        this.select = "SELECT * FROM";
        return this;
    }

    public Query select(String... items) {
        Arrays.stream(items).forEach(this::checkSafe);
        this.select = "SELECT " + StringUtils.join(", ", Arrays.asList(items)) + " FROM";
        return this;
    }

    /* INSERT */

    public Query insert(String item, int value) {
        return insert(item, String.valueOf(value));
    }

    public Query insert(String item, long value) {
        return insert(item, String.valueOf(value));
    }

    public Query insert(String item, String value) {
        this.insert.put(checkSafe(item), bindParam(value));
        return this;
    }

    public Query insertOrUpdate() {
        this.insertOrUpdate = true;
        return this;
    }

    /* WHERE */

    public Query where(String key, int value) {
        return where(SQLCondition.EQUALS, key, String.valueOf(value));
    }

    public Query where(String key, String value) {
        return where(SQLCondition.EQUALS, key, value);
    }

    public Query where(String key, SubQuery subQuery) throws DatabaseQueryException {
        return where(SQLCondition.EQUALS, key, subQuery);
    }

    public Query where(SQLCondition condition, String key, int value) {
        return where(condition, key, String.valueOf(value));
    }

    public Query where(SQLCondition condition, String key, String value) {
        this.where.add(checkSafe(key) + " " + condition.getOperator() + " " + bindParam(value));
        return this;
    }

    public Query where(SQLCondition condition, String key, SubQuery subQuery) throws DatabaseQueryException {
        this.where.add(checkSafe(key) + " " + condition.getOperator() + " (" + subQuery.build() + ")");
        return this;
    }

    /* HAVING */

    public Query having(String key, int value) {
        return having(SQLCondition.EQUALS, key, String.valueOf(value));
    }

    public Query having(String key, String value) {
        return having(SQLCondition.EQUALS, key, value);
    }

    public Query having(String key, SubQuery subQuery) throws DatabaseQueryException {
        return having(SQLCondition.EQUALS, key, subQuery);
    }

    public Query having(SQLCondition condition, String key, int value) {
        return having(condition, key, String.valueOf(value));
    }

    public Query having(SQLCondition condition, String key, String value) {
        this.having.add(checkSafe(key) + " " + condition.getOperator() + " " + bindParam(value));
        return this;
    }

    public Query having(SQLCondition condition, String key, SubQuery subQuery) throws DatabaseQueryException {
        this.having.add(checkSafe(key) + " " + condition.getOperator() + " (" + subQuery.build() + ")");
        return this;
    }

    /* ORDER */

    public Query order(SQLOrder order, String... tables) {
        Arrays.stream(tables).forEach(this::checkSafe);
        this.order = "ORDER BY " + StringUtils.join(", ", Arrays.asList(tables)) + " " + order.name();
        return this;
    }

    /* LIMIT */

    public Query limit(int limit) {
        if (limit < 0) throw new InvalidParameterException("Limit must be a positive number");

        this.limit = "LIMIT " + limit;
        return this;
    }

    public Query limit(int limit, int offset) {
        if (limit < 0 || offset < 0) throw new InvalidParameterException("Limit and offset must be positive numbers");

        this.limit = "LIMIT " + offset + ", " + limit;
        return this;
    }

    /* GROUP */

    public Query group(String column) {
        this.group = "GROUP by " + checkSafe(column);
        return this;
    }

    /* JOIN */

    public Query join(String table, String firstKey, String secondKey) {
        return join(SQLJoin.LEFT, table, firstKey, secondKey);
    }

    public Query join(SQLJoin join, String table, String firstKey, String secondKey) {
        this.join.add(join.name() + " JOIN " + checkSafe(table) + " ON "
                + checkSafe(this.table) + "." + checkSafe(firstKey) + " = "
                + checkSafe(table) + "." + checkSafe(secondKey));
        return this;
    }

    /* UPDATABLE */

    public Query updatable() {
        this.updatable = true;
        return this;
    }

    /* UTILS */

    protected String bindParam(String param) {
        String key = ":param" + paramIndex;
        args.put(key, param);
        paramIndex++;
        return key;
    }

    /* EXECUTE */

    public ResultSet execute() throws SQLException, DatabaseQueryException {
        String query = makeQuery();

        // execute query
        NamedParamStatement statement = null;
        try {
            if (this.updatable) {
                statement = new NamedParamStatement(
                        this.connection, query,
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_UPDATABLE
                );
            } else {
                statement = new NamedParamStatement(this.connection, query);
            }

            // replace arguments
            for (String arg : args.keySet()) {
                statement.bind(arg, args.get(arg));
            }

            // if query has result
            if (this.select != null) {
                return statement.executeQuery();
            }

            statement.execute();
            return null;
        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                if (statement != null) statement.close();
                if (this.connection != null) this.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int getRow() throws DatabaseQueryException, SQLException {
        String query = makeQuery();

        // execute query
        NamedParamStatement statement = null;
        try {
            statement = new NamedParamStatement(this.connection, query, PreparedStatement.RETURN_GENERATED_KEYS);

            // replace arguments
            for (String arg : args.keySet()) {
                statement.bind(arg, args.get(arg));
            }

            statement.execute();
            ResultSet result = statement.getGeneratedKeys();

            return result.next() ? result.getInt(1) : -1;
        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                if (statement != null) statement.close();
                if (this.connection != null) this.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void delete() throws DatabaseQueryException {
        List<String> parts = new ArrayList<>();
        parts.add("DELETE FROM");
        parts.add(this.table);
        if (!this.where.isEmpty()) {
            parts.add("WHERE");
            parts.add("(" + StringUtils.join(") AND (", this.where) + ")");
        }

        String query = StringUtils.join(" ", parts);
        NamedParamStatement statement = null;
        try {
            statement = new NamedParamStatement(this.connection, query);

            // replace arguments
            for (String arg : args.keySet()) {
                statement.bind(arg, args.get(arg));
            }

            statement.execute();
        } catch (SQLException e) {
            throw new DatabaseQueryException("You have an error in your sql syntax: " + query);
        } finally {
            try {
                if (statement != null) statement.close();
                if (this.connection != null) this.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
