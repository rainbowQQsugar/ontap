package com.salesforce.androidsyncengine.syncmanifest;

/**
 * Created by Jakub Stefanowski on 31.03.2017.
 */

public abstract class ManifestProcessorFactory {

    private static final ManifestProcessorFactory EMPTY_INSTANCE = new ManifestProcessorFactory() {
        @Override
        public ManifestProcessor createProcessor() {
            return ManifestProcessor.EMPTY;
        }
    };

    private static ManifestProcessorFactory instance = EMPTY_INSTANCE;

    public static void setInstance(ManifestProcessorFactory factory) {
        synchronized (ManifestProcessorFactory.class) {
            instance = factory == null ? EMPTY_INSTANCE : factory;
        }
    }

    public static ManifestProcessorFactory getInstance() {
        return instance;
    }

    public abstract ManifestProcessor createProcessor();
}
