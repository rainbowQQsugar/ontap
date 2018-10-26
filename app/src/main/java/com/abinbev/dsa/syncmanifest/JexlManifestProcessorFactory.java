package com.abinbev.dsa.syncmanifest;

import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessor;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessorFactory;

/**
 * Created by Jakub Stefanowski on 31.03.2017.
 */

public class JexlManifestProcessorFactory extends ManifestProcessorFactory {
    @Override
    public ManifestProcessor createProcessor() {
        return new JexlManifestProcessor();
    }
}
