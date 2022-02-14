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
package org.camunda.bpm.engine.test.api.cfg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Svetlana Dorokhova.
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class PersistenceExceptionTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private RuntimeService runtimeService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
  }

  @Test
  public void testPersistenceExceptionContainsRealCause() {
    Assume.assumeFalse(engineRule.getProcessEngineConfiguration().getDatabaseType().equals(DbSqlSessionFactory.MARIADB));
    StringBuffer longString = new StringBuffer();
    for (int i = 0; i < 100; i++) {
      longString.append("tensymbols");
    }
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process1").startEvent().userTask(longString.toString()).endEvent().done();
    testRule.deploy(modelInstance);
    try {
      runtimeService.startProcessInstanceByKey("process1").getId();
      fail("persistence exception is expected");
    } catch (ProcessEngineException ex) {
      Throwable cause = ex.getCause();
      assertThat(cause.getMessage()).contains("insertHistoricTaskInstanceEvent");
    }
  }

}
