package fr.onecraft.halloween.core.database.objects;

import fr.onecraft.halloween.core.database.exceptions.DatabaseQueryException;
import fr.onecraft.halloween.utils.StringUtils;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

abstract class BaseQuery {
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
    final Map<String, String> insert = new LinkedHashMap<>();
    final Map<String, String> args = new HashMap<>();

    String checkSafe(String param) {
        if (!param.matches("[a-zA-Z_]+")) {
            throw new InvalidParameterException("Parameter key must match [a-zA-Z_]");
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
        } else {
            throw new DatabaseQueryException("You must use insert, select or delete into your query");
        }

        return StringUtils.join(" ", parts);
    }
}
