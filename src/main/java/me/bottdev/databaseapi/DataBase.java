package me.bottdev.databaseapi;

import me.bottdev.databaseapi.Table.DataTable;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBase {

    private final String host;
    private final int port;
    private final String id;
    private final String user;
    private final String password;

    private Connection connection;

    public List<String> tablesIds;
    public static HashMap<String, DataTable> tables;

    public DataBase(String id, String host, String user, String password, int port, List<String> tablesIds) {
        this.id = id;
        this.host = host;
        this.user = user;
        this.password = password;
        this.port = port;

        this.tablesIds = tablesIds;

        Connect();
    }

    public boolean isConnected() {
        return !(connection == null);
    }

    public void Connect() {
        if (!isConnected()) {
            try {
                String url = "jdbc:mysql://" + host + ":" + port + "/" + id + "?useSSL=false&useUnicode=true&characterEncoding=utf8";
                connection = DriverManager.getConnection(url, user, password);

                loadTablesIds();
                loadTables();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public void Disconnect() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void loadTablesIds()
    {
        try {

            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String[] types = {"TABLE"};
            ResultSet rs = databaseMetaData.getTables(null, null, "%", types);
            while (rs.next()) {
                tablesIds.add(rs.getString("TABLE_NAME"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getTablesIds() {
        return tablesIds;
    }

    private void loadTables() {
        tables = new HashMap<>();
        for (String id : tablesIds) {
            tables.put(id, new DataTable(this, id, new HashMap<>(), new ArrayList<>()));
        }
    }

    public HashMap<String, DataTable> getAllTables() {
        return tables;
    }

    public DataTable getTable(String id) {
        if (tables.containsKey(id)) {
            return tables.get(id);
        }
        return null;
    }

    public boolean tableExists(String id) {
        return tablesIds.contains(id);
    }

    public void createTable(String name, String... columns) {
        if (isConnected()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        
                        String options = String.join(", ", columns);
                        
                        Statement statement = connection.createStatement();
                        statement.executeUpdate("CREATE TABLE " + name + " (" + options + ")");

                        tablesIds.add(name);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(DataBaseAPI.getPlugin(DataBaseAPI.class));
        }
    }

    public void dropTable(String name) {
        if (isConnected() && tableExists(name)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {

                        Statement statement = connection.createStatement();
                        statement.executeUpdate("DROP TABLE " + name);

                        tablesIds.remove(name);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(DataBaseAPI.getPlugin(DataBaseAPI.class));
        }
    }
}
