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
package org.camunda.bpm.client.topic;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.client.util.ProcessModels.*;

/**
 * @author Tassilo Weidner
 */
public class TopicSubscriptionIT {

  protected static final String BUSINESS_KEY = "aBusinessKey";
  protected static final String VARIABLE_NAME = "aVariableName";
  protected static final String ANOTHER_VARIABLE_NAME = "anotherVariableName";
  protected static final String VARIABLE_VALUE = "aVariableValue";
  protected static final String ANOTHER_VARIABLE_VALUE = "anotherVariableValue";

  protected ClientRule clientRule = new ClientRule();
  protected EngineRule engineRule = new EngineRule();
  protected ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(clientRule).around(thrown);

  protected ExternalTaskClient client;

  protected ProcessDefinitionDto processDefinition;
  protected ProcessDefinitionDto processDefinition2;
  protected RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();

  @Before
  public void setup() throws Exception {
    client = clientRule.client();
    handler.clear();
    processDefinition = engineRule.deploy(BPMN_ERROR_EXTERNAL_TASK_PROCESS).get(0);
    processDefinition2 = engineRule.deploy(ONE_EXTERNAL_TASK_WITH_OUTPUT_PARAM_PROCESS).get(0);
  }

  @Test
  public void shouldSetTopicName() {
    // given
    TopicSubscriptionBuilder topicSubscriptionBuilder = client
      .subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler);

    // when
    TopicSubscription topicSubscription = topicSubscriptionBuilder.open();

