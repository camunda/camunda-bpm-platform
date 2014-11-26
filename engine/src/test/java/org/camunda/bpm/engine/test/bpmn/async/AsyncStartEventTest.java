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
package org.camunda.bpm.engine.test.bpmn.async;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricFormField;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;

public class AsyncStartEventTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testAsyncStartEvent() {
    runtimeService.startProcessInstanceByKey("asyncStartEvent");

    Task task = taskService.createTaskQuery().singleResult();
    Assert.assertNull("The user task should not have been reached yet", task);

    Assert.assertEquals(1, runtimeService.createExecutionQuery().activityId("startEvent").count());

    executeAvailableJobs();
    task = taskService.createTaskQuery().singleResult();

    Assert.assertEquals(0, runtimeService.createExecutionQuery().activityId("startEvent").count());

    Assert.assertNotNull("The user task should have been reached", task);
  }

  @Deployment
  public void testAsyncStartEventListeners() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("asyncStartEvent");

    Assert.assertNull(runtimeService.getVariable(instance.getId(), "listener"));

    executeAvailableJobs();

    Assert.assertNotNull(runtimeService.getVariable(instance.getId(), "listener"));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncStartEvent.bpmn20.xml")
  public void testAsyncStartEventHistory() {
    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      runtimeService.startProcessInstanceByKey("asyncStartEvent");

      HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
      Assert.assertNotNull(historicInstance);
      Assert.assertNotNull(historicInstance.getStartTime());

      HistoricActivityInstance historicStartEvent = historyService.createHistoricActivityInstanceQuery().singleResult();
      Assert.assertNull(historicStartEvent);
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncStartEvent.bpmn20.xml")
  public void testAsyncStartEventVariableHistory() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("foo", "bar");
    String processInstanceId = runtimeService.startProcessInstanceByKey("asyncStartEvent", variables).getId();

    VariableInstance variableFoo = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variableFoo);
    assertEquals("foo", variableFoo.getName());
    assertEquals("bar", variableFoo.getValue());

    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    executeAvailableJobs();

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());

    // assert process instance is ended
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().singleResult();
      assertNotNull(variable);
      assertEquals("foo", variable.getVariableName());
      assertEquals("bar", variable.getValue());
      assertEquals(processInstanceId, variable.getActivityInstanceId());

      if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {

        String startEventId = historyService
            .createHistoricActivityInstanceQuery()
            .activityId("startEvent")
            .singleResult()
            .getId();

        HistoricDetail historicDetail = historyService
            .createHistoricDetailQuery()
            .singleResult();

        assertEquals(startEventId, historicDetail.getActivityInstanceId());

      }

    }
  }

  @Deployment
  public void testMultipleAsyncStartEvents() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("foo", "bar");
    runtimeService.correlateMessage("newInvoiceMessage", new HashMap<String, Object>(), variables);

    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    executeAvailableJobs();

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("taskAfterMessageStartEvent", task.getTaskDefinitionKey());

    taskService.complete(task.getId());

    // assert process instance is ended
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testMultipleAsyncStartEvents.bpmn20.xml"})
  public void testMultipleAsyncStartEventsVariableHistory() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("foo", "bar");
    runtimeService.correlateMessage("newInvoiceMessage", new HashMap<String, Object>(), variables);

    VariableInstance variableFoo = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variableFoo);
    assertEquals("foo", variableFoo.getName());
    assertEquals("bar", variableFoo.getValue());

    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    executeAvailableJobs();

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    // assert process instance is ended
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {

      String processInstanceId = historyService
          .createHistoricProcessInstanceQuery()
          .singleResult()
          .getId();

      HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().singleResult();
      assertNotNull(variable);
      assertEquals("foo", variable.getVariableName());
      assertEquals("bar", variable.getValue());
      assertEquals(processInstanceId, variable.getActivityInstanceId());

      if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {

        String theStartActivityInstanceId = historyService
            .createHistoricActivityInstanceQuery()
            .activityId("messageStartEvent")
            .singleResult()
            .getId();

        HistoricDetail historicDetail = historyService
            .createHistoricDetailQuery()
            .singleResult();

        assertEquals(theStartActivityInstanceId, historicDetail.getActivityInstanceId());

      }
    }

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testCallActivity-super.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testCallActivity-sub.bpmn20.xml"
  })
  public void testCallActivity() {
    runtimeService.startProcessInstanceByKey("super");

    ProcessInstance pi = runtimeService
        .createProcessInstanceQuery()
        .processDefinitionKey("sub")
        .singleResult();

    assertTrue(pi instanceof ExecutionEntity);

    assertEquals("theSubStart", ((ExecutionEntity)pi).getActivityId());

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncStartEvent.bpmn20.xml")
  public void testSubmitForm() {

    String processDefinitionId = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("asyncStartEvent")
        .singleResult()
        .getId();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("foo", "bar");

    formService.submitStartForm(processDefinitionId, properties);

    VariableInstance variableFoo = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variableFoo);
    assertEquals("foo", variableFoo.getName());
    assertEquals("bar", variableFoo.getValue());

    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    executeAvailableJobs();

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    // assert process instance is ended
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {

      String processInstanceId = historyService
          .createHistoricProcessInstanceQuery()
          .singleResult()
          .getId();

      HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().singleResult();
      assertNotNull(variable);
      assertEquals("foo", variable.getVariableName());
      assertEquals("bar", variable.getValue());
      assertEquals(processInstanceId, variable.getActivityInstanceId());

      if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {

        String theStartActivityInstanceId = historyService
            .createHistoricActivityInstanceQuery()
            .activityId("startEvent")
            .singleResult()
            .getId();

        HistoricFormField historicFormUpdate = (HistoricFormField) historyService
            .createHistoricDetailQuery()
            .formFields()
            .singleResult();

        assertNotNull(historicFormUpdate);
        assertEquals("bar", historicFormUpdate.getFieldValue());

        HistoricVariableUpdate historicVariableUpdate = (HistoricVariableUpdate) historyService
            .createHistoricDetailQuery()
            .variableUpdates()
            .singleResult();

        assertNotNull(historicVariableUpdate);
        assertEquals(theStartActivityInstanceId, historicVariableUpdate.getActivityInstanceId());
        assertEquals("bar", historicVariableUpdate.getValue());

      }
    }
  }

  /**
   * CAM-2828
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncStartEvent.bpmn20.xml")
  public void FAILING_testSubmitFormHistoricUpdates() {

    String processDefinitionId = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("asyncStartEvent")
        .singleResult()
        .getId();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("foo", "bar");

    formService.submitStartForm(processDefinitionId, properties);
    executeAvailableJobs();

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {

      String theStartActivityInstanceId = historyService
          .createHistoricActivityInstanceQuery()
          .activityId("startEvent")
          .singleResult()
          .getId();

      HistoricDetail historicFormUpdate = historyService
          .createHistoricDetailQuery()
          .formFields()
          .singleResult();

      assertNotNull(historicFormUpdate);
      assertEquals(theStartActivityInstanceId, historicFormUpdate.getActivityInstanceId());

    }
  }

}
