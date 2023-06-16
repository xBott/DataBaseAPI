package me.bottdev.databaseapi.Table;

import com.google.gson.JsonObject;
import me.bottdev.databaseapi.DataBase;
import me.bottdev.databaseapi.DataBaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TableColumn {

    private final DataTable dataTable;
    private final DataBase dataBase;
    private final String id;
    private final String type;

    final HashMap<Integer, Object> values;

    public TableColumn(DataTable dataTable, String id, String type, HashMap<Integer, Object> hashMap) {
        this.dataTable = dataTable;
        this.dataBase = dataTable.getDataBase();
        this.id = id;
        this.type = type;
        values = hashMap;

        loadValues();
    }


    private void loadValues() {
        try {
            Statement statement = dataBase.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT " + id + " FROM " + dataTable.getId());

            int index = 0;
            while (resultSet.next()) {
                String value = resultSet.getString(1);
                values.put(index, value);
                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public DataTable getTable() {
        return dataTable;
    }


    public boolean containsValue(Object value) {
        for (Integer key : values.keySet()) {

            Object o = values.get(key);

            if (o instanceof String) {

                if (String.valueOf(values.get(key)).equalsIgnoreCase(String.valueOf(value))) {
                    return true;
                }

            } else {
                if (values.get(key) == value) {
                    return true;
                }
            }
        }
        return false;
    }


    public List<Object> getValues() {
        List<Object> results = new ArrayList<>();
        for (Integer key : values.keySet()) {
            results.add(values.get(key));
        }
        return results;
    }

    public Object getValue(int index) {
        return getValues().get(index);
    }


    public Object getValue(ColumnCondition... conditions) {

        List<Integer> indexes = checkConditions(conditions);
        if (!indexes.isEmpty()) {
            if (getType().equalsIgnoreCase("LONGTEXT")) {

                JSONParser parser = new JSONParser();
                try {
                    return parser.parse(String.valueOf(values.get(indexes.get(0))));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else {
                return values.get(indexes.get(0));
            }

        }

        return null;
    }

    public List<Object> getValues(ColumnCondition... conditions) {

        List<Object> results = new ArrayList<>();

        List<Integer> indexes = checkConditions(conditions);
        if (!indexes.isEmpty()) {
            for (Integer index : indexes) {
                if (getType().equalsIgnoreCase("LONGTEXT")) {

                    JSONParser parser = new JSONParser();
                    try {
                        results.add(parser.parse(String.valueOf(values.get(index))));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    results.add(values.get(index));
                }

            }
        }

        return results;
    }

    public Object getValue(Player p) {

        List<Integer> indexes = checkConditions(new ColumnCondition("UUID", "=", p.getUniqueId()));
        if (!indexes.isEmpty()) {
            if (getType().equalsIgnoreCase("LONGTEXT")) {

                JSONParser parser = new JSONParser();
                try {
                    return parser.parse(String.valueOf(values.get(indexes.get(0))));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else {
                return values.get(indexes.get(0));
            }
        }

        return null;
    }



    public void setValues(Object value, ColumnCondition... conditions) {
        List<Integer> indexes = checkConditions(conditions);
        if (!indexes.isEmpty()) {
            for (Integer index : indexes) {
                values.replace(index, value);
                updateValue(value, conditions);
            }
        }
    }

    public void setValue(Object value, ColumnCondition... conditions) {
        List<Integer> indexes = checkConditions(conditions);
        if (!indexes.isEmpty()) {
            values.replace(indexes.get(0), value);
            updateValue(value, conditions);
        }
    }

    public void setValue(Player p, Object value) {
        List<Integer> indexes = checkConditions(new ColumnCondition("UUID", "=", p.getUniqueId()));
        if (!indexes.isEmpty()) {
            values.replace(indexes.get(0), value);
            updateValue(value, new ColumnCondition("UUID", "=", p.getUniqueId()));
        }
    }

    private void updateValue(Object value, ColumnCondition... conditions) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {

                    Statement statement = dataBase.getConnection().createStatement();

                    StringBuilder query = new StringBuilder("UPDATE " + dataTable.getId() + " SET " + getId() + "=");

                    if ("TEXT".equalsIgnoreCase(getType()) || "LONGTEXT".equalsIgnoreCase(getType())) {
                        query.append("'").append(value).append("'");
                    } else {
                        query.append(value);
                    }

                    query.append(" WHERE ");

                    List<String> query_conditions = new ArrayList<>();
                    for (ColumnCondition condition : conditions) {
                        if (condition.value instanceof JsonObject) {
                            continue;
                        }
                        if (condition.value instanceof String) {
                            query_conditions.add(condition.column + condition.action + "'" + condition.value + "'");
                        } else {
                            query_conditions.add(condition.column + condition.action + condition.value);
                        }
                    }
                    query.append(String.join("," ,query_conditions));

                    statement.executeUpdate(query.toString());

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(DataBaseAPI.getPlugin(DataBaseAPI.class));
    }



    public boolean conditionsMet(ColumnCondition... conditions) {
        List<Integer> indexes = checkConditions(conditions);
        return !indexes.isEmpty();
    }


    public List<Integer> checkConditions(ColumnCondition... conditions) {

        List<Integer> results = new ArrayList<>();

        for (int i = 0; i < dataTable.getRowsCount(); i++) {
            int met_conditions = 0;

            for (ColumnCondition condition : conditions) {

                String column_id = condition.column;
                TableColumn column = dataTable.getColumn(column_id);
                if (column == null) continue;

                String action = condition.action;
                String value = String.valueOf(condition.value);

                Object object = column.getValue(i);

                switch (column.getType()) {
                    case "TEXT" -> {
                        switch (action) {
                            case "=" -> {
                                if (value.equalsIgnoreCase((String) object)) {
                                    met_conditions++;
                                }
                            }
                            case "!=" -> {
                                if (!value.equalsIgnoreCase((String) object)) {
                                    met_conditions++;
                                }
                            }
                            case "contains" -> {
                                if (((String) object).contains(value)) {
                                    met_conditions++;
                                }
                            }
                        }
                    }
                    case "INT" -> {
                        int int_value = Integer.parseInt(value);
                        switch (action) {
                            case "=" -> {
                                if (int_value == Integer.parseInt((String) object)) {
                                    met_conditions++;
                                }
                            }
                            case "!=" -> {
                                if (int_value != Integer.parseInt((String) object)) {
                                    met_conditions++;
                                }
                            }
                            case ">" -> {
                                if (Integer.parseInt((String) object) > int_value) {
                                    met_conditions++;
                                }
                            }
                            case "<" -> {
                                if (Integer.parseInt((String) object) < int_value) {
                                    met_conditions++;
                                }
                            }
                            case ">=" -> {
                                if (Integer.parseInt((String) object) >= int_value) {
                                    met_conditions++;
                                }
                            }
                            case "<=" -> {
                                if (Integer.parseInt((String) object) <= int_value) {
                                    met_conditions++;
                                }
                            }
                        }
                    }
                    case "DOUBLE" -> {
                        double double_value = Double.parseDouble(value);
                        switch (action) {
                            case "=" -> {
                                if (double_value == Double.parseDouble((String)object)) {
                                    met_conditions++;
                                }
                            }
                            case "!=" -> {
                                if (double_value != Double.parseDouble((String)object)) {
                                    met_conditions++;
                                }
                            }
                            case ">" -> {
                                if (Double.parseDouble((String)object) > double_value) {
                                    met_conditions++;
                                }
                            }
                            case "<" -> {
                                if (Double.parseDouble((String)object) < double_value) {
                                    met_conditions++;
                                }
                            }
                            case ">=" -> {
                                if (Double.parseDouble((String)object) >= double_value) {
                                    met_conditions++;
                                }
                            }
                            case "<=" -> {
                                if (Double.parseDouble((String)object) <= double_value) {
                                    met_conditions++;
                                }
                            }
                        }
                    }
                    case "BOOLEAN" -> {
                        boolean boolean_value = Boolean.parseBoolean(value);
                        switch (action) {
                            case "=" -> {
                                if (boolean_value == Boolean.parseBoolean((String)object)) {
                                    met_conditions++;
                                }
                            }
                            case "!=" -> {
                                if (boolean_value != Boolean.parseBoolean((String)object)) {
                                    met_conditions++;
                                }
                            }
                        }
                    }
                }
            }
            if (met_conditions == conditions.length) {
                results.add(i);
            }
        }

        return results;
    }


}
