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
package org.camunda.bpm.engine.test.bpmn.event.signal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

public class SignalEventReceivedBuilderTest extends PluggableProcessEngineTest {

  protected BpmnModelInstance signalStartProcess(String processId) {
    return Bpmn.createExecutableProcess(processId)
      .startEvent()
        .signal("signal")
      .userTask()
      .endEvent()
      .done();
  }

  protected BpmnModelInstance signalCatchProcess(String processId) {
    return Bpmn.createExecutableProcess(processId)
      .startEvent()
      .intermediateCatchEvent()
        .signal("signal")
      .userTask()
      .endEvent()
      .done();
  }

  @Test
  public void testSendSignalToStartEvent() {
    testRule.deploy(signalStartProcess("signalStart"));

    runtimeService.createSignalEvent("signal").send();

    assertThat(taskService.createTaskQuery().count()).isEqualTo(1L);
  }

  @Test
  public void testSendSignalToIntermediateCatchEvent() {
    testRule.deploy(signalCatchProcess("signalCatch"));

    runtimeService.startProcessInstanceByKey("signalCatch");

    runtimeService.createSignalEvent("signal").send();

    assertThat(taskService.createTaskQuery().count()).isEqualTo(1L);
  }

  @Test
  public void testSendSignalToStartAndIntermediateCatchEvent() {
    testRule.deploy(signalStartProcess("signalStart"), signalCatchProcess("signalCatch"));

    runtimeService.startProcessInstanceByKey("signalCatch");

    runtimeService.createSignalEvent("signal").send();

    assertThat(taskService.createTaskQuery().count()).isEqualTo(2L);
  }

  @Test
  public void testSendSignalToMultipleStartEvents() {
    testRule.deploy(signalStartProcess("signalStart"), signalStartProcess("signalStart2"));

    runtimeService.createSignalEvent("signal").send();

    assertThat(taskService.createTaskQuery().count()).isEqualTo(2L);
  }

  @Test
  public void testSendSignalToMultipleIntermediateCatchEvents() {
    testRule.deploy(signalCatchProcess("signalCatch"), signalCatchProcess("signalCatch2"));

    runtimeService.startProcessInstanceByKey("signalCatch");
    runtimeService.startProcessInstanceByKey("signalCatch2");

    runtimeService.createSignalEvent("signal").send();

    assertThat(taskService.createTaskQuery().count()).isEqualTo(2L);
  }

  @Test
  public void testSendSignalWithExecutionId() {
    testRule.deploy(signalCatchProcess("signalCatch"), signalCatchProcess("signalCatch2"));

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalCatch");
    runtimeService.startProcessInstanceByKey("signalCatch2");

    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().processInstanceId(processInstance.getId()).singleResult();
    String executionId = eventSubscription.getExecutionId();

    runtimeService.createSignalEvent("signal").executionId(executionId).send();

    assertThat(taskService.createTaskQuery().count()).isEqualTo(1L);
  }

  @Test
  public void testSendSignalToStartEventWithVariables() {
    testRule.deploy(signalStartProcess("signalStart"));

    Map<String, Object> variables = Variables.createVariables()
        .putValue("var1", "a")
        .putValue("var2", "b");

    runtimeService.createSignalEvent("signal").setVariables(variables).send();

    Execution execution = runtimeService.createExecutionQuery().singleResult();
    assertThat(runtimeService.getVariables(execution.getId())).isEqualTo(variables);
  }

  @Test
  public void testSendSignalToIntermediateCatchEventWithVariables() {
    testRule.deploy(signalCatchProcess("signalCatch"));

    runtimeService.startProcessInstanceByKey("signalCatch");

    Map<String, Object> variables = Variables.createVariables()
        .putValue("var1", "a")
        .putValue("var2", "b");

    runtimeService.createSignalEvent("signal").setVariables(variables).send();

    Execution execution = runtimeService.createExecutionQuery().singleResult();
    assertThat(runtimeService.getVariables(execution.getId())).isEqualTo(variables);
  }

  @Test
  public void testNoSignalEventSubscription() {
    // assert that no exception is thrown
    runtimeService.createSignalEvent("signal").send();
  }

  @Test
  public void testNonExistingExecutionId() {

    try {
      runtimeService.createSignalEvent("signal").executionId("nonExisting").send();

    } catch (NullValueException e) {
      assertThat(e.getMessage()).contains("Cannot find execution with id 'nonExisting'");
    }
  }

  @Test
  public void testNoSignalEventSubscriptionWithExecutionId() {
    testRule.deploy(Bpmn.createExecutableProcess("noSignal")
        .startEvent()
        .userTask()
        .endEvent()
        .done());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("noSignal");
    String executionId = processInstance.getId();

    try {
      runtimeService.createSignalEvent("signal").executionId(executionId).send();

    } catch (NotFoundException e) {
      assertThat(e.getMessage()).contains("Execution '" + executionId + "' has not subscribed to a signal event with name 'signal'");
    }
  }

}
