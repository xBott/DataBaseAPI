package me.bottdev.databaseapi.Commands;


import me.bottdev.databaseapi.DataBase;
import me.bottdev.databaseapi.DataBaseAPI;
import me.bottdev.databaseapi.Table.ColumnCondition;
import me.bottdev.databaseapi.Table.DataTable;
import me.bottdev.databaseapi.Table.JSON.JsonList;
import me.bottdev.databaseapi.Table.JSON.JsonMap;
import me.bottdev.databaseapi.Table.TableColumn;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import java.util.*;

public class MainCommand implements CommandExecutor, TabExecutor {

    private final DataBaseAPI plugin;

    public MainCommand(DataBaseAPI plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (p.isOp()) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("columns")) {
                        int index = Integer.parseInt(args[1]);
                        String id = args[2];

                        DataBase dataBase = plugin.getLoadedDataBase(index);
                        DataTable table = dataBase.getTable(id);

                        p.sendMessage(ChatColor.YELLOW + table.getId() + ":");
                        for (TableColumn column : table.getColumns()) {
                            p.sendMessage(ChatColor.WHITE + column.getId() + "   " + ChatColor.AQUA + column.getType());
                            for (Object value : column.getValues()) {
                                p.sendMessage(ChatColor.GRAY + "  - " + value);
                            }
                        }
                    }
                    if (args[0].equalsIgnoreCase("test")) {

                        DataBase dataBase = plugin.getLoadedDataBase(0);
                        DataTable table = dataBase.getTable("player_data");


                        table.getColumn("NAME").getValues(new ColumnCondition("XP", ">", 500), new ColumnCondition("LEVEL", "=", 67)).forEach(o -> Bukkit.broadcastMessage((String) o));
                        table.getColumn("LEVEL").getValues(new ColumnCondition("NAME", "=", "Pidaras")).forEach(o -> Bukkit.broadcastMessage((String) o));

                        if (table.getColumn("NAME").containsValue("Pidaras")) {
                            table.getColumn("NAME").setValue("PlayerTest1", new ColumnCondition("UUID", "=", "b4007b01-d6df-4f63-943f-772fb5f0b237"));
                        } else {
                            table.getColumn("NAME").setValue("Pidaras", new ColumnCondition("UUID", "=", "b4007b01-d6df-4f63-943f-772fb5f0b237"));
                        }

                    }
                    if (args[0].equalsIgnoreCase("create_table")) {

                        DataBase dataBase = plugin.getLoadedDataBase(0);
                        String name = args[1];

                        dataBase.createTable(name, "UUID TEXT", "TEST DOUBLE");
                    }
                    if (args[0].equalsIgnoreCase("drop_table")) {

                        DataBase dataBase = plugin.getLoadedDataBase(0);
                        String name = args[1];

                        dataBase.dropTable(name);
                    }
                    if (args[0].equalsIgnoreCase("create_row")) {

                        DataBase dataBase = plugin.getLoadedDataBase(0);

                        int random = new Random().nextInt(12);
                        dataBase.getTable("player_data").createRow(
                                "PlayerTest" + random,
                                "name" + random,
                                UUID.randomUUID().toString(),
                                new Random().nextInt(100),
                                new Random().nextInt(3000),
                                new String[]{"HUMAN", "ELF"}[new Random().nextInt(2)]);
                    }
                    if (args[0].equalsIgnoreCase("delete_row")) {

                        DataBase dataBase = plugin.getLoadedDataBase(0);

                        dataBase.getTable("player_data").deleteRow(new ColumnCondition("NAME", "=", args[1]));
                    }
                    if (args[0].equalsIgnoreCase("test_json")) {

                        DataBase dataBase = plugin.getLoadedDataBase(0);
                        DataTable table = dataBase.getTable("player_skills");

                        if (table.getRowsCount() > 0) {

                            JSONObject jsonObject = (JSONObject)table.getColumn("BATTLE_SKILLS").getValue(new ColumnCondition("PLAYER", "=", "TestPlayer"));

                            JsonList<String> jsonList2 = new JsonList<>(jsonObject);

                            List<String> data_list = jsonList2.getList();
                            data_list.add("Huy_bobra");
                            jsonList2.updateList(data_list);

                            table.getColumn("BATTLE_SKILLS").setValue(jsonList2.getJsonObject(), new ColumnCondition("PLAYER", "=", "TestPlayer"));

                        } else {
                            table.createRow(UUID.randomUUID().toString(), "TestPlayer", 100, "{}", "{}", "{}");
                        }
                    }
                    if (args[0].equalsIgnoreCase("test_json2")) {

                        DataBase dataBase = plugin.getLoadedDataBase(0);
                        DataTable table = dataBase.getTable("player_skills");

                        if (table.getColumn("UUID").containsValue(Objects.requireNonNull(((Player) sender).getPlayer()).getUniqueId().toString())) {

                            JsonMap<String, JSONObject> jsonMap = new JsonMap<>();
                            jsonMap.loadFromJson((JSONObject) table.getColumn("BATTLE_SKILLS").getValue(((Player) sender)));

                            jsonMap.printMap();

                            Map<String, JSONObject> map = jsonMap.getMap();

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("id", "test_skill");
                            jsonObject.put("name", "&cTest Skill");
                            map.put("skill1", jsonObject);

                            jsonMap.updateMap(map);

                            jsonMap.printMap();

                            table.getColumn("BATTLE_SKILLS").setValue(((Player) sender), jsonMap.getJsonObject());

                        } else {

                            table.createRow(((Player) sender).getUniqueId(), sender.getName(), 0,
                                    new JsonMap<>(new HashMap<String, JSONObject>()).getJsonObject(),
                                    new JsonMap<>(new HashMap<String, JSONObject>()).getJsonObject(),
                                    new JsonMap<>(new HashMap<String, JSONObject>()).getJsonObject());

                        }
                    }
                }
            } else {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 1.0f);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {

        List<String> list = List.of("columns", "test", "create_table", "drop_table", "create_row", "delete_row", "test_json", "test_json2");


        List<String> completions = null;
        if (sender.isOp()) {
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                for (String s : list) {
                    if (s.startsWith(input)) {

                        if (completions == null) {
                            completions = new ArrayList<>();
                        }
                        completions.add(s);
                    }
                }
            }

            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("columns")) {
                    String input = args[1].toLowerCase();
                    for (String s : new String[]{"0"}) {
                        if (s.startsWith(input)) {

                            if (completions == null) {
                                completions = new ArrayList<>();
                            }
                            completions.add(s);
                        }
                    }
                }
            }

            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("columns")) {
                    try {
                        int index = Integer.parseInt(args[1]);
                        DataBase dataBase = plugin.getLoadedDataBase(index);

                        String input = args[2].toLowerCase();
                        for (String s : dataBase.getTablesIds()) {
                            if (s.startsWith(input)) {

                                if (completions == null) {
                                    completions = new ArrayList<>();
                                }
                                completions.add(s);
                            }
                        }

                    } catch (Exception ignored) {
                    }

                }
            }
        }

        if (completions != null) {
            Collections.sort(completions);
        }

        return completions;

    }

}
