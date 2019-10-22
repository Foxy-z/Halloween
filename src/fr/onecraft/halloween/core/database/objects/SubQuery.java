package fr.onecraft.halloween.core.database.objects;

import fr.onecraft.halloween.core.database.enums.SQLJoin;
import fr.onecraft.halloween.core.database.enums.SQLOrder;
import fr.onecraft.halloween.core.database.exceptions.DatabaseQueryException;
import fr.onecraft.halloween.utils.StringUtils;

import java.security.InvalidParameterException;
import java.util.Arrays;

public class SubQuery extends BaseQuery {
    private Query query;

    public SubQuery(Query query) {
        this.query = query;
    }

    public String build() throws DatabaseQueryException {
        return this.makeQuery();
    }

    public SubQuery from(String table) {
        this.table = checkSafe(table);
        return this;
    }

    public SubQuery select() {
        this.select = "SELECT * FROM";
        return this;
    }

    public SubQuery select(String... items) {
        Arrays.stream(items).forEach(this::checkSafe);
        this.select = "SELECT " + StringUtils.join(", ", Arrays.asList(items)) + " FROM";
        return this;
    }

    public SubQuery insert(String item, int value) {
        return insert(item, String.valueOf(value));
    }

    public SubQuery insert(String item, long value) {
        return insert(item, String.valueOf(value));
    }

    public SubQuery insert(String item, String value) {
        this.insert.put(checkSafe(item), query.bindParam(value));
        return this;
    }

    public SubQuery insertOrUpdate() {
        this.insertOrUpdate = true;
        return this;
    }

    public SubQuery where(String key, int value) {
        return where(key, String.valueOf(value));
    }

    public SubQuery where(String key, String value) {
        this.where.add(checkSafe(key) + " = " + query.bindParam(value));
        return this;
    }

    public SubQuery order(SQLOrder order, String... tables) {
        Arrays.stream(tables).forEach(this::checkSafe);
        this.order = "ORDER BY " + StringUtils.join(", ", Arrays.asList(tables)) + " " + order.name();
        return this;
    }

    public SubQuery limit(int limit) {
        if (limit < 0) throw new InvalidParameterException("Limit must be a positive number");

        this.limit = "LIMIT " + limit;
        return this;
    }

    public SubQuery limit(int limit, int offset) {
        if (limit < 0 || offset < 0) throw new InvalidParameterException("Limit and offset must be positive numbers");

        this.limit = "LIMIT " + offset + ", " + limit;
        return this;
    }

    public SubQuery group(String column) {
        this.group = "GROUP by " + checkSafe(column);
        return this;
    }

    public SubQuery join(String table, String firstKey, String secondKey) {
        return join(SQLJoin.LEFT, table, firstKey, secondKey);
    }

    public SubQuery join(SQLJoin join, String table, String firstKey, String secondKey) {
        this.join.add(join.name() + " JOIN " + checkSafe(table) + " ON "
                + checkSafe(this.table) + "." + checkSafe(firstKey) + " = "
                + checkSafe(table) + "." + checkSafe(secondKey));

        return this;
    }
}
