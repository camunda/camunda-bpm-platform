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

package org.camunda.bpm.engine.test.bpmn.event.signal;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class SignalEventReceivedBuilderTest extends PluggableProcessEngineTestCase {

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

  public void testSendSignalToStartEvent() {
    deployment(signalStartProcess("signalStart"));

    runtimeService.createSignalEvent("signal").send();

    assertThat(taskService.createTaskQuery().count(), is(1L));
  }

  public void testSendSignalToIntermediateCatchEvent() {
    deployment(signalCatchProcess("signalCatch"));

    runtimeService.startProcessInstanceByKey("signalCatch");

    runtimeService.createSignalEvent("signal").send();

    assertThat(taskService.createTaskQuery().count(), is(1L));
  }

  public void testSendSignalToStartAndIntermediateCatchEvent() {
    deployment(signalStartProcess("signalStart"), signalCatchProcess("signalCatch"));

    runtimeService.startProcessInstanceByKey("signalCatch");

    runtimeService.createSignalEvent("signal").send();

    assertThat(taskService.createTaskQuery().count(), is(2L));
  }

  public void testSendSignalToMultipleStartEvents() {
    deployment(signalStartProcess("signalStart"), signalStartProcess("signalStart2"));

    runtimeService.createSignalEvent("signal").send();

    assertThat(taskService.createTaskQuery().count(), is(2L));
  }

  public void testSendSignalToMultipleIntermediateCatchEvents() {
    deployment(signalCatchProcess("signalCatch"), signalCatchProcess("signalCatch2"));

    runtimeService.startProcessInstanceByKey("signalCatch");
    runtimeService.startProcessInstanceByKey("signalCatch2");

    runtimeService.createSignalEvent("signal").send();

    assertThat(taskService.createTaskQuery().count(), is(2L));
  }

  public void testSendSignalWithExecutionId() {
    deployment(signalCatchProcess("signalCatch"), signalCatchProcess("signalCatch2"));

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalCatch");
    runtimeService.startProcessInstanceByKey("signalCatch2");

    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().processInstanceId(processInstance.getId()).singleResult();
    String executionId = eventSubscription.getExecutionId();

    runtimeService.createSignalEvent("signal").executionId(executionId).send();

    assertThat(taskService.createTaskQuery().count(), is(1L));
  }

  public void testSendSignalToStartEventWithVariables() {
    deployment(signalStartProcess("signalStart"));

    Map<String, Object> variables = Variables.createVariables()
        .putValue("var1", "a")
        .putValue("var2", "b");

    runtimeService.createSignalEvent("signal").setVariables(variables).send();

    Execution execution = runtimeService.createExecutionQuery().singleResult();
    assertThat(runtimeService.getVariables(execution.getId()), is(variables));
  }

  public void testSendSignalToIntermediateCatchEventWithVariables() {
    deployment(signalCatchProcess("signalCatch"));

    runtimeService.startProcessInstanceByKey("signalCatch");

    Map<String, Object> variables = Variables.createVariables()
        .putValue("var1", "a")
        .putValue("var2", "b");

    runtimeService.createSignalEvent("signal").setVariables(variables).send();

    Execution execution = runtimeService.createExecutionQuery().singleResult();
    assertThat(runtimeService.getVariables(execution.getId()), is(variables));
  }

  public void testNoSignalEventSubscription() {
    // assert that no exception is thrown
    runtimeService.createSignalEvent("signal").send();
  }

  public void testNonExistingExecutionId() {

    try {
      runtimeService.createSignalEvent("signal").executionId("nonExisting").send();

    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Cannot find execution with id 'nonExisting'"));
    }
  }

  public void testNoSignalEventSubscriptionWithExecutionId() {
    deployment(Bpmn.createExecutableProcess("noSignal")
        .startEvent()
        .userTask()
        .endEvent()
        .done());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("noSignal");
    String executionId = processInstance.getId();

    try {
      runtimeService.createSignalEvent("signal").executionId(executionId).send();

    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Execution '" + executionId + "' has not subscribed to a signal event with name 'signal'"));
    }
  }

}
