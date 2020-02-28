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
package org.camunda.bpm.engine.impl.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;

import ch.qos.logback.classic.Level;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class BatchInvocationsPerJobByBatchTypeConfigTest {

  protected static final String PROCESS_ENGINE_CONFIG =
      "camunda.cfg.invocationsPerJobByBatchType.xml";

  protected static final String CONFIG_LOGGER = "org.camunda.bpm.engine.cfg";

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch(CONFIG_LOGGER)
      .level(Level.WARN);

  protected ProcessEngine processEngine;
  protected ProcessEngineConfigurationImpl engineConfiguration;

  @Before
  public void setup() {
    processEngine = ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource(PROCESS_ENGINE_CONFIG)
        .buildProcessEngine();

    engineConfiguration =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
  }

  @After
  public void teardown() {
    processEngine.close();
  }

  @Test
  public void shouldSetInvocationsPerJobByBatchType() {
    // when
    Map<String, Integer> invocationsPerBatchJobByBatchType =
        engineConfiguration.getInvocationsPerBatchJobByBatchType();

    // then
    assertThat(invocationsPerBatchJobByBatchType)
        .containsExactly(
            entry(Batch.TYPE_PROCESS_INSTANCE_MIGRATION, 7),
            entry(Batch.TYPE_PROCESS_INSTANCE_MODIFICATION, 3),
            entry("custom-batch-operation", 42)
        );
  }

  @Test
  public void shouldWriteLogWhenBatchTypeIsUnknown() {
    // then
    assertThat(loggingRule.getFilteredLog("ENGINE-12014 The configuration property " +
        "'invocationsPerJobByBatchType' contains an invalid batch type 'custom-batch-operation' " +
        "which is neither a custom nor a built-in batch type")).hasSize(1);
  }

}