    // then
    assertThat(topicSubscription.getTopicName()).isEqualTo(EXTERNAL_TASK_TOPIC_FOO);
  }

  @Test
  public void shouldSetExternalTaskHandler() {
    // given
    TopicSubscriptionBuilder topicSubscriptionBuilder = client
      .subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler);

    // when
    TopicSubscription topicSubscription = topicSubscriptionBuilder.open();

    // then
    assertThat(topicSubscription.getExternalTaskHandler()).isEqualTo(handler);
  }

  @Test
  public void shouldFilterByBusinessKey() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), BUSINESS_KEY);

    // when
    TopicSubscription topicSubscription = client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .businessKey(BUSINESS_KEY)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    assertThat(task.getBusinessKey()).isEqualTo(BUSINESS_KEY);
    assertThat(topicSubscription.getBusinessKey()).isEqualTo(BUSINESS_KEY);
  }

  @Test
  public void shouldFilterByProcessDefinitionId() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());
    String processDefinitionId2 = processDefinition2.getId();
    engineRule.startProcessInstance(processDefinition2.getId());

    // when
    TopicSubscription topicSubscription = client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .processDefinitionId(processDefinitionId2)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    assertThat(handler.getHandledTasks().size()).isEqualTo(1);
    ExternalTask task = handler.getHandledTasks().get(0);
    assertThat(task.getProcessDefinitionId()).isEqualTo(processDefinitionId2);
    assertThat(topicSubscription.getProcessDefinitionId()).isEqualTo(processDefinitionId2);
  }

  @Test
  public void shouldFilterBySingleProcessDefinitionIdIn() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());
    String processDefinitionId2 = processDefinition2.getId();
    engineRule.startProcessInstance(processDefinitionId2);

    // when
    TopicSubscription topicSubscription = client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .processDefinitionIdIn(processDefinitionId2)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    assertThat(task.getProcessDefinitionId()).isEqualTo(processDefinitionId2);
    assertThat(topicSubscription.getProcessDefinitionIdIn().get(0)).isEqualTo(processDefinitionId2);
  }

  @Test
  public void shouldFilterByProcessDefinitionIdIn() {
    // given
    String processDefinitionId1 = processDefinition.getId();
    engineRule.startProcessInstance(processDefinitionId1);
    String processDefinitionId2 = processDefinition2.getId();
    engineRule.startProcessInstance(processDefinitionId2);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .processDefinitionIdIn(processDefinitionId1, processDefinitionId2)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    List<ExternalTask> handledTasks = handler.getHandledTasks();
    assertThat(handledTasks.size()).isEqualTo(2);
  }

  @Test
  public void shouldFilterByProcessDefinitionKey() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());
    engineRule.startProcessInstance(processDefinition2.getId());

    // when
    TopicSubscription topicSubscription = client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .processDefinitionKey(PROCESS_KEY_2)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    assertThat(handler.getHandledTasks().size()).isEqualTo(1);
    ExternalTask task = handler.getHandledTasks().get(0);
    assertThat(task.getProcessDefinitionKey()).isEqualTo(PROCESS_KEY_2);
    assertThat(topicSubscription.getProcessDefinitionKey()).isEqualTo(PROCESS_KEY_2);
  }

  @Test
  public void shouldFilterBySingleProcessDefinitionKeyIn() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());
    engineRule.startProcessInstance(processDefinition2.getId());

    // when
    TopicSubscription topicSubscription = client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .processDefinitionKeyIn(PROCESS_KEY_2)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    assertThat(task.getProcessDefinitionKey()).isEqualTo(PROCESS_KEY_2);
    assertThat(topicSubscription.getProcessDefinitionKeyIn().get(0)).isEqualTo(PROCESS_KEY_2);
  }

  @Test
  public void shouldFilterByProcessDefinitionKeyIn() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());
    engineRule.startProcessInstance(processDefinition2.getId());

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .processDefinitionKeyIn(PROCESS_KEY, PROCESS_KEY_2)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    List<ExternalTask> handledTasks = handler.getHandledTasks();
    assertThat(handledTasks.size()).isEqualTo(2);
  }

  @Test
  public void shouldFilterByBusinessKeyAndVariable() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), BUSINESS_KEY, VARIABLE_NAME, Variables.stringValue(VARIABLE_VALUE));
    engineRule.startProcessInstance(processDefinition.getId(), BUSINESS_KEY, ANOTHER_VARIABLE_NAME, Variables.stringValue(ANOTHER_VARIABLE_VALUE));

    // when
    TopicSubscription topicSubscription = client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .businessKey(BUSINESS_KEY)
      .variables(VARIABLE_NAME)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 2);

    assertThat(topicSubscription.getVariableNames().size()).isEqualTo(1);
    assertThat(topicSubscription.getVariableNames().get(0)).isEqualTo(VARIABLE_NAME);

    List<ExternalTask> handledTasks = handler.getHandledTasks();
    assertThat(handledTasks.size()).isEqualTo(2);

    ExternalTask task = handledTasks.get(0);
    assertThat(task.getBusinessKey()).isEqualTo(BUSINESS_KEY);

    if (task.getVariable(VARIABLE_NAME) != null) {
      assertThat(task.getAllVariables().size()).isEqualTo(1);
      assertThat((String) task.getVariable(VARIABLE_NAME)).isEqualTo(VARIABLE_VALUE);
    } else {
      assertThat(task.getAllVariables().size()).isEqualTo(0);
    }
  }

  @Test
  public void shouldFilterByNotExistingVariable() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME, Variables.stringValue(VARIABLE_VALUE));
    engineRule.startProcessInstance(processDefinition.getId(), ANOTHER_VARIABLE_NAME, Variables.stringValue(ANOTHER_VARIABLE_VALUE));

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .variables("notExistingVariable")
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 2);

    ExternalTask taskOne = handler.getHandledTasks().get(0);
    assertThat(taskOne.getAllVariables().size()).isEqualTo(0);

    ExternalTask taskTwo = handler.getHandledTasks().get(1);
    assertThat(taskTwo.getAllVariables().size()).isEqualTo(0);
  }

  @Test
  public void shouldFilterByNoVariable() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME, Variables.stringValue(VARIABLE_VALUE));
    engineRule.startProcessInstance(processDefinition.getId(), ANOTHER_VARIABLE_NAME, Variables.stringValue(ANOTHER_VARIABLE_VALUE));

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .variables()
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 2);

    ExternalTask taskOne = handler.getHandledTasks().get(0);
    assertThat(taskOne.getAllVariables().size()).isEqualTo(0);

    ExternalTask taskTwo = handler.getHandledTasks().get(0);
    assertThat(taskTwo.getAllVariables().size()).isEqualTo(0);
  }

  @Test
  public void shouldSetLockDuration() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    // when
    TopicSubscription topicSubscription = client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .lockDuration(1000 * 60 * 30)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    assertThat(topicSubscription.getLockDuration()).isEqualTo(1000 * 60 * 30);

    // not the most reliable way to test it
    assertThat(handler.getHandledTasks().get(0).getLockExpirationTime())
      .isAfter(new Date(System.currentTimeMillis() + 1000 * 60 * 15));
  }

  @Test
  public void shouldThrowExceptionDueToClientLockDurationNotGreaterThanZero() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    // then
    thrown.expect(ExternalTaskClientException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .lockDuration(0)
      .open();
  }

  @Test
  public void shouldThrowExceptionDueToTopicNameNull() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    // then
    thrown.expect(ExternalTaskClientException.class);

    // when
    client.subscribe(null)
      .open();
  }

  @Test
  public void shouldThrowExceptionDueToMissingHandler() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    // then
    thrown.expect(ExternalTaskClientException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .open();
  }

  @Test
  public void shouldThrowExceptionDueToHandlerNull() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    // then
    thrown.expect(ExternalTaskClientException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(null)
      .open();
  }

  @Test
  public void shouldUnsubscribeFromTopic() {
    // given
    TopicSubscription topicSubscription = client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // when
    topicSubscription.close();

    // then
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();
  }

  @Test
  public void shouldThrowExceptionDueToTopicNameAlreadySubscribed() {
    // given
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    thrown.expect(ExternalTaskClientException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();
  }

}
