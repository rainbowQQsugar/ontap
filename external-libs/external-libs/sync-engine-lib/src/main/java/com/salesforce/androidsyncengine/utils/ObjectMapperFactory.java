package com.salesforce.androidsyncengine.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Jakub Stefanowski on 04.04.2017.
 */

public final class ObjectMapperFactory {

    private ObjectMapperFactory() {}

    public static ObjectMapper createMapper() {
        return new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
