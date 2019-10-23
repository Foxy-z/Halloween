package fr.onecraft.halloween.core.database.objects;

import java.util.Map;

public class SubQuery extends BaseQuery<SubQuery> {

    public SubQuery() {

    }

    protected Map<String, String> getArguments() {
        return this.args;
    }
}
