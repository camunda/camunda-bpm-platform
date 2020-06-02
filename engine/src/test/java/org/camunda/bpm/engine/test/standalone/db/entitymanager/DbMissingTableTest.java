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
package org.camunda.bpm.engine.test.standalone.db.entitymanager;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;

import ch.qos.logback.classic.Level;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;


public class DbMissingTableTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch("org.camunda.bpm.engine.persistence");

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule).around(loggingRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;

  protected boolean jdbcBatchProcessingVal;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    jdbcBatchProcessingVal = processEngineConfiguration.isJdbcBatchProcessing();
    processEngineConfiguration.setJdbcBatchProcessing(true);
  }

  @After
  public void tearDown() {
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();
    processEngineConfiguration.setJdbcBatchProcessing(jdbcBatchProcessingVal);
  }

  @Test
  public void shouldReportMissingDbTableInLogs() {
    // given
    exceptionRule.expect(ProcessEngineException.class);
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("timerProcess")
        .startEvent()
        .userTask("user_task")
          .boundaryEvent("timer")
            .cancelActivity(false)
            .timerWithCycle("R5/PT5M")
          .endEvent()
          .moveToActivity("user_task")
        .endEvent()
        .done();
    try {
      Connection connection = processEngineConfiguration.getDataSource().getConnection();
      connection.createStatement()
          .execute("ALTER TABLE ACT_RU_JOB DROP COLUMN REPEAT_OFFSET_");
      connection.close();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
    testRule.deploy(modelInstance);

    // when
    runtimeService.startProcessInstanceByKey("timerProcess");

    // then
    assertThat(loggingRule.getFilteredLog("ENGINE-03083").get(0).getMessage())
        .containsIgnoringCase("REPEAT_OFFSET_");
  }

}