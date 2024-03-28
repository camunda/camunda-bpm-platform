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
package org.camunda.bpm.engine.test.standalone.testing;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cmd.GetDatabaseCountsCmd;
import org.camunda.bpm.engine.impl.management.DatabaseContentReport;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/*
 * CAUTION: The test order is important for this test.
 * Test run_order_1_shouldLeaveDbDirty will create data which is not cleaned up.
 * Test run_order_2_shouldNotFailDueToDirtyDb will be executed afterwards and should have a clean database.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProcessEngineRuleCleanupAfterTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  private RuntimeService runtimeService;
  private RepositoryService repositoryService;

  public static final BpmnModelInstance ONE_TASK_PROCESS = Bpmn.createExecutableProcess("oneTaskProcess")
      .startEvent()
      .userTask("userTask")
      .endEvent()
      .done();

  @Before
  public void setup() {
    runtimeService = engineRule.getRuntimeService();
    repositoryService = engineRule.getRepositoryService();
  }

  @Test
  public void run_order_1_shouldLeaveDbDirty() {
    //given some content in the database
    repositoryService.createDeployment().addModelInstance("oneTaskProcess.bpmn", ONE_TASK_PROCESS).deploy();
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // then data should be silently cleaned, no exception expected
  }

  @Test
  public void run_order_2_shouldNotFailDueToDirtyDb() {
    // given after run_order_1_shouldLeaveDbDirty was executed, the database is clean for this test

    // then
    DatabaseContentReport databaseContentReport = engineRule.getProcessEngineConfiguration()
        .getCommandExecutorTxRequired()
        .execute(new GetDatabaseCountsCmd());

    assertThat(databaseContentReport.isDatabaseClean(true)).isTrue();
  }

}
