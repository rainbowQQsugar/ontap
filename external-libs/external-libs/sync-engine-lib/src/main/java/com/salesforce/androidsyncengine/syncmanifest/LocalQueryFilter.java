package com.salesforce.androidsyncengine.syncmanifest;

import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;

/**
 * Created by Jakub Stefanowski on 12.04.2017.
 */
// TODO: unused yet
public class LocalQueryFilter {

    private String rawQuery;

    private String[] values;

    private boolean selectAll;

    private String[] columns;
    private String from;
    private String where;
    private String groupBy;
    private String having;
    private String orderBy;
    private String limit;

    public String getRawQuery() {
        return rawQuery;
    }

    public void setRawQuery(String rawQuery) {
        this.rawQuery = rawQuery;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public boolean isSelectAll() {
        return selectAll;
    }

    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getHaving() {
        return having;
    }

    public void setHaving(String having) {
        this.having = having;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    /** Build query using current parameters. */
    public String buildQuery() {
        return buildQuery(null);
    }

    /** Build query using current parameters and default values. */
    public String buildQuery(String defaultTable, String... defaultColumns) {
        if (!isEmpty(rawQuery)) {
            return rawQuery;
        }
        else if (!isEmpty(values)) {
            return buildValuesQuery(values);
        }
        else if (selectAll) {
            String[] columns = firstNonEmpty(this.columns, defaultColumns);
            String from = firstNonEmpty(this.from, defaultTable);
            return buildQuery(columns, from, null /* where */, groupBy, having, orderBy, limit);
        }
        else {
            assertArg("You have to set 'selectAll' to true or pass 'where' parameter", !isEmpty(where));

            String[] columns = firstNonEmpty(this.columns, defaultColumns);
            String from = firstNonEmpty(this.from, defaultTable);
            return buildQuery(columns, from, where, groupBy, having, orderBy, limit);
        }
    }

    private static String buildQuery(String[] columns, String from, String where, String groupBy, String having, String orderBy, String limit) {
        assertArg("Columns array cannot be empty.", !isEmpty(columns));
        assertArg("Statement 'from' cannot be empty.", !isEmpty(from));

        return SQLiteQueryBuilder.buildQueryString(false, from, columns, where, groupBy, having, orderBy, limit);
    }

    private static String buildValuesQuery(String[] values) {
        StringBuilder sb = new StringBuilder("VALUES(");
        sb.append(values[0]);

        for (int i = 1; i < values.length; i++) {
            sb.append(',').append(values[i]);
        }

        sb.append(')');
        return sb.toString();
    }

    private static boolean isEmpty(String str) {
        return TextUtils.isEmpty(str);
    }

    private static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }

    private static void assertArg(String msg, boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }

    private static String firstNonEmpty(String str1, String str2) {
        return !isEmpty(str1) ? str1 : str2;
    }

    private static String[] firstNonEmpty(String[] arr1, String[] arr2) {
        return !isEmpty(arr1) ? arr1 : arr2;
    }
}
