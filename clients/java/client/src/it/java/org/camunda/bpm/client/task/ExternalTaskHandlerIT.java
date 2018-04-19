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
package org.camunda.bpm.client.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.client.rule.ClientRule.LOCK_DURATION;
import static org.camunda.bpm.client.util.ProcessModels.BPMN_ERROR_EXTERNAL_TASK_PROCESS;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_ID;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_FOO;
import static org.camunda.bpm.client.util.ProcessModels.PROCESS_KEY;
import static org.camunda.bpm.client.util.ProcessModels.USER_TASK_AFTER_BPMN_ERROR;
import static org.camunda.bpm.client.util.ProcessModels.USER_TASK_ID;

import java.util.concurrent.atomic.AtomicBoolean;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.dto.IncidentDto;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.dto.ProcessInstanceDto;
import org.camunda.bpm.client.dto.TaskDto;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class ExternalTaskHandlerIT {

  protected ClientRule clientRule = new ClientRule();
  protected EngineRule engineRule = new EngineRule();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(clientRule);

  protected ExternalTaskClient client;

  protected ProcessDefinitionDto processDefinition;
  protected ProcessInstanceDto processInstance;

  @Before
  public void setup() throws Exception {
    client = clientRule.client();
    processDefinition = engineRule.deploy(BPMN_ERROR_EXTERNAL_TASK_PROCESS).get(0);
    processInstance = engineRule.startProcessInstance(processDefinition.getId());
  }

  @Test
  public void shouldInvokeHandler() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    assertThat(handler.getHandledTasks()).hasSize(1);

    ExternalTask task = handler.getHandledTasks().get(0);

    assertThat(task.getProcessDefinitionId()).isEqualTo(processDefinition.getId());
    assertThat(task.getProcessDefinitionKey()).isEqualTo(PROCESS_KEY);

    assertThat(task.getBusinessKey()).isNull();
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(task.getActivityId()).isEqualTo(EXTERNAL_TASK_ID);
    assertThat(task.getActivityInstanceId()).isNotNull();

    assertThat(task.getTopicName()).isEqualTo(EXTERNAL_TASK_TOPIC_FOO);
    assertThat(task.getLockExpirationTime()).isNotNull();

    assertThat(task.getAllVariables()).isEmpty();
    // todo assert business key
  }

  @Test
  public void shouldCompleteTask() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.complete(task);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstance.getId());
    assertThat(task).isNotNull();
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(task.getTaskDefinitionKey()).isEqualTo(USER_TASK_ID);
  }

  @Test
  public void shouldExtendLock() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.extendLock(task, LOCK_DURATION * 10);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask externalTaskBeforeExtendLock = handler.getHandledTasks().get(0);
    ExternalTask externalTaskAfterExtendLock = engineRule.getExternalTaskByProcessInstanceId(processInstance.getId());

    assertThat(externalTaskAfterExtendLock.getLockExpirationTime()).isAfter(externalTaskBeforeExtendLock.getLockExpirationTime());
  }

  @Test
  public void shouldUnlockTask() {
    // given
    final AtomicBoolean unlocked = new AtomicBoolean(false);
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      if (!unlocked.get()) {
        client.unlock(task);
      }
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() >= 2);

    ExternalTask firstInvocation = handler.getHandledTasks().get(0);
    ExternalTask secondInvocation = handler.getHandledTasks().get(1);
    assertThat(firstInvocation.getId()).isEqualTo(secondInvocation.getId());
  }

  @Test
  public void shoulInvokeHandleBpmnError() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.handleBpmnError(task, "500");
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstance.getId());
    assertThat(task).isNotNull();
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(task.getTaskDefinitionKey()).isEqualTo(USER_TASK_AFTER_BPMN_ERROR);
  }

  @Test
  public void shouldInvokeHandleFailure() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.handleFailure(task, "my-message", "my-details", 0, 0);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    IncidentDto incident = engineRule.getIncidentByProcessInstanceId(processInstance.getId());
    assertThat(incident).isNotNull();
    assertThat(incident.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(incident.getActivityId()).isEqualTo(EXTERNAL_TASK_ID);
  }

}
