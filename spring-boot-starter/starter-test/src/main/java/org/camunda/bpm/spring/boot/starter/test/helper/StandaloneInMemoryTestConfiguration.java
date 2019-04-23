/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.spring.boot.starter.test.helper;


import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.MockExpressionManager;
import org.camunda.bpm.engine.impl.history.HistoryLevel;

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
    historyLevel = HistoryLevel.HISTORY_LEVEL_FULL;

    getProcessEnginePlugins().addAll(plugins);
  }

  public ProcessEngineRule rule() {
    return new ProcessEngineRule(buildProcessEngine());
  }
}
