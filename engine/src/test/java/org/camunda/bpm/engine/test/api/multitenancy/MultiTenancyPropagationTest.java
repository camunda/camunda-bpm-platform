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

package org.camunda.bpm.engine.test.api.multitenancy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;

public class MultiTenancyPropagationTest extends PluggableProcessEngineTestCase {

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";
  protected static final String TENANT_ID = "tenant1";

  public void testPropagateTenantIdToProcessDefinition() {

    deploymentForTenant(TENANT_ID,  Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).done());

    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .singleResult();

    assertNotNull(processDefinition);
    // inherit the tenant id from deployment
    assertEquals(TENANT_ID, processDefinition.getTenantId());
  }

  public void testPropagateTenantIdToProcessInstance() {

    deploymentForTenant(TENANT_ID,  Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .userTask()
        .endEvent()
       .done());

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance, is(notNullValue()));
    // inherit the tenant id from process definition
    assertThat(processInstance.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToConcurrentExecution() {

    deploymentForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .parallelGateway("fork")
          .userTask()
          .parallelGateway("join")
          .endEvent()
          .moveToNode("fork")
            .userTask()
            .connectTo("join")
            .done());

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    List<Execution> executions = runtimeService.createExecutionQuery().list();
    assertThat(executions.size(), is(3));
    assertThat(executions.get(0).getTenantId(), is(TENANT_ID));
    // inherit the tenant id from process instance
    assertThat(executions.get(1).getTenantId(), is(TENANT_ID));
    assertThat(executions.get(2).getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToEmbeddedSubprocess() {

    deploymentForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .subProcess()
        .embeddedSubProcess()
          .startEvent()
          .userTask()
          .endEvent()
      .subProcessDone()
      .endEvent()
    .done());

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    List<Execution> executions = runtimeService.createExecutionQuery().list();
    assertThat(executions.size(), is(2));
    assertThat(executions.get(0).getTenantId(), is(TENANT_ID));
    // inherit the tenant id from parent execution (e.g. process instance)
    assertThat(executions.get(1).getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToVariableInstanceOnStartProcessInstance() {

    deploymentForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .userTask()
        .endEvent()
      .done());

    VariableMap variables = Variables.putValue("var", "test");
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variableInstance, is(notNullValue()));
    // inherit the tenant id from process instance
    assertThat(variableInstance.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToVariableInstanceFromExecution() {

    deploymentForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .serviceTask()
          .camundaClass(SetVariableTask.class.getName())
          .camundaAsyncAfter()
        .endEvent()
      .done());

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variableInstance, is(notNullValue()));
    // inherit the tenant id from execution
    assertThat(variableInstance.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToStartMessageEventSubscription() {

    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_ID)
        .addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/messageStartEvent.bpmn"));

    // the event subscription of the message start is created on deployment
    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertThat(eventSubscription, is(notNullValue()));
    // inherit the tenant id from process definition
    assertThat(eventSubscription.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToStartSignalEventSubscription() {

    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_ID)
        .addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/signalStartEvent.bpmn"));

    // the event subscription of the signal start event is created on deployment
    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertThat(eventSubscription, is(notNullValue()));
    // inherit the tenant id from process definition
    assertThat(eventSubscription.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToIntermediateMessageEventSubscription() {

    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_ID)
        .addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/intermediateMessageCatchEvent.bpmn"));

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertThat(eventSubscription, is(notNullValue()));
    // inherit the tenant id from process instance
    assertThat(eventSubscription.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToIntermediateSignalEventSubscription() {

    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_ID)
        .addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/intermediateSignalCatchEvent.bpmn"));

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertThat(eventSubscription, is(notNullValue()));
    // inherit the tenant id from process instance
    assertThat(eventSubscription.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToCompensationEventSubscription() {

    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_ID)
        .addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/compensationBoundaryEvent.bpmn"));

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // the event subscription is created after execute the activity with the attached compensation boundary event
    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertThat(eventSubscription, is(notNullValue()));
    // inherit the tenant id from process instance
    assertThat(eventSubscription.getTenantId(), is(TENANT_ID));
  }

  public static class SetVariableTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
      execution.setVariable("var", "test");
    }
  }

}
