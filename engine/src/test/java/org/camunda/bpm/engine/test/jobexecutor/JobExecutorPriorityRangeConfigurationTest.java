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
package org.camunda.bpm.engine.test.jobexecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JobExecutorPriorityRangeConfigurationTest {

  ProcessEngineConfigurationImpl config;

  protected Long defaultJobExecutorPriorityRangeMin;
  protected Long defaultJobExecutorPriorityRangeMax;

  @Before
  public void setup() {
    config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource("camunda.cfg.xml");
    defaultJobExecutorPriorityRangeMin = config.getJobExecutorPriorityRangeMin();
    defaultJobExecutorPriorityRangeMax = config.getJobExecutorPriorityRangeMax();
  }

  @After
  public void tearDown() {
    config.setJobExecutorPriorityRangeMin(defaultJobExecutorPriorityRangeMin);
    config.setJobExecutorPriorityRangeMax(defaultJobExecutorPriorityRangeMax);
  }

  @Test
  public void shouldAcceptValidPriorityRangeConfiguration() {
    // given
    config.setJobExecutorPriorityRangeMin(10L);
    config.setJobExecutorPriorityRangeMax(10L);

    // when
    config.buildProcessEngine();

    // then
    assertThat(config.getJobExecutorPriorityRangeMin()).isEqualTo(10L);
    assertThat(config.getJobExecutorPriorityRangeMax()).isEqualTo(10L);
  }

  @Test
  public void shouldAllowNegativePriorityRangeConfiguration() {
    // given
    config.setJobExecutorPriorityRangeMin(-10L);
    config.setJobExecutorPriorityRangeMax(-5);

    // when
    config.buildProcessEngine();

    // then
    // no exception
  }

  @Test
  public void shouldThrowExceptionOnNegativeMaxPriorityRangeConfiguration() {
    // given
    config.setJobExecutorPriorityRangeMin(0L);
    config.setJobExecutorPriorityRangeMax(-10L);

    // then
    assertThatThrownBy(() -> {
      config.buildProcessEngine();
    }).isInstanceOf(ProcessEngineException.class)
    .hasMessage("ENGINE-14031 Invalid configuration for job executor priority range. Reason: jobExecutorPriorityRangeMin can not be greater than jobExecutorPriorityRangeMax");
  }

  @Test
  public void shouldThrowExceptionJobExecutorPriorityMinLargerThanMax() {
    // given
    config.setJobExecutorPriorityRangeMin(10L);
    config.setJobExecutorPriorityRangeMax(5L);

    // then
    assertThatThrownBy(() -> {
      config.buildProcessEngine();
    }).isInstanceOf(ProcessEngineException.class)
    .hasMessage("ENGINE-14031 Invalid configuration for job executor priority range. Reason: jobExecutorPriorityRangeMin can not be greater than jobExecutorPriorityRangeMax");
  }
}
