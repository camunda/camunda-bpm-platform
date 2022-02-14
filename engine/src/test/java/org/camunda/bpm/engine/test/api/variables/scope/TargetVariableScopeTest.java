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
package org.camunda.bpm.engine.test.api.variables.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ScriptEvaluationException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Askar Akhmerov
 * @author Tassilo Weidner
 */
public class TargetVariableScopeTest {
  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  @Rule
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/variables/scope/TargetVariableScopeTest.testExecutionWithDelegateProcess.bpmn","org/camunda/bpm/engine/test/api/variables/scope/doer.bpmn"})
  public void testExecutionWithDelegateProcess() {
    // Given we create a new process instance
    VariableMap variables = Variables.createVariables().putValue("orderIds", Arrays.asList(new int[]{1, 2, 3}));
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("Process_MultiInstanceCallAcitivity",variables);

    // it runs without any problems
    assertThat(processInstance.isEnded()).isTrue();
    assertThat(((ProcessInstanceWithVariablesImpl) processInstance).getVariables().containsKey("targetOrderId")).isFalse();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/variables/scope/TargetVariableScopeTest.testExecutionWithScriptTargetScope.bpmn","org/camunda/bpm/engine/test/api/variables/scope/doer.bpmn"})
  public void testExecutionWithScriptTargetScope () {
    VariableMap variables = Variables.createVariables().putValue("orderIds", Arrays.asList(new int[]{1, 2, 3}));
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("Process_MultiInstanceCallAcitivity",variables);

    // it runs without any problems
    assertThat(processInstance.isEnded()).isTrue();
    assertThat(((ProcessInstanceWithVariablesImpl) processInstance).getVariables().containsKey("targetOrderId")).isFalse();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/variables/scope/TargetVariableScopeTest.testExecutionWithoutProperTargetScope.bpmn","org/camunda/bpm/engine/test/api/variables/scope/doer.bpmn"})
  public void testExecutionWithoutProperTargetScope () {
    VariableMap variables = Variables.createVariables().putValue("orderIds", Arrays.asList(new int[]{1, 2, 3}));
    ProcessDefinition processDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("Process_MultiInstanceCallAcitivity").singleResult();

    // when/then
    //fails due to inappropriate variable scope target
    assertThatThrownBy(() -> engineRule.getRuntimeService().startProcessInstanceByKey("Process_MultiInstanceCallAcitivity",variables))
      .isInstanceOf(ScriptEvaluationException.class)
      .hasMessageContaining("Unable to evaluate script while executing activity 'CallActivity_1' in the process definition with id '"
          + processDefinition.getId() + "': org.camunda.bpm.engine.ProcessEngineException: ENGINE-20011 "
              + "Scope with specified activity Id NOT_EXISTING and execution");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/variables/scope/doer.bpmn"})
  public void testWithDelegateVariableMapping () {
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process1")
        .startEvent()
          .subProcess("SubProcess_1")
          .embeddedSubProcess()
            .startEvent()
              .callActivity()
                .calledElement("Process_StuffDoer")
                .camundaVariableMappingClass("org.camunda.bpm.engine.test.api.variables.scope.SetVariableMappingDelegate")
              .serviceTask()
                .camundaClass("org.camunda.bpm.engine.test.api.variables.scope.AssertVariableScopeDelegate")
            .endEvent()
          .subProcessDone()
        .endEvent()
        .done();
    instance = modify(instance)
        .activityBuilder("SubProcess_1")
        .multiInstance()
        .parallel()
        .camundaCollection("orderIds")
        .camundaElementVariable("orderId")
        .done();

    ProcessDefinition processDefinition = testHelper.deployAndGetDefinition(instance);
    VariableMap variables = Variables.createVariables().putValue("orderIds", Arrays.asList(new int[]{1, 2, 3}));
    engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId(),variables);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/variables/scope/doer.bpmn"})
  public void testWithDelegateVariableMappingAndChildScope () {
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process1")
        .startEvent()
          .parallelGateway()
            .subProcess("SubProcess_1")
            .embeddedSubProcess()
              .startEvent()
              .callActivity()
                .calledElement("Process_StuffDoer")
                .camundaVariableMappingClass("org.camunda.bpm.engine.test.api.variables.scope.SetVariableToChildMappingDelegate")
              .serviceTask()
                .camundaClass("org.camunda.bpm.engine.test.api.variables.scope.AssertVariableScopeDelegate")
              .endEvent()
            .subProcessDone()
          .moveToLastGateway()
            .subProcess("SubProcess_2")
            .embeddedSubProcess()
              .startEvent()
                .userTask("ut")
              .endEvent()
            .subProcessDone()
        .endEvent()
        .done();
    instance = modify(instance)
        .activityBuilder("SubProcess_1")
        .multiInstance()
        .parallel()
        .camundaCollection("orderIds")
        .camundaElementVariable("orderId")
        .done();

    ProcessDefinition processDefinition = testHelper.deployAndGetDefinition(instance);

    VariableMap variables = Variables.createVariables().putValue("orderIds", Arrays.asList(new int[]{1, 2, 3}));

    // when/then
    //fails due to inappropriate variable scope target
    assertThatThrownBy(() -> engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId(),variables))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("org.camunda.bpm.engine.ProcessEngineException: ENGINE-20011 Scope with specified activity Id SubProcess_2 and execution");

  }

  public static class JavaDelegate implements org.camunda.bpm.engine.delegate.JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
      execution.setVariable("varName", "varValue", "activityId");
      assertThat(execution.getVariableLocal("varName")).isNotNull();
    }

  }

