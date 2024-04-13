package org.camunda.bpm.engine.test;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.cache.EnvironmentVariables;
import org.camunda.bpm.engine.test.cache.ObjectChangeTracker;
import org.camunda.bpm.engine.test.cache.TestContext;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurableCacheProcessEngineRule extends TestWatcher {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurableCacheProcessEngineRule.class);

    private static final boolean ENABLE_CACHE = EnvironmentVariables.enableEngineCache();

    @Override
    protected void starting(Description description) {
        // This method will be called before each test method starts
        LOG.info("Starting test: {}", description.getMethodName());

        if (ENABLE_CACHE && currentConfigIsSet()) {
            fetchCurrentConfigAndTrack();
        }

        super.starting(description);
    }

    @Override
    protected void finished(Description description) {
        // This method will be called after each test method finishes
        LOG.info("Finishing test: {}", description.getMethodName());

        if (ENABLE_CACHE && currentConfigIsSet()) {
            restoreAndClearCurrentConfig();
        }
    }

    protected void fetchCurrentConfigAndTrack() {
        var testContext = TestContext.getInstance();
        var currentConfig = testContext.getCurrentExecutionConfig();
        var customConfig = testContext.getCustomConfig();

        if (customConfig != null) {
            customConfig.accept((ProcessEngineConfigurationImpl) currentConfig);
        }

        var configTracker = ObjectChangeTracker.of(currentConfig);
        testContext.setConfigTracker(configTracker);
    }

    protected void restoreAndClearCurrentConfig() {
        var testContext = TestContext.getInstance();
        var configTracker = testContext.getConfigTracker();

        configTracker.restoreFields();
        configTracker.clear();

        clearCurrentExecutions();
    }

    protected void clearCurrentExecutions() {
        var testContext = TestContext.getInstance();

        testContext.clearCurrentExecutionConfig();
        testContext.clearCustomConfig();
    }

    protected boolean currentConfigIsSet() {
        var testContext = TestContext.getInstance();
        return testContext.getCurrentExecutionConfig() != null;
    }
}
