package org.camunda.bpm.spring.boot.starter.test.helper;


import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.MockExpressionManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Default in memory configuration, pre-configured with mock, dbSchema and metrics.
 */
public class StandaloneInMemoryTestConfiguration extends StandaloneInMemProcessEngineConfiguration {

  public StandaloneInMemoryTestConfiguration(ProcessEnginePlugin... plugins) {
    this(Optional.ofNullable(plugins)
      .map(Arrays::asList)
      .orElse(Collections.EMPTY_LIST)
    );
  }

  public StandaloneInMemoryTestConfiguration(List<ProcessEnginePlugin> plugins) {
    jobExecutorActivate = false;
    expressionManager = new MockExpressionManager();
    databaseSchemaUpdate = DB_SCHEMA_UPDATE_DROP_CREATE;
    isDbMetricsReporterActivate = false;

    getProcessEnginePlugins().addAll(plugins);
  }

  public ProcessEngineRule rule() {
    return new ProcessEngineRule(buildProcessEngine());
  }
}
