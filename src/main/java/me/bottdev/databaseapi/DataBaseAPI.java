package me.bottdev.databaseapi;

import me.bottdev.databaseapi.Commands.MainCommand;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DataBaseAPI extends JavaPlugin {

    public List<DataBase> loadedDataBases = new ArrayList<>();

    @Override
    public void onEnable() {
        setupCommands();
    }

    @SuppressWarnings("unused")
    public void loadDataBase(String id, String host, String user, String password, int port) {
        DataBase db = new DataBase(id, host, user, password, port, new ArrayList<>());
        if (db.isConnected()) {
            loadedDataBases.add(db);
            ConsoleCommandSender console = getServer().getConsoleSender();
            console.sendMessage(" ");
            console.sendMessage(" ");
            console.sendMessage("&aDataBase is connected!");
            console.sendMessage(" ");
            console.sendMessage(" ");
        }
    }

    public void setupCommands() {
        Objects.requireNonNull(getCommand("db")).setExecutor(new MainCommand(this));
    }

    @Override
    public void onDisable() {
        loadedDataBases.forEach(DataBase::Disconnect);
    }

    public List<DataBase> getLoadedDataBases() {
        return loadedDataBases;
    }

    public DataBase getLoadedDataBase(int index) {
        return loadedDataBases.get(index);
    }
}
