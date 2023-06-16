package me.bottdev.databaseapi.Table;

import java.util.UUID;

public class ColumnCondition {

    public final String column;
    public final String action;
    public Object value;


    public ColumnCondition(String column, String action, Object value) {
        this.column = column;
        this.action = action;
        this.value = value;

        if (value instanceof UUID) {
            this.value = value.toString();
        }
    }
}
