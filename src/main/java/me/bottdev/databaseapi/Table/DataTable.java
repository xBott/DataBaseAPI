package me.bottdev.databaseapi.Table;

import me.bottdev.databaseapi.DataBase;
import me.bottdev.databaseapi.DataBaseAPI;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataTable {

    private final DataBase dataBase;
    private final String id;

    final HashMap<String, TableColumn> columns;
    private final List<String> column_ids;
    public int rows_count;

    public DataTable(DataBase dataBase, String id, HashMap<String, TableColumn> hashMap, List<String> column_ids) {
        this.dataBase = dataBase;
        this.id = id;
        this.column_ids = column_ids;

        columns = hashMap;

        loadColumns();
        loadRowsCount();

    }

    private void loadColumns() {
        try {
            columns.clear();
            column_ids.clear();

            Statement statement = dataBase.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + id);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            int count = resultSetMetaData.getColumnCount();

            for (int i = 1; i <= count; i++) {
                String name = resultSetMetaData.getColumnName(i);
                String type = resultSetMetaData.getColumnTypeName(i);
                System.out.println(name);
                columns.put(name, new TableColumn(this, name, type, new HashMap<>()));
                column_ids.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRowsCount() {
        try {
            Statement statement = dataBase.getConnection().createStatement();
            String query = "SELECT COUNT(*) FROM " + id;
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            rows_count = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public DataBase getDataBase() {
        return dataBase;
    }

    public int getRowsCount() {
        return rows_count;
    }

    public List<TableColumn> getColumns() {
        List<TableColumn> result = new ArrayList<>();
        for (String key : getColumnsIds()) {
            result.add(columns.get(key));
        }
        return result;
    }

    public TableColumn getColumn(String id) {
        if (columns.containsKey(id)) {
            return columns.get(id);
        }
        return null;
    }

    public List<String> getColumnsIds() {
        return column_ids;
    }


    public void createRow(Object... values) {

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Statement statement = dataBase.getConnection().createStatement();

                    StringBuilder query = new StringBuilder("INSERT INTO " + id + "(");

                    List<String> column_ids = new ArrayList<>(getColumnsIds());
                    query.append(String.join(", ", column_ids)).append(") VALUES (");

                    List<String> default_values = new ArrayList<>();
                    for (int i = 0; i < getColumns().size(); i++) {
                        if (i > values.length-1) {
                            default_values.add("?");
                        } else {
                            if (getColumn(getColumnsIds().get(i)).getType().equalsIgnoreCase("TEXT") ||
                                    getColumn(getColumnsIds().get(i)).getType().equalsIgnoreCase("LONGTEXT")) {
                                default_values.add("'" + values[i] + "'");
                            } else {
                                default_values.add(String.valueOf(values[i]));
                            }
                        }
                    }

                    query.append(String.join(", ", default_values)).append(")");

                    statement.executeUpdate(query.toString());

                    loadColumns();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(DataBaseAPI.getPlugin(DataBaseAPI.class));
    }

    public void deleteRow(ColumnCondition... conditions) {

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Statement statement = dataBase.getConnection().createStatement();

                    StringBuilder query = new StringBuilder("DELETE FROM " + id + " WHERE ");

                    List<String> query_conditions = new ArrayList<>();
                    for (ColumnCondition condition : conditions) {
                        if (condition.value instanceof String) {
                            query_conditions.add(condition.column + condition.action + "'" + condition.value + "'");
                        } else {
                            query_conditions.add(condition.column + condition.action + condition.value);
                        }
                    }
                    query.append(String.join("," ,query_conditions));

                    statement.executeUpdate(query.toString());
                    loadColumns();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(DataBaseAPI.getPlugin(DataBaseAPI.class));
    }
}
