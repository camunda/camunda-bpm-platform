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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;

public class MultiTenancyExecutionPropagationTest extends PluggableProcessEngineTestCase {

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

  public void testPropagateTenantIdToTask() {

    deploymentForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .userTask()
        .endEvent()
      .done());

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task, is(notNullValue()));
    // inherit the tenant id from execution
    assertThat(task.getTenantId(), is(TENANT_ID));
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

  public void testPropagateTenantIdToVariableInstanceFromTask() {

    deploymentForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .userTask()
          .camundaAsyncAfter()
        .endEvent()
      .done());

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    VariableMap variables = Variables.createVariables().putValue("var", "test");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariablesLocal(task.getId(), variables);

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variableInstance, is(notNullValue()));
    // inherit the tenant id from task
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

  public void testPropagateTenantIdToStartTimerJobDefinition() {

    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_ID)
        .addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/timerStartEvent.bpmn"));

    // the job definition is created on deployment
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    assertThat(jobDefinition, is(notNullValue()));
    // inherit the tenant id from process definition
    assertThat(jobDefinition.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToIntermediateTimerJob() {

    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_ID)
        .addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/intermediateTimerEvent.bpmn"));

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // the job is created when the timer event is reached
    Job job = managementService.createJobQuery().singleResult();
    assertThat(job, is(notNullValue()));
    // inherit the tenant id from job definition
    assertThat(job.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToAsyncJob() {

    deploymentForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .userTask()
          .camundaAsyncBefore()
        .endEvent()
      .done());

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // the job is created when the asynchronous activity is reached
    Job job = managementService.createJobQuery().singleResult();
    assertThat(job, is(notNullValue()));
    // inherit the tenant id from job definition
    assertThat(job.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToFailedJobIncident() {

    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_ID)
        .addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/failingTask.bpmn"));

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    // execute the job of the async activity
    Job job = managementService.createJobQuery().singleResult();
    try {
      managementService.executeJob(job.getId());
    } catch(ProcessEngineException e) {
      // the job failed and created an incident
    }

    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertThat(incident, is(notNullValue()));
    // inherit the tenant id from execution
    assertThat(incident.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToFailedStartTimerIncident() {

    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_ID)
        .addClasspathResource("org/camunda/bpm/engine/test/api/multitenancy/timerStartEventWithfailingTask.bpmn"));

    // execute the job of the timer start event
    Job job = managementService.createJobQuery().singleResult();
    try {
      managementService.executeJob(job.getId());
    } catch(ProcessEngineException e) {
      // the job failed and created an incident
    }

    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertThat(incident, is(notNullValue()));
    // inherit the tenant id from job
    assertThat(incident.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToFailedExternalTaskIncident() {

    deploymentForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .serviceTask()
          .camundaType("external")
          .camundaTopic("test")
        .endEvent()
      .done());

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    // fetch the external task and mark it as failed which create an incident
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(1, "test-worker").topic("test", 1000).execute();
    externalTaskService.handleFailure(tasks.get(0).getId(), "test-worker", "expected", 0, 0);

    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertThat(incident, is(notNullValue()));
    // inherit the tenant id from job
    assertThat(incident.getTenantId(), is(TENANT_ID));
  }

  public static class SetVariableTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
      execution.setVariable("var", "test");
    }
  }

}
