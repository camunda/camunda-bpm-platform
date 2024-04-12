package org.camunda.bpm.engine.test.util;

import org.camunda.bpm.engine.test.cache.EnvironmentVariables;
import org.camunda.bpm.engine.test.cache.ObjectChangeTracker;
import org.camunda.bpm.engine.test.cache.TestContext;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class ConfigurableCacheProcessEngineRule extends TestWatcher {

    private static final boolean ENABLE_CACHE = EnvironmentVariables.enableEngineCache();

    @Override
    protected void starting(Description description) {
        // This method will be called before each test method starts
        System.out.println("Starting test: " + description.getMethodName());

        if (ENABLE_CACHE) {
            fetchCurrentConfigAndTrack();
        }

        super.starting(description);
    }

    @Override
    protected void finished(Description description) {
        // This method will be called after each test method finishes
        System.out.println("Finishing test: " + description.getMethodName());

        if (ENABLE_CACHE) {
            restoreAndClearCurrentConfig();
        }
    }

    protected void fetchCurrentConfigAndTrack() {
        var testContext = TestContext.getInstance();
        var currentConfig = testContext.getCurrentExecutionConfig();

        var configTracker = ObjectChangeTracker.of(currentConfig);
        testContext.setConfigTracker(configTracker);
    }

    protected void restoreAndClearCurrentConfig() {
        var testContext = TestContext.getInstance();
        var configTracker = testContext.getConfigTracker();

        configTracker.restoreFields();
        configTracker.clear();

        testContext.clearCurrentExecutionConfig();
    }
}