  public static class ExecutionListener implements org.camunda.bpm.engine.delegate.ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) {
      execution.setVariable("varName", "varValue", "activityId");
      assertThat(execution.getVariableLocal("varName")).isNotNull();
    }

  }

  public static class TaskListener implements org.camunda.bpm.engine.delegate.TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
      DelegateExecution execution = delegateTask.getExecution();
      execution.setVariable("varName", "varValue", "activityId");
      assertThat(execution.getVariableLocal("varName")).isNotNull();
    }
  }

  @Test
  public void testSetLocalScopeWithJavaDelegate() {
    testHelper.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
      .serviceTask()
        .id("activityId")
        .camundaClass(JavaDelegate.class)
      .endEvent()
      .done());

    engineRule.getRuntimeService().startProcessInstanceByKey("process");
  }

  @Test
  public void testSetLocalScopeWithExecutionListenerStart() {
    testHelper.deploy(Bpmn.createExecutableProcess("process")
      .startEvent().id("activityId")
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, ExecutionListener.class)
      .endEvent()
      .done());

    engineRule.getRuntimeService().startProcessInstanceByKey("process");
  }

  @Test
  public void testSetLocalScopeWithExecutionListenerEnd() {
    testHelper.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
      .endEvent().id("activityId")
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, ExecutionListener.class)
      .done());

    engineRule.getRuntimeService().startProcessInstanceByKey("process");
  }

  @Test
  public void testSetLocalScopeWithExecutionListenerTake() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process")
      .startEvent().id("activityId")
      .sequenceFlowId("sequenceFlow")
      .endEvent()
      .done();

    CamundaExecutionListener listener = modelInstance.newInstance(CamundaExecutionListener.class);
    listener.setCamundaEvent(ExecutionListener.EVENTNAME_TAKE);
    listener.setCamundaClass(ExecutionListener.class.getName());
    modelInstance.<SequenceFlow>getModelElementById("sequenceFlow").builder().addExtensionElement(listener);

    testHelper.deploy(modelInstance);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
  }

  @Test
  public void testSetLocalScopeWithTaskListener() {
    testHelper.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask().id("activityId")
        .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, TaskListener.class)
      .endEvent()
      .done());

    engineRule.getRuntimeService().startProcessInstanceByKey("process");
  }

  @Test
  public void testSetLocalScopeInSubprocessWithJavaDelegate() {
    testHelper.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
      .subProcess().embeddedSubProcess()
        .startEvent()
          .serviceTask().id("activityId")
            .camundaClass(JavaDelegate.class)
        .endEvent()
      .subProcessDone()
      .endEvent()
      .done());

    engineRule.getRuntimeService().startProcessInstanceByKey("process");
  }

  @Test
  public void testSetLocalScopeInSubprocessWithStartExecutionListener() {
    testHelper.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
      .subProcess().embeddedSubProcess()
        .startEvent().id("activityId")
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, ExecutionListener.class)
        .endEvent()
      .subProcessDone()
      .endEvent()
      .done());

    engineRule.getRuntimeService().startProcessInstanceByKey("process");
  }

  @Test
  public void testSetLocalScopeInSubprocessWithEndExecutionListener() {
    testHelper.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
      .subProcess().embeddedSubProcess()
        .startEvent()
        .endEvent().id("activityId")
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, ExecutionListener.class)
      .subProcessDone()
      .endEvent()
      .done());

    engineRule.getRuntimeService().startProcessInstanceByKey("process");
  }

  @Test
  public void testSetLocalScopeInSubprocessWithTaskListener() {
    testHelper.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
      .subProcess().embeddedSubProcess()
        .startEvent()
        .userTask().id("activityId")
        .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, TaskListener.class)
        .endEvent()
      .subProcessDone()
      .endEvent()
      .done());

    engineRule.getRuntimeService().startProcessInstanceByKey("process");
  }

}
