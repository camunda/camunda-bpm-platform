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
package org.camunda.bpm.engine.impl.cfg;

import static org.assertj.core.api.Assertions.*;
import static org.camunda.bpm.engine.impl.ProcessEngineLogger.CONFIG_LOGGER;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class ProcessEngineConfigurationTest {

  private ProcessEngineConfigurationImpl engineConfiguration;
  private ConfigurationLogger logger;
  private static final int SERIALIZABLE_VALUE = Connection.TRANSACTION_SERIALIZABLE;
  private static final String SERIALIZABLE_NAME = "SERIALIZABLE";
  public static final ProcessEngineException EXPECTED_EXCEPTION = CONFIG_LOGGER.invalidTransactionIsolationLevel(SERIALIZABLE_NAME);

  @Before
  public void setUp() {
    this.engineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault();
    this.logger = mock(ConfigurationLogger.class);
    when(logger.invalidTransactionIsolationLevel(SERIALIZABLE_NAME)).thenReturn(EXPECTED_EXCEPTION);
    engineConfiguration.initDataSource(); // initialize the datasource for the first time so we can modify the level
    ProcessEngineConfigurationImpl.LOG = logger;
  }

  @AfterClass
  public static void cleanUp() {
    ProcessEngineConfigurationImpl.LOG = CONFIG_LOGGER;
  }

  @Test
  public void shouldEnableStandaloneTasksByDefault() {
    // when
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();

    // then
    assertThat(engineConfiguration.isStandaloneTasksEnabled()).isTrue();
  }

  @Test
  public void shouldEnableImplicitUpdatesDetectionByDefault() {
    // when
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
    // then
    assertThat(engineConfiguration.isImplicitVariableUpdateDetectionEnabled()).isTrue();
  }

  @Test
  public void validIsolationLevel() {
    // given
    ((PooledDataSource) engineConfiguration.getDataSource()).setDefaultTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
    // when
    engineConfiguration.initDataSource();
    // then no exception
  }

  @Test
  public void invalidIsolationLevelWithSkipFlagDisabled() {
    // given
    ((PooledDataSource) engineConfiguration.getDataSource()).setDefaultTransactionIsolationLevel(SERIALIZABLE_VALUE);
    // when then
    assertThatThrownBy(() -> engineConfiguration.initDataSource())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessage(EXPECTED_EXCEPTION.getMessage());
  }

  @Test
  public void invalidIsolationLevelWithSkipFlagEnabled() {
    // given
    ((PooledDataSource) engineConfiguration.getDataSource()).setDefaultTransactionIsolationLevel(SERIALIZABLE_VALUE);
    engineConfiguration.setSkipIsolationLevelCheck(true);
    // when
    engineConfiguration.initDataSource();
    // then
    verify(logger).logSkippedIsolationLevelCheck(SERIALIZABLE_NAME);
  }

  @Test
  public void validIsolationLevelPropertyFromFileIsSetCorrectly() {
    // given
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource("camunda.cfg.skipIsolationLevelCheckEnabled.xml");
    // then
    assertTrue(engineConfiguration.skipIsolationLevelCheck);
  }
}
