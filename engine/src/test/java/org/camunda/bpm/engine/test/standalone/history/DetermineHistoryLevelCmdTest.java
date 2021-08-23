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
package org.camunda.bpm.engine.test.standalone.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cmd.DetermineHistoryLevelCmd;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.junit.After;
import org.junit.Test;

public class DetermineHistoryLevelCmdTest {

  private ProcessEngineImpl processEngineImpl;

  private static ProcessEngineConfigurationImpl config(final String schemaUpdate, final String historyLevel) {
    StandaloneInMemProcessEngineConfiguration engineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration.setProcessEngineName(UUID.randomUUID().toString());
    engineConfiguration.setDatabaseSchemaUpdate(schemaUpdate);
    engineConfiguration.setHistory(historyLevel);
    engineConfiguration.setDbMetricsReporterActivate(false);
    engineConfiguration.setJdbcUrl("jdbc:h2:mem:DetermineHistoryLevelCmdTest");

    return engineConfiguration;
  }


  @Test
  public void readLevelFullfromDB() throws Exception {
    final ProcessEngineConfigurationImpl config = config("true", ProcessEngineConfiguration.HISTORY_FULL);

    // init the db with level=full
    processEngineImpl = (ProcessEngineImpl) config.buildProcessEngine();

    HistoryLevel historyLevel = config.getCommandExecutorSchemaOperations().execute(new DetermineHistoryLevelCmd(config.getHistoryLevels()));

    assertThat(historyLevel).isEqualTo(HistoryLevel.HISTORY_LEVEL_FULL);
  }


  @Test
  public void useDefaultLevelAudit() throws Exception {
    ProcessEngineConfigurationImpl config = config("true", ProcessEngineConfiguration.HISTORY_AUTO);

    // init the db with level=auto -> audit
    processEngineImpl = (ProcessEngineImpl) config.buildProcessEngine();
    // the history Level has been overwritten with audit
    assertThat(config.getHistoryLevel()).isEqualTo(HistoryLevel.HISTORY_LEVEL_AUDIT);

    // and this is written to the database
    HistoryLevel databaseLevel =
        config.getCommandExecutorSchemaOperations().execute(new DetermineHistoryLevelCmd(config.getHistoryLevels()));
    assertThat(databaseLevel).isEqualTo(HistoryLevel.HISTORY_LEVEL_AUDIT);
  }

  @Test
  public void failWhenExistingHistoryLevelIsNotRegistered() {
    // init the db with custom level
    HistoryLevel customLevel = new HistoryLevel() {
      @Override
      public int getId() {
        return 99;
      }

      @Override
      public String getName() {
        return "custom";
      }

      @Override
      public boolean isHistoryEventProduced(HistoryEventType eventType, Object entity) {
        return false;
      }
    };
    ProcessEngineConfigurationImpl config = config("true", "custom");
    config.setCustomHistoryLevels(Arrays.asList(customLevel));
    processEngineImpl = (ProcessEngineImpl) config.buildProcessEngine();

    // when/then
    assertThatThrownBy(() -> config.getCommandExecutorSchemaOperations().execute(
        new DetermineHistoryLevelCmd(Collections.<HistoryLevel>emptyList())))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("The configured history level with id='99' is not registered in this config.");
  }

  @After
  public void after() {
    processEngineImpl.close();
    processEngineImpl = null;
  }
}