package me.bottdev.databaseapi.Table.JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonList<T> {

    private List<T> list;
    private JSONObject json_object;
    private final int mode;

    private final boolean checked;

    public JsonList(List<T> list) {
        this.mode = 1;
        this.checked = true;
        this.list = list;
        this.json_object = getJsonObject();

    }
    public JsonList(JSONObject o) {
        this.mode = 2;
        this.json_object = o;
        this.checked = checkJsonObject();
        this.list = getList();

    }

    private String getJsonString() {
        if (!checked) return "{}";

        return json_object.toString();
    }

    public void updateList(List<T> new_list) {
        if (!checked) return;

        this.list = new_list;
        this.json_object = getJsonObject();
    }

    @SuppressWarnings("unchecked")
    public JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject();
        JSONArray values = new JSONArray();
        values.addAll(list);
        jsonObject.put("function", "list");
        jsonObject.put("values", values);
        return jsonObject;
    }

    private boolean checkJsonObject() {
        if (mode == 1) return true;
        if (!json_object.containsKey("function")) return false;
        String function = (String)json_object.get("function");
        return function.equalsIgnoreCase("list");
    }

    public static boolean checkJsonObject(JSONObject json_object) {
        if (!json_object.containsKey("function")) return false;
        String function = (String)json_object.get("function");
        return function.equalsIgnoreCase("list");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Object> getObjectList() {
        if (!checked) return new ArrayList<>();

        JSONArray array = (JSONArray)json_object.get("values");

        return new ArrayList<>(array);
    }

    @SuppressWarnings("unchecked")
    public List<T> getList() {
        if (!checked) return new ArrayList<>();

        List<Object> objects = getObjectList();
        List<T> results = new ArrayList<>();

        for (Object o : objects) {
            results.add(((T)o));
        }
        return results;
    }

    @SuppressWarnings("deprecation")
    public void printList() {
        if (!checked) return;

        ObjectMapper mapper = new ObjectMapper();
        try {
            Bukkit.broadcastMessage(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(getJsonString())));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }



}
