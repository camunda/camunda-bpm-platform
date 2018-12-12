/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.test.api.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class SetBusinessKeyTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testRule);

  protected static final String PROCESS_KEY = "process";

  protected static final BpmnModelInstance SYNC_SERVICE_TASK_PROCESS =
      Bpmn.createExecutableProcess(PROCESS_KEY)
      .startEvent("startEvent")
      .serviceTask()
        .camundaClass(SetBusinessKeyDelegate.class)
      .userTask("userTask2")
      .endEvent("endEvent")
      .done();

  protected static final BpmnModelInstance ASYNC_SERVICE_TASK_PROCESS =
      Bpmn.createExecutableProcess(PROCESS_KEY)
      .startEvent("startEvent")
      .serviceTask()
        .camundaAsyncBefore()
        .camundaClass(SetBusinessKeyDelegate.class)
      .userTask("userTask2")
      .endEvent("endEvent")
      .done();

  protected static final String BUSINESS_KEY_VARIABLE = "businessKeyVar";
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ManagementService managementService;

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
    historyService = rule.getHistoryService();
    managementService = rule.getManagementService();
  }

  @Test
  public void testNewKeyInSyncServiceTask() {
    // given
    testRule.deploy(SYNC_SERVICE_TASK_PROCESS);

    // when
    String newBusinessKeyValue = "newBusinessKey";
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, Variables.createVariables().putValue(BUSINESS_KEY_VARIABLE, newBusinessKeyValue));

    // then
    checkBusinessKeyChanged(newBusinessKeyValue);
  }

  @Test
  public void testNewKeyInAsyncServiceTask() {
    // given
    testRule.deploy(ASYNC_SERVICE_TASK_PROCESS);

    String newBusinessKeyValue = "newBusinessKey";
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, Variables.createVariables().putValue(BUSINESS_KEY_VARIABLE, newBusinessKeyValue));

    // when
    executeJob();

    // then
    checkBusinessKeyChanged(newBusinessKeyValue);
  }

  @Test
  public void testNewKeyInStartExecListener() {
    // given
    String listener = ExecutionListener.EVENTNAME_START;
    BpmnModelInstance process = createModelExecutionListener(listener);
    testRule.deploy(process);

    // when
    String newBusinessKeyValue = "newBusinessKey";
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, Variables.createVariables().putValue(BUSINESS_KEY_VARIABLE, newBusinessKeyValue));

    // then
    checkBusinessKeyChanged(newBusinessKeyValue);
  }

  @Test
  public void testNewKeyInEndExecListener() {
    // given
    String listener = ExecutionListener.EVENTNAME_END;
    BpmnModelInstance process = createModelExecutionListener(listener);
    testRule.deploy(process);

    String newBusinessKeyValue = "newBusinessKey";
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, Variables.createVariables().putValue(BUSINESS_KEY_VARIABLE, newBusinessKeyValue));

    completeTask("userTask1");

    // assume
    assertNotNull(taskService.createTaskQuery().taskDefinitionKey("userTask2").singleResult());

    // then
    checkBusinessKeyChanged(newBusinessKeyValue);
  }


  @Test
  public void testNewKeyInStartTaskListener() {
    // given
    String listener = TaskListener.EVENTNAME_CREATE;
    BpmnModelInstance process = createModelTaskListener(listener);
    testRule.deploy(process);

    // when
    String newBusinessKeyValue = "newBusinessKey";
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, Variables.createVariables().putValue(BUSINESS_KEY_VARIABLE, newBusinessKeyValue));

    // then
    checkBusinessKeyChanged(newBusinessKeyValue);
  }

  @Test
  public void testNewKeyInEndTaskListener() {
    // given
    String listener = TaskListener.EVENTNAME_COMPLETE;
    BpmnModelInstance process = createModelTaskListener(listener);
    testRule.deploy(process);

    String newBusinessKeyValue = "newBusinessKey";
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, Variables.createVariables().putValue(BUSINESS_KEY_VARIABLE, newBusinessKeyValue));

    completeTask("userTask1");

    // assume
    assertNotNull(taskService.createTaskQuery().taskDefinitionKey("userTask2").singleResult());

    // then
    checkBusinessKeyChanged(newBusinessKeyValue);
  }

  @Test
  public void testUpdateKeyInSyncServiceTask() {
    // given
    testRule.deploy(SYNC_SERVICE_TASK_PROCESS);

    // when
    String newBusinessKeyValue = "newBusinessKey";
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, "aBusinessKey", Variables.createVariables().putValue(BUSINESS_KEY_VARIABLE, newBusinessKeyValue));

    // then
    checkBusinessKeyChanged(newBusinessKeyValue);
  }

  @Test
  public void testUpdateKeyInAsyncServiceTask() {
    // given
    testRule.deploy(ASYNC_SERVICE_TASK_PROCESS);

    String newBusinessKeyValue = "newBusinessKey";
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, "aBusinessKey", Variables.createVariables().putValue(BUSINESS_KEY_VARIABLE, newBusinessKeyValue));

    // when
    executeJob();

    // then
    checkBusinessKeyChanged(newBusinessKeyValue);
  }

  @Test
  public void testUpdateKeyInStartExecListener() {
    // given
    String listener = ExecutionListener.EVENTNAME_START;
    BpmnModelInstance process = createModelExecutionListener(listener);
    testRule.deploy(process);

    // when
    String newBusinessKeyValue = "newBusinessKey";
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, "aBusinessKey", Variables.createVariables().putValue(BUSINESS_KEY_VARIABLE, newBusinessKeyValue));

    // then
    checkBusinessKeyChanged(newBusinessKeyValue);
  }

  @Test
  public void testUpdateKeyInEndExecListener() {
    // given
    String listener = ExecutionListener.EVENTNAME_END;
    BpmnModelInstance process = createModelExecutionListener(listener);
    testRule.deploy(process);

    String newBusinessKeyValue = "newBusinessKey";
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, "aBusinessKey", Variables.createVariables().putValue(BUSINESS_KEY_VARIABLE, newBusinessKeyValue));

    completeTask("userTask1");

    // assume
    assertNotNull(taskService.createTaskQuery().taskDefinitionKey("userTask2").singleResult());

    // then
    checkBusinessKeyChanged(newBusinessKeyValue);
  }

  @Test
  public void testUpdateKeyInEndTaskListener() {
    // given
    String listener = TaskListener.EVENTNAME_COMPLETE;
    BpmnModelInstance process = createModelTaskListener(listener);
    testRule.deploy(process);

    String newBusinessKeyValue = "newBusinessKey";
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, "aBusinessKey", Variables.createVariables().putValue(BUSINESS_KEY_VARIABLE, newBusinessKeyValue));

    // when
    completeTask("userTask1");

    // assume
    assertNotNull(taskService.createTaskQuery().taskDefinitionKey("userTask2").singleResult());

    // then
    checkBusinessKeyChanged(newBusinessKeyValue);
  }

  @Test
  public void testUpdateKeyNullValueInStartTaskListener() {
    // given
    String listener = TaskListener.EVENTNAME_CREATE;
    BpmnModelInstance process = createModelTaskListener(listener);
    testRule.deploy(process);

    // when
    String newBusinessKeyValue = null;
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, "aBusinessKey", Variables.createVariables().putValue(BUSINESS_KEY_VARIABLE, newBusinessKeyValue));

    // then
    checkBusinessKeyChanged(newBusinessKeyValue);
  }

  protected void checkBusinessKeyChanged(String newBusinessKeyValue) {
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).singleResult();
    assertNotNull(processInstance);
    assertEquals(newBusinessKeyValue, processInstance.getBusinessKey());

    HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertNotNull(historicInstance);
    assertEquals(newBusinessKeyValue, historicInstance.getBusinessKey());
  }

  protected BpmnModelInstance createModelExecutionListener(String listener) {
    return Bpmn.createExecutableProcess(PROCESS_KEY)
    .startEvent("startEvent")
    .userTask("userTask1").name("User task")
      .camundaExecutionListenerExpression(listener,
            "${execution.setProcessBusinessKey(execution.getVariable(\"" + BUSINESS_KEY_VARIABLE + "\"))}")
    .userTask("userTask2")
    .endEvent("endEvent")
    .done();
  }

  protected BpmnModelInstance createModelTaskListener(String listener) {
    return Bpmn.createExecutableProcess(PROCESS_KEY)
    .startEvent("startEvent")
    .userTask("userTask1").name("User task")
      .camundaTaskListenerClass(listener, SetBusinessKeyListener.class)
    .userTask("userTask2")
    .endEvent("endEvent")
    .done();
  }

  protected void executeJob() {
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());
  }

  protected void completeTask(String key) {
    Task task = taskService.createTaskQuery().taskDefinitionKey(key).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
  }

  public static class SetBusinessKeyDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      String newKeyValue = (String) execution.getVariable(BUSINESS_KEY_VARIABLE);
      execution.setProcessBusinessKey(newKeyValue);
    }

  }

  public static class SetBusinessKeyListener implements TaskListener {

    public void notify(DelegateTask delegateTask) {
      DelegateExecution execution = delegateTask.getExecution();
      String newKeyValue = (String) execution.getVariable(BUSINESS_KEY_VARIABLE);
      execution.setProcessBusinessKey(newKeyValue);
    }

  }
}
