package com.abinbev.dsa.utils.datamanager;

import com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;

import org.apache.commons.text.StrLookup;

import java.util.HashMap;
import java.util.Map;

import static com.salesforce.androidsyncengine.utils.CharUtils.COLON;

/**
 * Created by jstafanowski on 13.11.17.
 */
public class FormatValues extends StrLookup<String> {

    private static final String FORMAT_COLUMN = "${%s:%s}";
    private static final String FORMAT_TABLE = "${%s}";

    private final Map<String, String> values = new HashMap<>();

    public static FormatValues join(FormatValues... other) {
        FormatValues newFormatValues = new FormatValues();
        for (FormatValues otherFormatValues : other) {
            newFormatValues.values.putAll(otherFormatValues.values);
        }

        return newFormatValues;
    }

    public FormatValues putValue(String key, Object value) {
        values.put(key, String.valueOf(value));
        return this;
    }

    public FormatValues putColumn(String key, String table, String column) {
        values.put(key, String.format(FORMAT_COLUMN, table, column));
        return this;
    }

    public FormatValues putTable(String key, String table) {
        values.put(key, String.format(FORMAT_TABLE, table));
        return this;
    }

    public FormatValues addAll(FormatValues other) {
        values.putAll(other.values);
        return this;
    }

    private static void addStdColumns(TableBuilder tb) {
        tb.putColumn(StdFields.SOUP, StdFields.SOUP);
        tb.putColumn(StdFields.ID, StdFields.ID);
    }

    @Override
    public String lookup(String key) {
        return values.get(key);
    }

    public static class Builder {

        final FormatValues fv;

        private Builder(FormatValues formatValues) {
            this.fv = formatValues;
        }

        public Builder() {
            this(new FormatValues());
        }

        public TableBuilder putTable(String key, String table) {
            fv.putTable(key, table);
            TableBuilder tableBuilder = new TableBuilder(fv, key, table);
            addStdColumns(tableBuilder);
            return tableBuilder;
        }

        public Builder putColumn(String key, String table, String column) {
            fv.putColumn(key, table, column);
            return this;
        }

        public Builder putValue(String key, String value) {
            fv.putValue(key, value);
            return this;
        }

        public Builder addAll(FormatValues other) {
            fv.addAll(other);
            return this;
        }

        public FormatValues build() {
            return fv;
        }
    }

    public static class TableBuilder extends Builder {

        private final String tableKey;

        private final String tableValue;

        private TableBuilder(FormatValues fv, String tableKey, String tableValue) {
            super(fv);
            this.tableKey = tableKey;
            this.tableValue = tableValue;
        }

        public TableBuilder putColumn(String key, String column) {
            fv.putColumn(tableKey + COLON + key, tableValue, column);
            return this;
        }
    }
}
