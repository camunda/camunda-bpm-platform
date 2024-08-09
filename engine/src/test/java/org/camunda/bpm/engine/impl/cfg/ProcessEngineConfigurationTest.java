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
import static org.camunda.bpm.engine.ProcessEngineConfiguration.TRANSACTION_ISOLATION_LEVEL_DEFAULT;
import static org.camunda.bpm.engine.impl.ProcessEngineLogger.CONFIG_LOGGER;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class ProcessEngineConfigurationTest {

  private ProcessEngineConfigurationImpl engineConfiguration;
  private ConfigurationLogger logger;

  @Before
  public void setUp() {
    this.engineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault();
    this.logger = mock(ConfigurationLogger.class);
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
  public void validIsolationLevelPropertyIsSetCorrectly() {
    //given
    engineConfiguration.setTransactionIsolationLevel("readUncommitted");

    //when
    engineConfiguration.initDataSource();

    //then
    assertEquals(Connection.TRANSACTION_READ_UNCOMMITTED, engineConfiguration.getCurrentTransactionIsolationLevel().intValue());
    verify(logger).logNonOptimalIsolationLevel("readUncommitted", TRANSACTION_ISOLATION_LEVEL_DEFAULT);
  }

  @Test
  public void validIsolationLevelPropertyFromFileIsSetCorrectly() {
    //given
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("camunda.cfg.customIsolationLevel.xml");

    //when
    engineConfiguration.initDataSource();

    //then
    assertEquals(Connection.TRANSACTION_SERIALIZABLE, engineConfiguration.getCurrentTransactionIsolationLevel().intValue());
    verify(logger).logNonOptimalIsolationLevel("serializable", TRANSACTION_ISOLATION_LEVEL_DEFAULT);
  }

  @Test
  public void unsetIsolationLevelPropertyDefaultsToExpectedValue() {
    //when
    engineConfiguration.initDataSource();

    //then
    assertEquals("readCommitted", engineConfiguration.getTransactionIsolationLevel());
    assertEquals(Connection.TRANSACTION_READ_COMMITTED, engineConfiguration.getCurrentTransactionIsolationLevel().intValue());
    verify(logger, times(0)).logNonOptimalIsolationLevel(anyString(), anyString());
  }

  @Test
  public void nullIsolationLevelPropertyIsHandledCorrectly() {
    //given
    engineConfiguration.setTransactionIsolationLevel(null);

    //when
    engineConfiguration.initDataSource();

    //then
    assertEquals(Connection.TRANSACTION_READ_COMMITTED, engineConfiguration.getCurrentTransactionIsolationLevel().intValue());
    verify(logger, times(0)).logNullIsolationLevel(TRANSACTION_ISOLATION_LEVEL_DEFAULT);
  }

  @Test
  public void blankIsolationLevelPropertyIsHandledCorrectly() {
    //given
    engineConfiguration.setTransactionIsolationLevel("   ");

    //when
    engineConfiguration.initDataSource();

    //then
    assertEquals(Connection.TRANSACTION_READ_COMMITTED, engineConfiguration.getCurrentTransactionIsolationLevel().intValue());
    verify(logger, times(0)).logNullIsolationLevel(TRANSACTION_ISOLATION_LEVEL_DEFAULT);
  }

  @Test
  public void invalidIsolationLevelPropertyIsHandledCorrectly() {
    //given
    String propertyName = "transactionIsolationLevel";
    String propertyValue = "invalid";
    ProcessEngineException expectedException = CONFIG_LOGGER.invalidPropertyValue(propertyName, propertyValue);
    engineConfiguration.setTransactionIsolationLevel(propertyValue);
    when(logger.invalidPropertyValue(anyString(), anyString())).thenReturn(expectedException);

    //when then
    assertThatThrownBy(() -> engineConfiguration.initDataSource()).isEqualTo(expectedException);
    verify(logger).invalidPropertyValue(propertyName, propertyValue);
  }
}
