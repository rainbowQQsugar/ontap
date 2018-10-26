package com.abinbev.dsa.syncmanifest;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlInfo;
import org.apache.commons.text.StrLookup;
import org.apache.commons.text.StrSubstitutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jakub Stefanowski on 06.04.2017.
 */

class QueryProcessor {

    public static final String VARIABLE_PREFIX = "${";
    public static final String VARIABLE_SUFFIX = "}$";
    public static final char ESCAPE_CHAR = '$';

    private final JexlEngine jexlEngine;

    QueryProcessor(JexlEngine jexlEngine) {
        this.jexlEngine = jexlEngine;
    }

    public Object processExpression(String expression, JexlContext jexlContext, JexlInfo info) {
        return jexlEngine.createExpression(info, expression).evaluate(jexlContext);
    }

    public String processQuery(String query, JexlContext jexlContext, JexlInfo info) {
        Map<String, Object> dataMap = new HashMap<>();
        String preparedQuery = prepareQuery(query, jexlContext, info, dataMap);
        return insertData(preparedQuery, dataMap);
    }

    public List<String> processQuery(String query, JexlContext jexlContext, JexlInfo info, int maxItems) {
        List<String> result = new ArrayList<>();

        Map<String, Object> dataMap = new HashMap<>();
        String preparedQuery = prepareQuery(query, jexlContext, info, dataMap);

        int totalItems = calculateItems(dataMap);

        if (totalItems <= maxItems) {
            String singlePageQuery = insertData(preparedQuery, dataMap, 0, 1);
            result.add(singlePageQuery);
        }
        else {
            int totalPages = totalItems / maxItems;

            // If maxItems=100 and totalItems=200 we want to have 2 pages but if totalItems=201 we
            // want 3 pages.
            if (totalItems % maxItems != 0) {
                totalPages += 1;
            }

            for (int currentPage = 0; currentPage < totalPages; currentPage++) {
                String singlePageQuery = insertData(preparedQuery, dataMap, currentPage, totalPages);
                result.add(singlePageQuery);
            }
        }

        return result;
    }

    private String prepareQuery(final String query, final JexlContext jexlContext, final JexlInfo info, final Map<String, Object> dataMap) {
        QueryPreparingLookup lookup = new QueryPreparingLookup(jexlEngine, dataMap);
        lookup.setInfo(info);
        lookup.setJexlContext(jexlContext);

        StrSubstitutor substitutor = createDefaultSubstitutor(lookup);
        return substitutor.replace(query);
    }

    private String insertData(final String query, final Map<String, Object> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) return query;

        DataInsertingLookup lookup = new DataInsertingLookup(dataMap, 0, 1);
        StrSubstitutor substitutor = createDefaultSubstitutor(lookup);
        return substitutor.replace(query);
    }

    private String insertData(final String query, final Map<String, Object> dataMap, int currentPage, int totalPages) {
        if (dataMap == null || dataMap.isEmpty()) return query;

        DataInsertingLookup lookup = new DataInsertingLookup(dataMap, currentPage, totalPages);
        StrSubstitutor substitutor = createDefaultSubstitutor(lookup);
        return substitutor.replace(query);
    }

    private StrSubstitutor createDefaultSubstitutor(StrLookup<?> lookup) {
        StrSubstitutor substitutor = new StrSubstitutor(lookup);
        substitutor.setValueDelimiterMatcher(null);
        substitutor.setVariableSuffix(VARIABLE_SUFFIX);
        substitutor.setVariablePrefix(VARIABLE_PREFIX);
        substitutor.setEscapeChar(ESCAPE_CHAR);
        return substitutor;
    }

    /** Returns number of items from all arrays and collections in this data map. */
    private int calculateItems(Map<String, Object> dataMap) {
        if (dataMap == null) return 0;

        int totalSize = 0;

        for (Object data : dataMap.values()) {
            if (data instanceof Collection) {
                totalSize += ((Collection) data).size();
            }
            else if (data instanceof Object[]){
                totalSize += ((Object[]) data).length;
            }
        }

        return totalSize;
    }

    private static int ceil(float num) {
        return (int) Math.round(Math.ceil(num));
    }

    /**
     * Run through query, gather all evaluated data and insert to dataMap. Replaces keys in query
     * with numbers, which can be accessed in data map.
     */
    private static class QueryPreparingLookup extends StrLookup<Object> {

        private final Map<String, Object> dataMap;

        private final JexlEngine jexlEngine;

        private JexlInfo info;

        private JexlContext jexlContext;

        private int itemIndex = 0;

        private QueryPreparingLookup(JexlEngine jexlEngine, Map<String, Object> dataMap) {
            this.dataMap = dataMap;
            this.jexlEngine = jexlEngine;
        }

        @Override
        public String lookup(String key) {
            Object result = jexlEngine.createExpression(info, key).evaluate(jexlContext);
            result = convertResult(result);
            String dataKey = Integer.toString(itemIndex);
            dataMap.put(dataKey, result);
            itemIndex++;

            // Replace inline query with more simple key. It will match the key in data map.
            return ESCAPE_CHAR + VARIABLE_PREFIX + dataKey + VARIABLE_SUFFIX;
        }

        public void setInfo(JexlInfo info) {
            this.info = info;
        }

        public void setJexlContext(JexlContext jexlContext) {
            this.jexlContext = jexlContext;
        }

        private Object convertResult(Object obj) {
            // We need to do this conversion because some of the collections won't allow to
            // split them into pages (i.e. Set).
            if (obj instanceof Collection<?> && !(obj instanceof List<?>)) {
                Collection<?> collection = (Collection<?>) obj;
                ArrayList<Object> list = new ArrayList<>();
                list.addAll(collection);
                return list;
            }
            else {
                return obj;
            }
        }
    }

    /** Inserts data to query from provided data map. */
    private static class DataInsertingLookup extends StrLookup<Object> {

        private final Map<String, Object> dataMap;

        private final StringConverter converter;

        private int currentPage;

        private int totalPages;

        private DataInsertingLookup(Map<String, Object> dataMap, int currentPage, int totalPages) {
            this.dataMap = dataMap;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.converter = new StringConverter();
        }

        @Override
        public String lookup(String key) {
            Object data = dataMap.get(key);
            Object dataForPage = extractDataForPage(data, currentPage, totalPages);
            return converter.convert(dataForPage);
        }

        private Object extractDataForPage(Object data, int currentPage, int totalPages) {
            if (data instanceof List<?>) {
                List<?> list = (List<?>) data;
                if (list.size() < 2) {
                    return list;
                }
                else {
                    int arraySize = list.size();
                    int pageSize = ceil(arraySize / (float) totalPages);

                    int start = currentPage * pageSize;
                    int end = Math.min(start + pageSize, arraySize);                                // Last page will be most likely smaller than previous ones.

                    return list.subList(start, end);
                }
            }
            else if (data instanceof Object[]) {
                Object[] array = (Object[]) data;
                if (array.length < 2) {
                    return array;
                }
                else {
                    int arraySize = array.length;
                    int pageSize = ceil(arraySize / (float) totalPages);

                    int start = currentPage * pageSize;
                    int end = Math.min(start + pageSize, arraySize);                                // Last page will be most likely smaller than previous ones.

                    return Arrays.copyOfRange(array, start, end);
                }
            }
            else {
                return data;
            }
        }
    }
}
