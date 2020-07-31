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
package org.camunda.bpm.engine.test.api.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP;

import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class HistoryCleanupDisabledOnBootstrapTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
      configuration.setJdbcUrl("jdbc:h2:mem:" + HistoryCleanupDisabledOnBootstrapTest.class.getSimpleName());
      configuration.setHistoryCleanupEnabled(false);
      configuration.setHistoryCleanupBatchWindowStartTime("12:00");
      configuration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_CREATE_DROP);
  });

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  public HistoryService historyService;

  public static ProcessEngineConfigurationImpl engineConfiguration;

  @Before
  public void assignServices() {
    historyService = engineRule.getHistoryService();
    engineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @After
  public void resetConfig() {
    engineConfiguration.setHistoryCleanupEnabled(true);
  }

  @Test
  public void shouldNotCreateJobs() {
    // given

    // when
    List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();

    // then
    assertThat(historyCleanupJobs).isEmpty();
  }

}
