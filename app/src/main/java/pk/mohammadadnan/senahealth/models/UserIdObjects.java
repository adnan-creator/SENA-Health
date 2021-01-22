package pk.mohammadadnan.senahealth.models;

import java.util.List;

public class UserIdObjects {

    public String table;
    public List<List<String>> query;

    public UserIdObjects(String table, List<List<String>> query) {
        this.table = table;
        this.query = query;
    }

    public String getTable() {
        return table;
    }

    public List<List<String>> getQuery() {
        return query;
    }
}
