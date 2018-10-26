package com.salesforce.androidsyncengine.utils.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This adapter allows to parse both arrays and single items as Strings list.
 *
 * Created by Jakub Stefanowski on 31.03.2017.
 */

public class StringListAdapter extends TypeAdapter<List<String>> {

    @Override
    public void write(JsonWriter out, List<String> list) throws IOException {
        if (list == null) {
            out.nullValue();
            return;
        }

        out.beginArray();
        for (String val : list) {
            out.value(val);
        }
        out.endArray();
    }

    @Override
    public List<String> read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        else if (in.peek() == JsonToken.BEGIN_ARRAY) {
            List<String> list = new ArrayList<String>();
            in.beginArray();

            while (in.hasNext()) {
                String instance = in.nextString();
                list.add(instance);
            }

            in.endArray();
            return list;
        }
        else {
            List<String> list = new ArrayList<String>();
            list.add(in.nextString());

            return list;
        }
    }
}
