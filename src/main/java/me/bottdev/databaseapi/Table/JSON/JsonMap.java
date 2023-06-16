package me.bottdev.databaseapi.Table.JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

import java.util.*;

public class JsonMap<K, V> {

    private Map<K, V> map;
    private JSONObject json_object;
    private final int mode;

    private boolean checked;

    public JsonMap(Map<K, V> map) {
        this.mode = 1;
        this.checked = true;
        this.map = map;
        this.json_object = getJsonObject();

    }
    public JsonMap() {
        this.mode = 2;
        this.checked = false;
    }

    public void loadFromJson(JSONObject o) {
        if (checked) return;

        this.json_object = o;
        this.checked = checkJsonObject();
        this.map = getMap();
    }

    private String getJsonString() {
        if (!checked) return "{}";

        return json_object.toString();
    }

    public void updateMap(Map<K, V> map) {
        if (!checked) return;

        this.map = map;
        this.json_object = getJsonObject();
    }

    @SuppressWarnings("unchecked")
    public JSONObject getJsonObject() {

        JSONObject jsonObject = new JSONObject();
        JSONObject jsonValues = new JSONObject();
        for (K key : map.keySet()) {
            V value = map.get(key);
            jsonValues.put(key, value);
        }

        jsonObject.put("function", "map");
        jsonObject.put("values", jsonValues);

        return jsonObject;
    }

    private boolean checkJsonObject() {
        if (mode == 1) return true;
        if (!json_object.containsKey("function")) return false;
        String function = (String)json_object.get("function");
        return function.equalsIgnoreCase("map");
    }

    public static boolean checkJsonObject(JSONObject json_object) {
        if (!json_object.containsKey("function")) return false;
        String function = (String)json_object.get("function");
        return function.equalsIgnoreCase("map");
    }

    private Map<Object, Object> getObjectMap() {
        if (!checked) return new HashMap<>();

        Map<Object, Object> map = new HashMap<>();

        for (Object key : ((JSONObject)json_object.get("values")).keySet()) {
            Object value = json_object.get(key);
            map.put(key, value);
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    public Map<K, V> getMap() {
        if (!checked) return new HashMap<>();

        Map<K, V> objects = (Map<K, V>) getObjectMap();
        Map<K, V> map = new HashMap<>();

        for (K key : objects.keySet()) {
            map.put(key, map.get(key));
        }
        return map;
    }

    @SuppressWarnings("deprecation")
    public void printMap() {
        if (!checked) return;

        ObjectMapper mapper = new ObjectMapper();
        try {
            Bukkit.broadcastMessage(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(getJsonString())));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }



}
