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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyMessageCorrelationTest extends PluggableProcessEngineTestCase {

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

  public void testCorrelateMessageToStartEventNoTenantIdSetForNonTenant() {
    deployment(MESSAGE_START_PROCESS);

    runtimeService.createMessageCorrelation("message").correlate();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.singleResult().getTenantId(), is(nullValue()));
  }

  public void testCorrelateMessageToStartEventNoTenantIdSetForTenant() {
    deploymentForTenant(TENANT_ONE, MESSAGE_START_PROCESS);

    runtimeService.createMessageCorrelation("message").correlate();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testCorrelateMessageToStartEventWithoutTenantId() {
    deployment(MESSAGE_START_PROCESS);
    deploymentForTenant(TENANT_ONE, MESSAGE_START_PROCESS);

    runtimeService.createMessageCorrelation("message")
      .withoutTenantId()
      .correlate();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.singleResult().getTenantId(), is(nullValue()));
  }

  public void testCorrelateMessageToStartEventWithTenantId() {
    deploymentForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    deploymentForTenant(TENANT_TWO, MESSAGE_START_PROCESS);

    runtimeService.createMessageCorrelation("message")
      .tenantId(TENANT_ONE)
      .correlate();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  public void testCorrelateMessageToIntermediateCatchEventNoTenantIdSetForNonTenant() {
    deployment(MESSAGE_CATCH_PROCESS);

    runtimeService.startProcessInstanceByKey("messageCatch");

    runtimeService.createMessageCorrelation("message").correlate();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count(), is(1L));
  }

  public void testCorrelateMessageToIntermediateCatchEventNoTenantIdSetForTenant() {
    deploymentForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);

    runtimeService.startProcessInstanceByKey("messageCatch");

    runtimeService.createMessageCorrelation("message").correlate();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testCorrelateMessageToIntermediateCatchEventWithoutTenantId() {
    deployment(MESSAGE_CATCH_PROCESS);
    deploymentForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();

    runtimeService.createMessageCorrelation("message")
      .withoutTenantId()
      .correlate();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.singleResult().getTenantId(), is(nullValue()));
  }

  public void testCorrelateMessageToIntermediateCatchEventWithTenantId() {
    deploymentForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);
    deploymentForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    runtimeService.createMessageCorrelation("message")
      .tenantId(TENANT_ONE)
      .correlate();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  public void testCorrelateMessageToStartAndIntermediateCatchEventWithoutTenantId() {
    deployment(MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);
    deploymentForTenant(TENANT_ONE, MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionWithoutTenantId().execute();

    runtimeService.createMessageCorrelation("message")
      .withoutTenantId()
      .correlateAll();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks.size(), is(2));
    assertThat(tasks.get(0).getTenantId(), is(nullValue()));
    assertThat(tasks.get(1).getTenantId(), is(nullValue()));
  }

  public void testCorrelateMessageToStartAndIntermediateCatchEventWithTenantId() {
    deploymentForTenant(TENANT_ONE, MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);
    deploymentForTenant(TENANT_TWO, MESSAGE_START_PROCESS, MESSAGE_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    runtimeService.createMessageCorrelation("message")
      .tenantId(TENANT_ONE)
      .correlateAll();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  public void testCorrelateMessageToMultipleIntermediateCatchEventsWithoutTenantId() {
    deployment(MESSAGE_CATCH_PROCESS);
    deploymentForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionWithoutTenantId().execute();

    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();

    runtimeService.createMessageCorrelation("message")
      .withoutTenantId()
      .correlateAll();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks.size(), is(2));
    assertThat(tasks.get(0).getTenantId(), is(nullValue()));
    assertThat(tasks.get(1).getTenantId(), is(nullValue()));
  }

  public void testCorrelateMessageToMultipleIntermediateCatchEventsWithTenantId() {
    deploymentForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);
    deploymentForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();

    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();
    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    runtimeService.createMessageCorrelation("message")
      .tenantId(TENANT_ONE)
      .correlateAll();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  public void testCorrelateStartMessageWithoutTenantId() {
    deployment(MESSAGE_START_PROCESS);
    deploymentForTenant(TENANT_ONE, MESSAGE_START_PROCESS);

    runtimeService.createMessageCorrelation("message")
      .withoutTenantId()
      .correlateStartMessage();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.singleResult().getTenantId(), is(nullValue()));
  }

  public void testCorrelateStartMessageWithTenantId() {
    deploymentForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    deploymentForTenant(TENANT_TWO, MESSAGE_START_PROCESS);

    runtimeService.createMessageCorrelation("message")
      .tenantId(TENANT_ONE)
      .correlateStartMessage();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  public void testCorrelateMessagesToStartEventsForMultipleTenants() {
    deploymentForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    deploymentForTenant(TENANT_TWO, MESSAGE_START_PROCESS);

    runtimeService.createMessageCorrelation("message").correlateAll();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testCorrelateMessagesToIntermediateCatchEventsForMultipleTenants() {
    deploymentForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);
    deploymentForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    runtimeService.createMessageCorrelation("message").correlateAll();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testCorrelateMessagesToStartAndIntermediateCatchEventForMultipleTenants() {
    deploymentForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    deploymentForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    runtimeService.createMessageCorrelation("message").correlateAll();

    assertThat(runtimeService.createProcessInstanceQuery().tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(taskService.createTaskQuery().tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testFailToCorrelateMessageToIntermediateCatchEventsForMultipleTenants() {
    deploymentForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);
    deploymentForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    try {
      runtimeService.createMessageCorrelation("message").correlate();

      fail("expected exception");
    } catch (MismatchingMessageCorrelationException e) {
      assertThat(e.getMessage(), containsString("Cannot correlate a message with name 'message' to a single execution"));
    }
  }

  public void testFailToCorrelateMessageToStartEventsForMultipleTenants() {
    deploymentForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    deploymentForTenant(TENANT_TWO, MESSAGE_START_PROCESS);

    try {
      runtimeService.createMessageCorrelation("message").correlate();

      fail("expected exception");
    } catch (MismatchingMessageCorrelationException e) {
      assertThat(e.getMessage(), containsString("Cannot correlate a message with name 'message' to a single process definition"));
    }
  }

  public void testFailToCorrelateStartMessageForMultipleTenants() {
    deploymentForTenant(TENANT_ONE, MESSAGE_START_PROCESS);
    deploymentForTenant(TENANT_TWO, MESSAGE_START_PROCESS);

    try {
      runtimeService.createMessageCorrelation("message").correlateStartMessage();

      fail("expected exception");
    } catch (MismatchingMessageCorrelationException e) {
      assertThat(e.getMessage(), containsString("Cannot correlate a message with name 'message' to a single process definition"));
    }
  }

  public void testFailToCorrelateMessageByProcessInstanceIdWithoutTenantId() {
    try {
      runtimeService.createMessageCorrelation("message")
      .processInstanceId("id")
      .withoutTenantId()
      .correlate();

      fail("expected exception");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot specify a tenant-id"));
    }
  }

  public void testFailToCorrelateMessageByProcessInstanceIdAndTenantId() {
    try {
      runtimeService.createMessageCorrelation("message")
      .processInstanceId("id")
      .tenantId(TENANT_ONE)
      .correlate();

      fail("expected exception");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot specify a tenant-id"));
    }
  }

  public void testFailToCorrelateMessageByProcessDefinitionIdWithoutTenantId() {
    try {
      runtimeService.createMessageCorrelation("message")
      .processDefinitionId("id")
      .withoutTenantId()
      .correlateStartMessage();

      fail("expected exception");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot specify a tenant-id"));
    }
  }

  public void testFailToCorrelateMessageByProcessDefinitionIdAndTenantId() {
    try {
      runtimeService.createMessageCorrelation("message")
      .processDefinitionId("id")
      .tenantId(TENANT_ONE)
      .correlateStartMessage();

      fail("expected exception");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot specify a tenant-id"));
    }
  }

}
