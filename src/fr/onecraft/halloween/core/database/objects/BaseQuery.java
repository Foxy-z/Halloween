package fr.onecraft.halloween.core.database.objects;

import fr.onecraft.halloween.core.database.enums.SQLCondition;
import fr.onecraft.halloween.core.database.enums.SQLJoin;
import fr.onecraft.halloween.core.database.enums.SQLOrder;
import fr.onecraft.halloween.core.database.exceptions.DatabaseQueryException;
import fr.onecraft.halloween.utils.StringUtils;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

abstract class BaseQuery<T extends BaseQuery> {
    int paramIndex;
    String table;
    String select;
    String order;
    String group;
    String limit;

    boolean insertOrUpdate = false;
    boolean updatable = false;

    final List<String> where = new ArrayList<>();
    final List<String> having = new ArrayList<>();
    final List<String> join = new ArrayList<>();
    final List<String> update = new ArrayList<>();
    final Map<String, String> insert = new LinkedHashMap<>();
    final Map<String, String> args = new HashMap<>();

    /* FROM */

    public T from(String table) {
        this.table = checkSafe(table);
        return (T) this;
    }

    /* SELECT */

    public T select() {
        this.select = "SELECT * FROM";
        return (T) this;
    }

    public T select(String... items) {
        Arrays.stream(items).forEach(this::checkSafe);
        this.select = "SELECT " + StringUtils.join(", ", Arrays.asList(items)) + " FROM";
        return (T) this;
    }

    /* WHERE */

    public T where(String key, int value) {
        return where(SQLCondition.EQUALS, key, String.valueOf(value));
    }

    public T where(String key, String value) {
        return where(SQLCondition.EQUALS, key, value);
    }

    public T where(SQLCondition condition, String key, int value) {
        return where(condition, key, String.valueOf(value));
    }

    public T where(SQLCondition condition, String key, String value) {
        this.where.add(checkSafe(key) + " " + condition.getOperator() + " " + bindParam(value));
        return (T) this;
    }

    /* HAVING */

    public T having(String key, int value) {
        return having(SQLCondition.EQUALS, key, String.valueOf(value));
    }

    public T having(String key, String value) {
        return having(SQLCondition.EQUALS, key, value);
    }

    public T having(SQLCondition condition, String key, int value) {
        return having(condition, key, String.valueOf(value));
    }

    public T having(SQLCondition condition, String key, String value) {
        this.having.add(checkSafe(key) + " " + condition.getOperator() + " " + bindParam(value));
        return (T) this;
    }

    /* ORDER */

    public T order(SQLOrder order, String... tables) {
        Arrays.stream(tables).forEach(this::checkSafe);
        this.order = "ORDER BY " + StringUtils.join(", ", Arrays.asList(tables)) + " " + order.name();
        return (T) this;
    }

    /* LIMIT */

    public T limit(int limit) {
        if (limit < 0) throw new InvalidParameterException("Limit must be a positive number");

        this.limit = "LIMIT " + limit;
        return (T) this;
    }

    public T limit(int limit, int offset) {
        if (limit < 0 || offset < 0) throw new InvalidParameterException("Limit and offset must be positive numbers");

        this.limit = "LIMIT " + offset + ", " + limit;
        return (T) this;
    }

    /* GROUP */

    public T group(String column) {
        this.group = "GROUP by " + checkSafe(column);
        return (T) this;
    }

    /* JOIN */

    public T join(String table, String firstKey, String secondKey) {
        return join(SQLJoin.LEFT, table, firstKey, secondKey);
    }

    public T join(SQLJoin join, String table, String firstKey, String secondKey) {
        this.join.add(join.name() + " JOIN " + checkSafe(table) + " ON "
                + checkSafe(this.table) + "." + checkSafe(firstKey) + " = "
                + checkSafe(table) + "." + checkSafe(secondKey));
        return (T) this;
    }

    /* UTILS */

    String bindParam(String param) {
        String key = ":param" + paramIndex;
        args.put(key, param);
        paramIndex++;
        return key;
    }

    String checkSafe(String param) {
        if (!param.matches("[a-zA-Z_.]+") && !param.matches("count\\([a-zA-Z*_]+\\)")) {
            throw new InvalidParameterException("Parameter key must match [a-zA-Z*_]: '" + param + "' given");
        }

        return param;
    }

    String makeQuery() throws DatabaseQueryException {
        List<String> parts = new ArrayList<>();

        // if query is select
        if (this.select != null) {
            // bind select
            parts.add(this.select);

            // bind table
            parts.add(this.table);

            // bind join
            if (!this.join.isEmpty()) {
                parts.addAll(this.join);
            }

            // bind where
            if (!this.where.isEmpty()) {
                parts.add("WHERE");
                parts.add("(" + StringUtils.join(") AND (", this.where) + ")");
            }

            // bind group
            if (this.group != null) {
                parts.add(this.group);
            }

            // bind having
            if (!this.having.isEmpty()) {
                parts.add("HAVING");
                parts.add("(" + StringUtils.join(") AND (", this.having) + ")");
            }

            // bind order
            if (this.order != null) {
                parts.add(this.order);
            }

            // bind limit
            if (this.limit != null) {
                parts.add(this.limit);
            }
            // if query is insert
        } else if (!this.insert.isEmpty()) {
            // bind insert
            parts.add("INSERT INTO");
            parts.add(this.table);

            // bind columns
            parts.add("(");
            parts.add(StringUtils.join(", ", new ArrayList<>(this.insert.keySet())));
            parts.add(")");

            // bind values
            List<String> insertValues = this.insert.keySet().stream()
                    .map(this.insert::get)
                    .collect(Collectors.toList());

            parts.add("VALUES");
            parts.add("(");
            parts.add(StringUtils.join(", ", insertValues));
            parts.add(")");

            // bind duplicate keys case (if insertOrUpdate)
            if (this.insertOrUpdate) {
                parts.add("ON DUPLICATE KEY UPDATE");
                parts.add(StringUtils.join(", ", " = ", insert));
            }
        } else if (!this.update.isEmpty()) {
            // bind update
            parts.add("UPDATE");

            // bind table
            parts.add(this.table);

            // bind update
            parts.add("SET");
            parts.add(StringUtils.join(", ", this.update));

            // bind where
            if (!this.where.isEmpty()) {
                parts.add("WHERE");
                parts.add("(" + StringUtils.join(") AND (", this.where) + ")");
            }
        } else {
            throw new DatabaseQueryException("You must use insert, select or delete into your query");
        }

        return StringUtils.join(" ", parts);
    }
}
