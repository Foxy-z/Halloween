package fr.onecraft.halloween.core.database.objects;

import fr.onecraft.halloween.core.database.enums.SQLCondition;
import fr.onecraft.halloween.core.database.exceptions.DatabaseConnectionException;
import fr.onecraft.halloween.core.database.exceptions.DatabaseQueryException;
import fr.onecraft.halloween.utils.StringUtils;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Query extends BaseQuery<Query> {
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

    public Query where(String key, SubQuery subQuery) throws DatabaseQueryException {
        return where(SQLCondition.EQUALS, key, subQuery);
    }

    public Query where(SQLCondition condition, String key, SubQuery subQuery) throws DatabaseQueryException {
        this.where.add(checkSafe(key) + " " + condition.getOperator() + " (" + processSubQuery(subQuery) + ")");
        return this;
    }

    /* HAVING */

    public Query having(String key, SubQuery subQuery) throws DatabaseQueryException {
        return having(SQLCondition.EQUALS, key, subQuery);
    }

    public Query having(SQLCondition condition, String key, SubQuery subQuery) throws DatabaseQueryException {
        this.having.add(checkSafe(key) + " " + condition.getOperator() + " (" + processSubQuery(subQuery) + ")");
        return this;
    }

    /* UPDATE */

    public Query update(String key, int value) {
        return update(key, String.valueOf(value));
    }

    public Query update(String key, String value) {
        this.update.add(checkSafe(key) + " = " + bindParam(value));
        return this;
    }

    /* UPDATABLE */

    public Query updatable() {
        this.updatable = true;
        return this;
    }

    /* UTILS */

    private String processSubQuery(SubQuery subQuery) throws DatabaseQueryException {
        String subQueryStr = subQuery.makeQuery();

        Map<String, String> subQueryArgs = subQuery.getArguments();
        for (String key : subQueryArgs.keySet()) {
            String subKey = "subQuery" + this.paramIndex + "_" + key.substring(1);
            this.args.put(subKey, subQueryArgs.get(key));

            subQueryStr.replace(key, subKey);
        }

        return subQueryStr;
    }

    /* EXECUTE */

    public ResultSet execute() throws SQLException, DatabaseQueryException {
        String query = makeQuery();
        Bukkit.getConsoleSender().sendMessage(query);

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
            for (String arg : this.args.keySet()) {
                statement.bind(arg, this.args.get(arg));
            }

            // if query has result
            if (this.select != null) {
                return statement.executeQuery();
            }

            statement.execute();
            return null;
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(query);
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
