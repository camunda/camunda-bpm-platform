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
package org.camunda.bpm.engine.test.api.runtime;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Svetlana Dorokhova.
 */
public class ProcessInstanceModificationSubProcessTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  private RuntimeService runtimeService;
  private RepositoryService repositoryService;
  private TaskService taskService;
  private HistoryService historyService;

  @Before
  public void init() {
    repositoryService = rule.getRepositoryService();
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
    historyService = rule.getHistoryService();
  }

  @Ignore("CAM-9354")
  @Test
  public void shouldHaveEqualParentActivityInstanceId() {
    // given
    testHelper.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
        .subProcess("subprocess").embeddedSubProcess()
          .startEvent()
            .scriptTask("scriptTaskInSubprocess")
              .scriptFormat("groovy")
              .scriptText("throw new org.camunda.bpm.engine.delegate.BpmnError(\"anErrorCode\");")
            .userTask()
          .endEvent()
        .subProcessDone()
      .endEvent()
      .moveToActivity("subprocess")
        .boundaryEvent("boundary").error("anErrorCode")
          .userTask("userTaskAfterBoundaryEvent")
        .endEvent().done());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    runtimeService.createModification(processInstance.getProcessDefinitionId())
      .startAfterActivity("scriptTaskInSubprocess")
      .processInstanceIds(processInstance.getId())
      .execute();

    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId())
      .getActivityInstances("subprocess")[0];

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
      .activityId("subprocess")
      .unfinished()
      .singleResult();

    // assume
    assertNotNull(activityInstance);
    assertNotNull(historicActivityInstance);

    // then
    assertEquals(historicActivityInstance.getParentActivityInstanceId(), activityInstance.getParentActivityInstanceId());
  }

  @Test
  public void shouldCompleteParentProcess() {
    final BpmnModelInstance parentProcessInstance =
      Bpmn.createExecutableProcess("parentProcess")
        .startEvent()
          .callActivity("callActivity").calledElement("subprocess")
        .endEvent()
        .done();

    final BpmnModelInstance subprocessInstance =
      Bpmn.createExecutableProcess("subprocess")
        .startEvent()
          .userTask("userTask")
        .endEvent("subEnd")
        .done();

    testHelper.deploy(parentProcessInstance, subprocessInstance);

    // given I start the process, which wait as user task in subprocess
    runtimeService.startProcessInstanceByKey("parentProcess");

    assertNotNull(taskService.createTaskQuery().taskName("userTask").singleResult());

    final ProcessInstance subprocess = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").singleResult();
    assertNotNull(subprocess);

    // when I do process instance modification
    runtimeService.createProcessInstanceModification(
      subprocess.getProcessInstanceId())
      .cancelAllForActivity("userTask")
      .startAfterActivity("userTask")
      .execute();

    // then the process should be finished
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(0L));
  }

  @Test
  public void shouldContinueParentProcess() {
    final BpmnModelInstance parentProcessInstance =
      Bpmn.createExecutableProcess("parentProcess")
        .startEvent()
        .callActivity("callActivity").calledElement("subprocess")
        .userTask()
        .endEvent()
        .done();

    final BpmnModelInstance subprocessInstance =
      Bpmn.createExecutableProcess("subprocess")
        .startEvent()
          .userTask("userTask")
        .endEvent("subEnd")
        .done();

    testHelper.deploy(parentProcessInstance, subprocessInstance);

    // given I start the process, which wait as user task in subprocess
    ProcessInstance parentPI = runtimeService.startProcessInstanceByKey("parentProcess");

    assertNotNull(taskService.createTaskQuery().taskName("userTask").singleResult());

    final ProcessInstance subprocess = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").singleResult();
    assertNotNull(subprocess);

    // when I do process instance modification
    runtimeService.createProcessInstanceModification(
      subprocess.getProcessInstanceId())
      .cancelAllForActivity("userTask")
      .startAfterActivity("userTask")
      .execute();

    // then the parent process instance is still active
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(1L));

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getProcessInstanceId(), is(parentPI.getId()));
  }

  @Test
  public void shouldCompleteParentProcessWithParallelGateway() {

    final BpmnModelInstance modelInstance =
      Bpmn.createExecutableProcess("parentProcess").startEvent()
        .parallelGateway()
          .serviceTask("doNothingServiceTask").camundaExpression("${true}")
        .moveToLastGateway()
          .callActivity("callActivity").calledElement("subprocess")
        .parallelGateway("mergingParallelGateway")
      .endEvent()
      .done();

    final BpmnModelInstance parentProcessInstance =
      modify(modelInstance)
        .flowNodeBuilder("doNothingServiceTask").connectTo("mergingParallelGateway").done();

    final BpmnModelInstance subprocessInstance =
        Bpmn.createExecutableProcess("subprocess")
          .startEvent()
            .userTask("userTask")
          .endEvent("subEnd")
          .done();

    testHelper.deploy(parentProcessInstance, subprocessInstance);

    // given I start the process, which waits at user task in subprocess
    runtimeService.startProcessInstanceByKey("parentProcess");

    final ProcessInstance subprocess = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").singleResult();
    assertNotNull(subprocess);

    assertNotNull(taskService.createTaskQuery().taskName("userTask").singleResult());

    // when I do process instance modification
    runtimeService.createProcessInstanceModification(
      subprocess.getProcessInstanceId())
      .cancelAllForActivity("userTask")
      .startAfterActivity("userTask")
      .execute();

    // then the process should be finished
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(0L));

  }

  @Test
  public void shouldContinueParentProcessWithParallelGateway() {

    final BpmnModelInstance modelInstance =
      Bpmn.createExecutableProcess("parentProcess").startEvent()
        .parallelGateway()
          .serviceTask("doNothingServiceTask").camundaExpression("${true}")
        .moveToLastGateway()
          .callActivity("callActivity").calledElement("subprocess")
        .parallelGateway("mergingParallelGateway")
        .userTask()
      .endEvent()
      .done();

    final BpmnModelInstance parentProcessInstance =
      modify(modelInstance)
        .flowNodeBuilder("doNothingServiceTask").connectTo("mergingParallelGateway").done();

    final BpmnModelInstance subprocessInstance =
        Bpmn.createExecutableProcess("subprocess")
          .startEvent()
            .userTask("userTask")
          .endEvent("subEnd")
          .done();

    testHelper.deploy(parentProcessInstance, subprocessInstance);

    // given I start the process, which waits at user task in subprocess
    ProcessInstance parentPI = runtimeService.startProcessInstanceByKey("parentProcess");

    final ProcessInstance subprocess = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").singleResult();
    assertNotNull(subprocess);

    assertNotNull(taskService.createTaskQuery().taskName("userTask").singleResult());

    // when I do process instance modification
    runtimeService.createProcessInstanceModification(
      subprocess.getProcessInstanceId())
      .cancelAllForActivity("userTask")
      .startAfterActivity("userTask")
      .execute();

    // then the parent process instance is still active
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(1L));

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getProcessInstanceId(), is(parentPI.getId()));

  }

  @Test
  public void shouldCompleteParentProcessWithMultiInstance() {

    final BpmnModelInstance parentProcessInstance =
      Bpmn.createExecutableProcess("parentProcess")
        .startEvent()
          .callActivity("callActivity").calledElement("subprocess")
            .multiInstance().cardinality("3").multiInstanceDone()
        .endEvent()
        .done();

    final BpmnModelInstance subprocessInstance =
      Bpmn.createExecutableProcess("subprocess")
        .startEvent()
          .userTask("userTask")
        .endEvent("subEnd")
        .done();

    testHelper.deploy(parentProcessInstance, subprocessInstance);
    final String subprocessPrDefId = repositoryService.createProcessDefinitionQuery().processDefinitionKey("subprocess").singleResult().getId();

    // given I start the process, which waits at user task inside multiinstance subprocess
    runtimeService.startProcessInstanceByKey("parentProcess");

    final List<ProcessInstance> subprocesses = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").list();
    assertEquals(3, subprocesses.size());

    // when I do process instance modification
    runtimeService.createModification(subprocessPrDefId)
      .cancelAllForActivity("userTask")
      .startAfterActivity("userTask")
      .processInstanceIds(collectIds(subprocesses))
      .execute();

    // then the process should be finished
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(0L));

  }

  @Test
  public void shouldContinueParentProcessWithMultiInstance() {

    final BpmnModelInstance parentProcessInstance =
      Bpmn.createExecutableProcess("parentProcess")
        .startEvent()
          .callActivity("callActivity").calledElement("subprocess")
            .multiInstance().cardinality("3").multiInstanceDone()
        .userTask()
        .endEvent()
        .done();

    final BpmnModelInstance subprocessInstance =
      Bpmn.createExecutableProcess("subprocess")
        .startEvent()
          .userTask("userTask")
        .endEvent("subEnd")
        .done();

    testHelper.deploy(parentProcessInstance, subprocessInstance);
    final String subprocessPrDefId = repositoryService.createProcessDefinitionQuery().processDefinitionKey("subprocess").singleResult().getId();

    // given I start the process, which waits at user task inside multiinstance subprocess
    ProcessInstance parentPI = runtimeService.startProcessInstanceByKey("parentProcess");

    final List<ProcessInstance> subprocesses = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").list();
    assertEquals(3, subprocesses.size());

    // when I do process instance modification
    runtimeService.createModification(subprocessPrDefId)
      .cancelAllForActivity("userTask")
      .startAfterActivity("userTask")
      .processInstanceIds(collectIds(subprocesses))
      .execute();

    // then the parent process instance is still active
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(1L));

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getProcessInstanceId(), is(parentPI.getId()));

  }

  @Test
  public void shouldCompleteParentProcessWithMultiInstanceInsideEmbeddedSubProcess() {

    final BpmnModelInstance parentProcessInstance =
        Bpmn.createExecutableProcess("parentProcess")
          .startEvent()
          .subProcess()
            .embeddedSubProcess()
              .startEvent()
              .callActivity("callActivity")
                .calledElement("subprocess")
                .multiInstance()
                .cardinality("3")
                .multiInstanceDone()
              .endEvent()
          .subProcessDone()
          .endEvent()
          .done();

    final BpmnModelInstance subprocessInstance =
      Bpmn.createExecutableProcess("subprocess")
        .startEvent()
          .userTask("userTask")
        .endEvent("subEnd")
        .done();

    testHelper.deploy(parentProcessInstance, subprocessInstance);
    final String subprocessPrDefId = repositoryService.createProcessDefinitionQuery().processDefinitionKey("subprocess").singleResult().getId();

    // given I start the process, which waits at user task inside multiinstance subprocess
    runtimeService.startProcessInstanceByKey("parentProcess");

    final List<ProcessInstance> subprocesses = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").list();
    assertEquals(3, subprocesses.size());

    // when I do process instance modification
    runtimeService.createModification(subprocessPrDefId)
      .cancelAllForActivity("userTask")
      .startAfterActivity("userTask")
      .processInstanceIds(collectIds(subprocesses))
      .execute();

    // then the process should be finished
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(0L));
  }

  @Test
  public void shouldContinueParentProcessWithMultiInstanceInsideEmbeddedSubProcess() {

    final BpmnModelInstance parentProcessInstance =
        Bpmn.createExecutableProcess("parentProcess")
          .startEvent()
          .subProcess()
            .embeddedSubProcess()
              .startEvent()
              .callActivity("callActivity")
                .calledElement("subprocess")
                .multiInstance()
                .cardinality("3")
                .multiInstanceDone()
              .endEvent()
          .subProcessDone()
          .userTask()
          .endEvent()
          .done();

    final BpmnModelInstance subprocessInstance =
      Bpmn.createExecutableProcess("subprocess")
        .startEvent()
          .userTask("userTask")
        .endEvent("subEnd")
        .done();

    testHelper.deploy(parentProcessInstance, subprocessInstance);
    final String subprocessPrDefId = repositoryService.createProcessDefinitionQuery().processDefinitionKey("subprocess").singleResult().getId();

    // given I start the process, which waits at user task inside multiinstance subprocess
    ProcessInstance parentPI = runtimeService.startProcessInstanceByKey("parentProcess");

    final List<ProcessInstance> subprocesses = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").list();
    assertEquals(3, subprocesses.size());

    // when I do process instance modification
    runtimeService.createModification(subprocessPrDefId)
      .cancelAllForActivity("userTask")
      .startAfterActivity("userTask")
      .processInstanceIds(collectIds(subprocesses))
      .execute();

    // then the parent process instance is still active
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(1L));

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getProcessInstanceId(), is(parentPI.getId()));
  }

  @Test
  public void shouldCompleteParentProcessWithMultiInstanceEmbeddedSubProcess() {

    final BpmnModelInstance parentProcessInstance =
        Bpmn.createExecutableProcess("parentProcess")
          .startEvent()
          .subProcess()
            .embeddedSubProcess()
              .startEvent()
              .callActivity("callActivity")
                .calledElement("subprocess")
              .endEvent()
          .subProcessDone()
          .multiInstance()
          .cardinality("3")
          .multiInstanceDone()
          .endEvent()
          .done();

    final BpmnModelInstance subprocessInstance =
      Bpmn.createExecutableProcess("subprocess")
        .startEvent()
          .userTask("userTask")
        .endEvent("subEnd")
        .done();

    testHelper.deploy(parentProcessInstance, subprocessInstance);
    final String subprocessPrDefId = repositoryService.createProcessDefinitionQuery().processDefinitionKey("subprocess").singleResult().getId();

    // given I start the process, which waits at user task inside multiinstance subprocess
    runtimeService.startProcessInstanceByKey("parentProcess");

    final List<ProcessInstance> subprocesses = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").list();
    assertEquals(3, subprocesses.size());

    // when I do process instance modification
    runtimeService.createModification(subprocessPrDefId)
      .cancelAllForActivity("userTask")
      .startAfterActivity("userTask")
      .processInstanceIds(collectIds(subprocesses))
      .execute();

    // then the process should be finished
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(0L));
  }

  @Test
  public void shouldContinueParentProcessWithMultiInstanceEmbeddedSubProcess() {

    final BpmnModelInstance parentProcessInstance =
        Bpmn.createExecutableProcess("parentProcess")
          .startEvent()
          .subProcess()
            .embeddedSubProcess()
              .startEvent()
              .callActivity("callActivity")
                .calledElement("subprocess")
              .endEvent()
          .subProcessDone()
            .multiInstance()
            .cardinality("3")
            .multiInstanceDone()
          .userTask()
          .endEvent()
          .done();

    final BpmnModelInstance subprocessInstance =
      Bpmn.createExecutableProcess("subprocess")
        .startEvent()
          .userTask("userTask")
        .endEvent("subEnd")
        .done();

    testHelper.deploy(parentProcessInstance, subprocessInstance);
    final String subprocessPrDefId = repositoryService.createProcessDefinitionQuery().processDefinitionKey("subprocess").singleResult().getId();

    // given I start the process, which waits at user task inside multiinstance subprocess
    ProcessInstance parentPI = runtimeService.startProcessInstanceByKey("parentProcess");

    final List<ProcessInstance> subprocesses = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").list();
    assertEquals(3, subprocesses.size());

    // when I do process instance modification
    runtimeService.createModification(subprocessPrDefId)
      .cancelAllForActivity("userTask")
      .startAfterActivity("userTask")
      .processInstanceIds(collectIds(subprocesses))
      .execute();

    // then the parent process instance is still active
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(1L));

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getProcessInstanceId(), is(parentPI.getId()));
  }

  @Test
  public void shouldCancelParentProcessWithMultiInstanceCallActivity() {
    BpmnModelInstance parentProcess = Bpmn.createExecutableProcess("parentProcess")
      .startEvent()
      .callActivity("callActivity")
        .calledElement("subprocess")
        .multiInstance()
        .cardinality("3")
        .multiInstanceDone()
      .endEvent()
      .userTask()
      .endEvent()
      .done();

    BpmnModelInstance subProcess = Bpmn.createExecutableProcess("subprocess")
      .startEvent()
        .userTask("userTask")
      .endEvent("subEnd")
      .done();

    testHelper.deploy(parentProcess, subProcess);
    ProcessDefinition subProcessDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("subprocess")
        .singleResult();

    // given
    runtimeService.startProcessInstanceByKey("parentProcess");

    // assume
    List<ProcessInstance> subProcessInstances = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey("subprocess")
        .list();
    assertEquals(3, subProcessInstances.size());

    // when
    runtimeService.createModification(subProcessDefinition.getId())
      .startAfterActivity("userTask")
      .cancelAllForActivity("userTask")
      .processInstanceIds(collectIds(subProcessInstances))
      .execute();

    // then
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(0L));
  }

  @Test
  public void shouldCancelParentProcessWithCallActivityInMultiInstanceEmbeddedSubprocess() {
    BpmnModelInstance parentProcess = Bpmn.createExecutableProcess("parentProcess")
      .startEvent()
      .subProcess()
        .embeddedSubProcess()
        .startEvent()
        .callActivity("callActivity")
          .calledElement("subprocess")
        .endEvent()
      .subProcessDone()
        .multiInstance()
        .cardinality("3")
        .multiInstanceDone()
      .endEvent()
      .userTask()
      .endEvent()
      .done();

    BpmnModelInstance subProcess = Bpmn.createExecutableProcess("subprocess")
      .startEvent()
        .userTask("userTask")
      .endEvent("subEnd")
      .done();

    testHelper.deploy(parentProcess, subProcess);
    ProcessDefinition subProcessDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("subprocess")
        .singleResult();

    // given
    runtimeService.startProcessInstanceByKey("parentProcess");

    // assume
    List<ProcessInstance> subProcessInstances = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey("subprocess")
        .list();
    assertEquals(3, subProcessInstances.size());

    // when
    runtimeService.createModification(subProcessDefinition.getId())
      .startAfterActivity("userTask")
      .cancelAllForActivity("userTask")
      .processInstanceIds(collectIds(subProcessInstances))
      .execute();

    // then
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(0L));
  }

  @Test
  public void shouldCancelConcurrentExecutionInCallingProcess()
  {
    // given
    final BpmnModelInstance parentProcessInstance =
        Bpmn.createExecutableProcess("parentProcess")
          .startEvent()
          .parallelGateway("split")
            .callActivity("callActivity").calledElement("subprocess")
            .endEvent()
          .moveToLastGateway()
            .userTask("parentUserTask")
            .endEvent()
          .done();

      final BpmnModelInstance subprocessInstance =
        Bpmn.createExecutableProcess("subprocess")
          .startEvent()
            .userTask("childUserTask")
          .endEvent("subEnd")
          .done();

      testHelper.deploy(parentProcessInstance, subprocessInstance);

      ProcessInstance callingInstance = runtimeService.startProcessInstanceByKey("parentProcess");
      ProcessInstance calledInstance = runtimeService.createProcessInstanceQuery()
          .superProcessInstanceId(callingInstance.getId()).singleResult();

      // when
      runtimeService
        .createProcessInstanceModification(calledInstance.getId())
        .cancelAllForActivity("childUserTask")
        .execute();

      // then
      ProcessInstance calledInstanceAfterModification = runtimeService
          .createProcessInstanceQuery()
          .processInstanceId(calledInstance.getId())
          .singleResult();

      Assert.assertNull(calledInstanceAfterModification);

      ExecutionTree executionTree = ExecutionTree.forExecution(callingInstance.getId(), rule.getProcessEngine());
      assertThat(executionTree)
        .matches(
          describeExecutionTree("parentUserTask").scope()
        .done());
  }

  @Test
  public void shouldContinueParentWithEscalationEndEvent() {
    BpmnModelInstance parentProcess = Bpmn.createExecutableProcess("parentProcess")
                                          .startEvent()
                                          .callActivity("callActivity")
                                            .calledElement("subprocess")
                                          .boundaryEvent("escalationEvent")
                                            .escalation("escalation")
                                            .userTask("escalationTask")
                                            .endEvent()
                                          .moveToActivity("callActivity")
                                            .userTask("normalTask")
                                            .endEvent()
                                          .done();

    BpmnModelInstance subProcess = Bpmn.createExecutableProcess("subprocess")
                                       .startEvent()
                                       .userTask("userTask")
                                       .endEvent("subEnd")
                                       .escalation("escalation")
                                       .done();

    testHelper.deploy(parentProcess, subProcess);

    // given I start the process, which wait as user task in subprocess
    ProcessInstance parentPI = runtimeService.startProcessInstanceByKey("parentProcess");

    assertNotNull(taskService.createTaskQuery().taskName("userTask").singleResult());

    final ProcessInstance subprocess = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").singleResult();
    assertNotNull(subprocess);

    // when I do process instance modification
    runtimeService.createProcessInstanceModification(
            subprocess.getProcessInstanceId())
                  .cancelAllForActivity("userTask")
                  .startAfterActivity("userTask")
                  .execute();

    // then the parent process instance is still active
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(1L));
    assertNotNull(taskService.createTaskQuery().taskName("escalationTask").singleResult());

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getProcessInstanceId(), is(parentPI.getId()));
  }

  @Test
  public void shouldContinueParentWithErrorEndEvent() {
    BpmnModelInstance parentProcess = Bpmn.createExecutableProcess("parentProcess")
                                          .startEvent()
                                          .callActivity("callActivity")
                                            .calledElement("subprocess")
                                          .boundaryEvent("errorEvent")
                                            .error("error")
                                            .userTask("errorTask")
                                            .endEvent()
                                          .moveToActivity("callActivity")
                                            .userTask("normalTask")
                                            .endEvent()
                                          .done();

    BpmnModelInstance subProcess = Bpmn.createExecutableProcess("subprocess")
                                       .startEvent()
                                       .userTask("userTask")
                                       .endEvent("subEnd")
                                       .error("error")
                                       .done();

    testHelper.deploy(parentProcess, subProcess);

    // given I start the process, which wait as user task in subprocess
    ProcessInstance parentPI = runtimeService.startProcessInstanceByKey("parentProcess");

    assertNotNull(taskService.createTaskQuery().taskName("userTask").singleResult());

    final ProcessInstance subprocess = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").singleResult();
    assertNotNull(subprocess);

    // when I do process instance modification
    runtimeService.createProcessInstanceModification(
            subprocess.getProcessInstanceId())
                  .cancelAllForActivity("userTask")
                  .startAfterActivity("userTask")
                  .execute();

    // then the parent process instance is still active
    assertThat(runtimeService.createProcessInstanceQuery().count(), is(1L));
    assertNotNull(taskService.createTaskQuery().taskName("errorTask").singleResult());

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getProcessInstanceId(), is(parentPI.getId()));
  }


  private List<String> collectIds(List<ProcessInstance> processInstances) {
    List<String> supbrocessIds = new ArrayList<String>();
    for (ProcessInstance processInstance: processInstances) {
      supbrocessIds.add(processInstance.getId());
    }
    return supbrocessIds;
  }

}
