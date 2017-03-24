/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.history;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author Svetlana Dorokhova
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricDetailQueryTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testHelper);


  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;
  protected TaskService taskService;
  private IdentityService identityService;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    identityService = engineRule.getIdentityService();
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByUserOperationId() {
    startProcessInstance(PROCESS_KEY);

    identityService.setAuthenticatedUserId("demo");

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.resolveTask(taskId, getVariables());

    //then
    String userOperationId = historyService.createHistoricDetailQuery().singleResult().getUserOperationId();

    HistoricDetailQuery query = historyService.createHistoricDetailQuery()
            .userOperationId(userOperationId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByInvalidUserOperationId() {
    startProcessInstance(PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.resolveTask(taskId, getVariables());

    //then
    HistoricDetailQuery query = historyService.createHistoricDetailQuery()
            .userOperationId("invalid");

    assertEquals(0, query.list().size());
    assertEquals(0, query.count());

    try {
      query.userOperationId(null);
      fail("It was possible to set a null value as userOperationId.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByExecutionId() {
    startProcessInstance(PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.resolveTask(taskId, getVariables());

    //then
    String executionId = historyService.createHistoricDetailQuery().singleResult().getExecutionId();

    HistoricDetailQuery query = historyService.createHistoricDetailQuery()
            .executionId(executionId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryByInvalidExecutionId() {
    startProcessInstance(PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.resolveTask(taskId, getVariables());

    //then
    HistoricDetailQuery query = historyService.createHistoricDetailQuery()
            .executionId("invalid");

    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }


  protected VariableMap getVariables() {
    return Variables.createVariables()
            .putValue("aVariableName", "aVariableValue");
  }

  protected void startProcessInstance(String key) {
    startProcessInstances(key, 1);
  }

  protected void startProcessInstances(String key, int numberOfInstances) {
    for (int i = 0; i < numberOfInstances; i++) {
      runtimeService.startProcessInstanceByKey(key);
    }

    testHelper.executeAvailableJobs();
  }
}
