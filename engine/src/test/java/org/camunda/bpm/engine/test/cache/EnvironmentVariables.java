package org.camunda.bpm.engine.test.cache;

/**
 * Enables / Disables Caching of the engine and tracking of process engine configurations.
 */
public class EnvironmentVariables {

    public static final boolean ENABLE_CACHE = true; //TODO Externalize this to be an environment variable

    public static boolean enableEngineCache() {
        return ENABLE_CACHE;
    }
}
