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

package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class MultiTenancyMessageCorrelationTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final BpmnModelInstance MESSAGE_START_PROCESS = Bpmn.createExecutableProcess("messageStart")
      .startEvent()
        .message("message")
      .userTask()
      .endEvent()
      .done();

  protected static final BpmnModelInstance MESSAGE_CATCH_PROCESS = Bpmn.createExecutableProcess("messageCatch")
      .startEvent()
      .intermediateCatchEvent()
        .message("message")
      .userTask()
      .endEvent()
      .done();

  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  @Test
  public void correlateMessageToStartEventNoTenantIdSetForNonTenant() {
    testRule.deploy(MESSAGE_START_PROCESS);

    engineRule.getRuntimeService().createMessageCorrelation("message").correlate();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.singleResult().getTenantId(), is(nullValue()));
  }

  @Test
  public void correlateMessageToStartEventNoTenantIdSetForTenant() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);

    engineRule.getRuntimeService().createMessageCorrelation("message").correlate();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  @Test
  public void correlateMessageToStartEventWithoutTenantId() {
    testRule.deploy(MESSAGE_START_PROCESS);
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .withoutTenantId()
      .correlate();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.singleResult().getTenantId(), is(nullValue()));
  }

  @Test
  public void correlateMessageToStartEventWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_START_PROCESS);

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .tenantId(TENANT_ONE)
      .correlate();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void correlateMessageToIntermediateCatchEventNoTenantIdSetForNonTenant() {
    testRule.deploy(MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().startProcessInstanceByKey("messageCatch");

    engineRule.getRuntimeService().createMessageCorrelation("message").correlate();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
  }

  @Test
  public void correlateMessageToIntermediateCatchEventNoTenantIdSetForTenant() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().startProcessInstanceByKey("messageCatch");

    engineRule.getRuntimeService().createMessageCorrelation("message").correlate();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  @Test
  public void correlateMessageToIntermediateCatchEventWithoutTenantId() {
    testRule.deploy(MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionWithoutTenantId().execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .withoutTenantId()
      .correlate();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.singleResult().getTenantId(), is(nullValue()));
  }

  @Test
  public void correlateMessageToIntermediateCatchEventWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .tenantId(TENANT_ONE)
      .correlate();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void correlateMessageToStartAndIntermediateCatchEventWithoutTenantId() {
    testRule.deploy(MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionWithoutTenantId().execute();

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .withoutTenantId()
      .correlateAll();

    List<Task> tasks = engineRule.getTaskService().createTaskQuery().list();
    assertThat(tasks.size(), is(2));
    assertThat(tasks.get(0).getTenantId(), is(nullValue()));
    assertThat(tasks.get(1).getTenantId(), is(nullValue()));
  }

  @Test
  public void correlateMessageToStartAndIntermediateCatchEventWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .tenantId(TENANT_ONE)
      .correlateAll();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void correlateMessageToMultipleIntermediateCatchEventsWithoutTenantId() {
    testRule.deploy(MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionWithoutTenantId().execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionWithoutTenantId().execute();

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .withoutTenantId()
      .correlateAll();

    List<Task> tasks = engineRule.getTaskService().createTaskQuery().list();
    assertThat(tasks.size(), is(2));
    assertThat(tasks.get(0).getTenantId(), is(nullValue()));
    assertThat(tasks.get(1).getTenantId(), is(nullValue()));
  }

  @Test
  public void correlateMessageToMultipleIntermediateCatchEventsWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .tenantId(TENANT_ONE)
      .correlateAll();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void correlateStartMessageWithoutTenantId() {
    testRule.deploy(MESSAGE_START_PROCESS);
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .withoutTenantId()
      .correlateStartMessage();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.singleResult().getTenantId(), is(nullValue()));
  }

  @Test
  public void correlateStartMessageWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_START_PROCESS);

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .tenantId(TENANT_ONE)
      .correlateStartMessage();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void correlateMessagesToStartEventsForMultipleTenants() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_START_PROCESS);

    engineRule.getRuntimeService().createMessageCorrelation("message").correlateAll();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  @Test
  public void correlateMessagesToIntermediateCatchEventsForMultipleTenants() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getRuntimeService().createMessageCorrelation("message").correlateAll();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  @Test
  public void correlateMessagesToStartAndIntermediateCatchEventForMultipleTenants() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getRuntimeService().createMessageCorrelation("message").correlateAll();

    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery().tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(engineRule.getTaskService().createTaskQuery().tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void failToCorrelateMessageToIntermediateCatchEventsForMultipleTenants() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    // declare expected exception
    thrown.expect(MismatchingMessageCorrelationException.class);
    thrown.expectMessage("Cannot correlate a message with name 'message' to a single execution");

    engineRule.getRuntimeService().createMessageCorrelation("message").correlate();
  }

  @Test
  public void failToCorrelateMessageToStartEventsForMultipleTenants() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_START_PROCESS);

    // declare expected exception
    thrown.expect(MismatchingMessageCorrelationException.class);
    thrown.expectMessage("Cannot correlate a message with name 'message' to a single process definition");

    engineRule.getRuntimeService().createMessageCorrelation("message").correlate();
  }

  @Test
  public void failToCorrelateStartMessageForMultipleTenants() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_START_PROCESS);

    // declare expected exception
    thrown.expect(MismatchingMessageCorrelationException.class);
    thrown.expectMessage("Cannot correlate a message with name 'message' to a single process definition");

    engineRule.getRuntimeService().createMessageCorrelation("message").correlateStartMessage();
  }

  @Test
  public void failToCorrelateMessageByProcessInstanceIdWithoutTenantId() {
    // declare expected exception
    thrown.expect(BadUserRequestException.class);
    thrown.expectMessage("Cannot specify a tenant-id");

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .processInstanceId("id")
      .withoutTenantId()
      .correlate();
  }

  @Test
  public void failToCorrelateMessageByProcessInstanceIdAndTenantId() {
    // declare expected exception
    thrown.expect(BadUserRequestException.class);
    thrown.expectMessage("Cannot specify a tenant-id");

    engineRule.getRuntimeService().createMessageCorrelation("message")
    .processInstanceId("id")
    .tenantId(TENANT_ONE)
    .correlate();
  }

  @Test
  public void failToCorrelateMessageByProcessDefinitionIdWithoutTenantId() {
    // declare expected exception
    thrown.expect(BadUserRequestException.class);
    thrown.expectMessage("Cannot specify a tenant-id");

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .processDefinitionId("id")
      .withoutTenantId()
      .correlateStartMessage();
  }

  @Test
  public void failToCorrelateMessageByProcessDefinitionIdAndTenantId() {
    // declare expected exception
    thrown.expect(BadUserRequestException.class);
    thrown.expectMessage("Cannot specify a tenant-id");

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .processDefinitionId("id")
      .tenantId(TENANT_ONE)
      .correlateStartMessage();
  }

  @Test
  public void correlateMessageToStartEventNoAuthenticatedTenants() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_START_PROCESS);
    testRule.deploy(MESSAGE_START_PROCESS);

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .correlateStartMessage();

    engineRule.getIdentityService().clearAuthentication();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
  }

  @Test
  public void correlateMessageToStartEventWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_START_PROCESS);

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .correlateStartMessage();

    engineRule.getIdentityService().clearAuthentication();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void correlateMessageToStartEventDisabledTenantCheck() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_START_PROCESS);

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .tenantId(TENANT_ONE)
      .correlateStartMessage();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void correlateMessageToIntermediateCatchEventNoAuthenticatedTenants() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);
    testRule.deploy(MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionWithoutTenantId().execute();

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .correlate();

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.withoutTenantId().count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void correlateMessageToIntermediateCatchEventWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .correlate();

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void correlateMessageToIntermediateCatchEventDisabledTenantCheck() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .tenantId(TENANT_ONE)
      .correlate();

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void correlateMessageToStartAndIntermediateCatchEventWithNoAuthenticatedTenants() {
    testRule.deploy(MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionWithoutTenantId().execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .correlateAll();

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(2L));
    assertThat(query.withoutTenantId().count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(0L));
  }

  @Test
  public void correlateMessageToStartAndIntermediateCatchEventWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_TWO, MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .correlateAll();

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void correlateMessageToStartAndIntermediateCatchEventDisabledTenantCheck() {
    testRule.deployForTenant(TENANT_TWO, MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .correlateAll();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(4L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(2L));
  }

  @Test
  public void failToCorrelateMessageByProcessInstanceIdNoAuthenticatedTenants() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);

    ProcessInstance processInstance = engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch")
        .processDefinitionTenantId(TENANT_ONE).execute();

    // declared expected exception
    thrown.expect(MismatchingMessageCorrelationException.class);
    thrown.expectMessage("Cannot correlate message");

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .processInstanceId(processInstance.getId())
      .correlate();
  }

  @Test
  public void correlateMessageByProcessInstanceIdWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);

    ProcessInstance processInstance = engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").execute();

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .processInstanceId(processInstance.getId())
      .correlate();

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  @Test
  public void failToCorrelateMessageByProcessDefinitionIdNoAuthenticatedTenants() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);

    ProcessDefinition processDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery().
        processDefinitionKey("messageStart").tenantIdIn(TENANT_ONE).singleResult();

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot create an instance of the process definition");

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .processDefinitionId(processDefinition.getId())
      .correlateStartMessage();
  }

  @Test
  public void correlateMessageByProcessDefinitionIdWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_START_PROCESS);

    ProcessDefinition processDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery().
        processDefinitionKey("messageStart").singleResult();

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRuntimeService().createMessageCorrelation("message")
      .processDefinitionId(processDefinition.getId())
      .correlateStartMessage();

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

}
