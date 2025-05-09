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
package org.camunda.bpm.client.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.client.util.ProcessModels.BPMN_ERROR_EXTERNAL_TASK_PROCESS;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_PRIORITY;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_FOO;
import static org.camunda.bpm.client.util.ProcessModels.TWO_PRIORITISED_EXTERNAL_TASKS_PROCESS;
import static org.camunda.bpm.client.util.PropertyUtil.DEFAULT_PROPERTIES_PATH;
import static org.camunda.bpm.client.util.PropertyUtil.loadProperties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.UrlResolver;
import org.camunda.bpm.client.backoff.BackoffStrategy;
import org.camunda.bpm.client.backoff.ErrorAwareBackoffStrategy;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.exception.EngineException;
import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.exception.RestException;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.util.PropertyUtil;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

/**
 * @author Tassilo Weidner
 */
public class ClientIT {

  protected static final String BASE_URL;

  static {
    Properties properties = loadProperties(DEFAULT_PROPERTIES_PATH);
    String engineRest = properties.getProperty(PropertyUtil.CAMUNDA_ENGINE_REST);
    String engineName = properties.getProperty(PropertyUtil.CAMUNDA_ENGINE_NAME);
    BASE_URL = engineRest + engineName;
  }

  protected ClientRule clientRule = new ClientRule(() -> ExternalTaskClient.create().baseUrl(BASE_URL)); // without lock duration
  protected EngineRule engineRule = new EngineRule();
  protected ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(clientRule).around(thrown);

  protected ExternalTaskClient client;

  protected ProcessDefinitionDto processDefinition;
  protected RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();

  @Before
  public void setup() throws Exception {
    client = clientRule.client();
    handler.clear();
    processDefinition = engineRule.deploy(BPMN_ERROR_EXTERNAL_TASK_PROCESS).get(0);
  }

  @Test
  public void shouldSanitizeWhitespaceOfBaseUrl() {
    ExternalTaskClient client = null;

    try {
      // given
      engineRule.startProcessInstance(processDefinition.getId());

      client = ExternalTaskClient.create()
        .baseUrl(" " + BASE_URL + " ")
        .build();

      client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
        .handler(handler)
        .open();

      // when
      clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 1);

      // then
      assertThat(handler.getHandledTasks()).hasSize(1);
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldSanitizeMultipleBackslashesOfBaseUrl() {
    ExternalTaskClient client = null;

    try {
      // given
      engineRule.startProcessInstance(processDefinition.getId());

      client = ExternalTaskClient.create()
        .baseUrl(BASE_URL + "//")
        .build();

      client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
        .handler(handler)
        .open();

      // when
      clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 1);

      // then
      assertThat(handler.getHandledTasks()).hasSize(1);
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldSetDefaultSerializationFormat() {
    ExternalTaskClient client = null;

    try {
      // given
      engineRule.startProcessInstance(processDefinition.getId());

      client = ExternalTaskClient.create()
        .baseUrl(BASE_URL)
        .defaultSerializationFormat("application/x-java-serialized-object")
        .build();

      final ObjectValue[] objectValue = { null };
      RecordingExternalTaskHandler recordingHandler = new RecordingExternalTaskHandler((t, s) -> {
        List<String> list = new ArrayList<>(Arrays.asList("lorem", "ipsum", "dolor", "sit"));
        objectValue[0] = Variables.objectValue(list).create();
        s.complete(t, Collections.singletonMap("variable", objectValue[0]));
      });

      client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
        .handler(recordingHandler)
        .open();

      // when
      clientRule.waitForFetchAndLockUntil(() -> recordingHandler.getHandledTasks().size() == 1);

      // then
      assertThat(objectValue[0].getSerializationDataFormat()).isEqualTo("application/x-java-serialized-object");
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldThrowExceptionDueToBaseUrlIsEmpty() {
    ExternalTaskClient client = null;

    try {
      // given
      ExternalTaskClientBuilder externalTaskClientBuilder = ExternalTaskClient.create();
      
      // then
      thrown.expect(ExternalTaskClientException.class);
      
      // when
      client = externalTaskClientBuilder.build();
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldThrowExceptionDueToBaseUrlIsNull() {
    ExternalTaskClient client = null;

    try {
      // given
      ExternalTaskClientBuilder externalTaskClientBuilder = ExternalTaskClient.create();
      
      // then
      thrown.expect(ExternalTaskClientException.class);
      
      // when
      client = externalTaskClientBuilder
          .baseUrl(null)
          .build();
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldThrowExceptionDueToBaseUrlResolverIsNull() {
    ExternalTaskClient client = null;

    try {
      // given
      ExternalTaskClientBuilder externalTaskClientBuilder = ExternalTaskClient.create();

      // then
      thrown.expect(ExternalTaskClientException.class);

      // when
      client = externalTaskClientBuilder
          .urlResolver(null)
          .build();
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldThrowExceptionDueToBaseUrlAndBaseUrlResolverIsNull() {
    ExternalTaskClient client = null;

    try {
      // given
      ExternalTaskClientBuilder externalTaskClientBuilder = ExternalTaskClient.create();

      // then
      thrown.expect(ExternalTaskClientException.class);

      // when
      client = externalTaskClientBuilder
          .baseUrl(null)
          .urlResolver(null)
          .build();
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldThrowExceptionDueToMaxTasksNotGreaterThanZero() {
    ExternalTaskClient client = null;

    try {
      // given
      ExternalTaskClientBuilder externalTaskClientBuilder = ExternalTaskClient.create()
          .baseUrl("http://camunda.com/engine-rest");
      
      // then
      thrown.expect(ExternalTaskClientException.class);
      
      // when
      client = externalTaskClientBuilder
          .maxTasks(0)
          .build();
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldUseCustomWorkerId() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    ClientRule clientRule = new ClientRule(() -> ExternalTaskClient.create()
      .baseUrl(BASE_URL)
      .workerId("aWorkerId"));

    try {
      clientRule.before();

      // when
      clientRule.client().subscribe(EXTERNAL_TASK_TOPIC_FOO)
        .handler(handler)
        .open();

      // then
      clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());
    } finally {
      clientRule.after();
    }

    ExternalTask task = handler.getHandledTasks().get(0);
    assertThat(task.getWorkerId()).isEqualTo("aWorkerId");
  }

  @Test
  public void shouldThrowExceptionDueToAsyncResponseTimeoutNotGreaterThanZero() {
    ExternalTaskClient client = null;

    try {
      // given
      ExternalTaskClientBuilder clientBuilder = ExternalTaskClient.create()
          .baseUrl("http://camunda.com/engine-rest")
          .asyncResponseTimeout(0);
      
      // then
      thrown.expect(ExternalTaskClientException.class);
      
      // when
      client = clientBuilder.build();
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldUseDefaultLockDuration() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    // when
    TopicSubscription topicSubscription = client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    assertThat(topicSubscription.getLockDuration()).isNull();

    // not the most reliable way to test it
    assertThat(handler.getHandledTasks().get(0).getLockExpirationTime())
      .isBefore(new Date(System.currentTimeMillis() + 1000 * 60));
  }

  @Test
  public void shouldUseClientLockDuration() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    ClientRule clientRule = new ClientRule(() -> ExternalTaskClient.create()
      .baseUrl(BASE_URL)
      .lockDuration(1000 * 60 * 30));

    TopicSubscription topicSubscription = null;
    try {
      clientRule.before();

      // when
      topicSubscription = clientRule.client().subscribe(EXTERNAL_TASK_TOPIC_FOO)
        .handler(handler)
        .open();

      // then
      clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());
    } finally {
      clientRule.after();
    }

    assertThat(topicSubscription.getLockDuration()).isNull();

    // not the most reliable way to test it
    assertThat(handler.getHandledTasks().get(0).getLockExpirationTime())
      .isBefore(new Date(System.currentTimeMillis() + 1000 * 60 * 30));
  }

  @Test
  public void shouldThrowExceptionDueToClientLockDurationNotGreaterThanZero() {
    ExternalTaskClient client = null;

    try {
      // given
      ExternalTaskClientBuilder externalTaskClientBuilder = ExternalTaskClient.create()
          .baseUrl("http://camunda.com/engine-rest")
          .lockDuration(0);
      
      // then
      thrown.expect(ExternalTaskClientException.class);
      
      // when
      client = externalTaskClientBuilder.build();
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldThrowExceptionDueToInterceptorIsNull() {
    ExternalTaskClient client = null;

    try {
      // given
      ExternalTaskClientBuilder externalTaskClientBuilder = ExternalTaskClient.create()
        .baseUrl("http://camunda.com/engine-rest")
        .addInterceptor(null);

      // then
      thrown.expect(ExternalTaskClientException.class);

      // when
      client = externalTaskClientBuilder.build();
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldPerformBackoff() {
    // given
    AtomicBoolean   isBackoffPerformed = new AtomicBoolean(false);
    BackoffStrategy backOffStrategy = new BackOffStrategyBean() {
      public long calculateBackoffTime() {
        isBackoffPerformed.set(true);
        return 1000L;
      }
    };

    ClientRule clientRule = new ClientRule(() -> ExternalTaskClient.create()
      .baseUrl(BASE_URL)
      .backoffStrategy(backOffStrategy));

    try {
      clientRule.before();

      // when
      clientRule.client().subscribe(EXTERNAL_TASK_TOPIC_FOO)
        .handler(handler)
        .open();

      // then
      clientRule.waitForFetchAndLockUntil(isBackoffPerformed::get);
    } finally {
      clientRule.after();
    }

    assertThat(isBackoffPerformed.get()).isTrue();
  }

  @Test
  public void shouldResetBackoff() {
    // given
    AtomicBoolean isBackoffReset = new AtomicBoolean(false);
    BackoffStrategy backOffStrategy = new BackOffStrategyBean() {
      @Override
      public void reconfigure(List<ExternalTask> externalTasks) {
        isBackoffReset.set(true);
      }
    };

    ClientRule clientRule = new ClientRule(() -> ExternalTaskClient.create()
      .baseUrl(BASE_URL)
      .backoffStrategy(backOffStrategy));

    try {
      clientRule.before();

      // when
      clientRule.client().subscribe(EXTERNAL_TASK_TOPIC_FOO)
        .handler(handler)
        .open();

      engineRule.startProcessInstance(processDefinition.getId());

      // then
      clientRule.waitForFetchAndLockUntil(isBackoffReset::get);
    } finally {
      clientRule.after();
    }

    // then
    assertThat(isBackoffReset.get()).isTrue();
  }

  @Test
  public void shouldIgnoreBackoffStrategy() {
    // given
    AtomicBoolean isBackoffStrategyIgnored = new AtomicBoolean(true);
    BackoffStrategy backoffStrategy = new BackOffStrategyBean() {
      @Override
      public void reconfigure(List<ExternalTask> externalTasks) {
        isBackoffStrategyIgnored.set(false);
      }
    };

    ClientRule clientRule = new ClientRule(() -> ExternalTaskClient.create()
      .baseUrl(BASE_URL)
      .disableBackoffStrategy()
      .backoffStrategy(backoffStrategy));

    try {
      clientRule.before();

      clientRule.client().subscribe(EXTERNAL_TASK_TOPIC_FOO)
        .handler(handler)
        .open();

      engineRule.startProcessInstance(processDefinition.getId());

      // At this point TopicSubscriptionManager#acquire might not have been executed completely
      clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 1);

      engineRule.startProcessInstance(processDefinition.getId());

      // when
      // At this point TopicSubscriptionManager#acquire have been executed completely at least once
      clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 2);
    } finally {
      clientRule.after();
    }

    // then
    assertThat(isBackoffStrategyIgnored.get()).isTrue();
  }

  @Test
  public void shouldPerformAutoFetching() {
    ExternalTaskClient client = null;

    try {
      // given
      ExternalTaskClientBuilder clientBuilder = ExternalTaskClient.create()
        .baseUrl(BASE_URL);

      // when
      client = clientBuilder.build();

      // then
      assertThat(client.isActive()).isTrue();
    } finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldDisableAutoFetching() {
    ExternalTaskClient client = null;

    try {
      // given
      ExternalTaskClientBuilder clientBuilder = ExternalTaskClient.create()
        .baseUrl(BASE_URL)
        .disableAutoFetching();

      // when
      client = clientBuilder.build();

      // then
      assertThat(client.isActive()).isFalse();
    } finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldStartFetchingWhenAutoFetchingIsDisabled() {
    ExternalTaskClient client = null;

    try {
      // given
      client = ExternalTaskClient.create()
        .baseUrl(BASE_URL)
        .disableAutoFetching()
        .build();

      // assume
      assertThat(client.isActive()).isFalse();

      // when
      client.start();

      // then
      assertThat(client.isActive()).isTrue();
    } finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldRestartFetchingWhenAutoFetchingIsDisabled() {
    ExternalTaskClient client = null;

    try {
      // given
      client = ExternalTaskClient.create()
        .baseUrl(BASE_URL)
        .disableAutoFetching()
        .build();

      client.start();
      client.stop();

      // assume
      assertThat(client.isActive()).isFalse();

      // when
      client.start();

      // then
      assertThat(client.isActive()).isTrue();
    } finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldFailWithCorrectError() throws FileNotFoundException {
    BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(new FileInputStream("src/it/resources/failing-output-mapping-model.bpmn"));
    String processDefinitionKey = engineRule.deploy(bpmnModelInstance).get(0).getId();

    ExternalTaskClient client = null;

    try {
      // given
      engineRule.startProcessInstance(processDefinitionKey);

      client = ExternalTaskClient.create()
        .baseUrl(BASE_URL)
        .defaultSerializationFormat("application/x-java-serialized-object")
        .build();

      RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((t, s) -> s.complete(t));
      client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
        .handler(handler)
        .open();

      // when
      clientRule.waitForFetchAndLockUntil(handler::isFailed);

      // then
      assertThat(handler.getException().getType()).isEqualTo("ScriptEvaluationException");
    } finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldPassExceptionToErrorAwareBackoffStrategy() {
    // given
    ExternalTaskClient client = null;

    AtomicReference<ExternalTaskClientException> ex = new AtomicReference<>();

    try {
      client = ExternalTaskClient.create()
        .baseUrl(BASE_URL)
        .backoffStrategy(new ErrorAwareBackoffStrategy() {

          @Override
          public void reconfigure(List<ExternalTask> externalTasks, ExternalTaskClientException e) {
            ex.set(e);
          }

          @Override
          public long calculateBackoffTime() {
            return 0;
          }
        })
        .build();

      RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((t, s) -> s.complete(t));
      client.subscribe("something")
        .processVariableEquals("foo", new MyPojo())
        .handler(handler)
        .open();

      // when
      clientRule.waitForFetchAndLockUntil(() -> ex.get() != null);

      // then
      EngineException exception = (EngineException) ex.get();
      assertThat(exception).isInstanceOf(EngineException.class);
      assertThat(exception.getCode()).isEqualTo(0);
      assertThat(exception.getType()).isEqualTo("ProcessEngineException");
      assertThat(exception.getMessage()).isEqualTo("TASK/CLIENT-03009 Exception while fetching and locking task: Object values cannot be used to query");
      assertThat(exception.getHttpStatusCode()).isEqualTo(500);
      assertThat(exception.getCause())
          .isInstanceOf(RestException.class)
          .hasMessage("Object values cannot be used to query");

    } finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldUsePriorityOnFetchAndLockByDefault() {
    String processId = engineRule.deploy(TWO_PRIORITISED_EXTERNAL_TASKS_PROCESS).get(0).getId();

    // given
    engineRule.startProcessInstance(processId).getId();

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
        .handler(handler)
        .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 2);

    assertThat(handler.getHandledTasks()).hasSize(2);
    assertThat(handler.getHandledTasks().get(0).getPriority()).isGreaterThan(EXTERNAL_TASK_PRIORITY);
    assertThat(handler.getHandledTasks().get(1).getPriority()).isEqualTo(EXTERNAL_TASK_PRIORITY);
  }

  @Test
  public void shouldNotUsePriorityOnFetchAndLock() {
    String processId = engineRule.deploy(TWO_PRIORITISED_EXTERNAL_TASKS_PROCESS).get(0).getId();
    ExternalTaskClient client = null;

    try {
      // given
      engineRule.startProcessInstance(processId).getId();

      client = ExternalTaskClient.create()
                                 .baseUrl(" " + BASE_URL + " ")
                                 .maxTasks(3)
                                 .usePriority(false)
                                 .build();

      client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

      // when
      clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 2);

      // then tasks are fetched in an arbitrary order
      // and a first low priority task can't be guaranteed
      assertThat(handler.getHandledTasks()).hasSize(2);
      assertThat(handler.getHandledTasks().get(0).getPriority()).isGreaterThanOrEqualTo(EXTERNAL_TASK_PRIORITY);
      assertThat(handler.getHandledTasks().get(1).getPriority()).isGreaterThanOrEqualTo(EXTERNAL_TASK_PRIORITY);
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldUseDescOrderByDefaultOnFetchAndLockWithUseCreateTime() {
    // given
    String process1 = engineRule.deploy(TWO_PRIORITISED_EXTERNAL_TASKS_PROCESS).get(0).getId();

    ExternalTaskClient client = null;

    try {
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);

      client = ExternalTaskClient.create()
          .baseUrl(" " + BASE_URL + " ")
          .maxTasks(3)
          .usePriority(false)
          // when
          .useCreateTime(true)
          .build();

      client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
          .handler(handler)
          .open();

      clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 4);

      // then tasks are fetched in creation time DESC order
      assertThat(handler.getHandledTasks()).hasSize(4);

      var tasks = handler.getHandledTasks();

      assertThat(tasks)
          .extracting("createTime", Date.class)
          .isSortedAccordingTo(Comparator.reverseOrder());
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldUseDescOrderOnFetchAndLockWithUseCreateTimeDESC() {
    String process1 = engineRule.deploy(TWO_PRIORITISED_EXTERNAL_TASKS_PROCESS).get(0).getId();

    ExternalTaskClient client = null;

    try {
      // given
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);

      client = ExternalTaskClient.create()
          .baseUrl(" " + BASE_URL + " ")
          .maxTasks(3)
          .usePriority(false)
          // when
          .orderByCreateTime().desc()
          .build();

      client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
          .handler(handler)
          .open();

      clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 4);

      // then tasks are fetched in creation time DESC order
      assertThat(handler.getHandledTasks()).hasSize(4);

      var tasks = handler.getHandledTasks();

      assertThat(tasks)
          .extracting("createTime", Date.class)
          .isSortedAccordingTo(Comparator.reverseOrder());
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldUseAscOrderOnFetchAndLockWithUseCreateTimeASC() {
    String process1 = engineRule.deploy(TWO_PRIORITISED_EXTERNAL_TASKS_PROCESS).get(0).getId();

    ExternalTaskClient client = null;

    try {
      // given
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);

      client = ExternalTaskClient.create()
          .baseUrl(" " + BASE_URL + " ")
          .maxTasks(3)
          .usePriority(false)
          // when
          .orderByCreateTime().asc()
          .build();

      client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
          .handler(handler)
          .open();

      clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 4);

      // then tasks are fetched in creation time ASC order
      assertThat(handler.getHandledTasks()).hasSize(4);

      var tasks = handler.getHandledTasks();

      assertThat(tasks)
          .extracting("createTime", Date.class)
          .isSortedAccordingTo(Comparator.naturalOrder());
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldUseDescOrderOnFetchAndLockWithUseCreateTimeTrue() {
    String process1 = engineRule.deploy(TWO_PRIORITISED_EXTERNAL_TASKS_PROCESS).get(0).getId();

    ExternalTaskClient client = null;

    try {
      // given
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);

      client = ExternalTaskClient.create()
          .baseUrl(" " + BASE_URL + " ")
          .maxTasks(3)
          .usePriority(false)
          // when
          .useCreateTime(true)
          .build();

      client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
          .handler(handler)
          .open();

      clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 4);

      // then tasks are fetched in creation time DESC order
      assertThat(handler.getHandledTasks()).hasSize(4);

      var tasks = handler.getHandledTasks();

      assertThat(tasks)
          .extracting("createTime", Date.class)
          .isSortedAccordingTo(Comparator.reverseOrder());
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldUsePriorityOrderOnFetchAndLockWithUseCreateTimeFalse() {
    String process1 = engineRule.deploy(TWO_PRIORITISED_EXTERNAL_TASKS_PROCESS).get(0).getId();

    ExternalTaskClient client = null;

    try {
      // given
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);

      client = ExternalTaskClient.create()
          .baseUrl(" " + BASE_URL + " ")
          .maxTasks(3)
          // when
          .usePriority(true)
          .useCreateTime(false)
          .build();

      client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
          .handler(handler)
          .open();

      clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 4);

      // then tasks are fetched in creation time DESC order
      assertThat(handler.getHandledTasks()).hasSize(4);

      var tasks = handler.getHandledTasks();

      assertThat(tasks)
          .extracting("priority", Date.class)
          .isSortedAccordingTo(Comparator.reverseOrder());
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldUseCreateTimeDescOnPriorityEquality() {
    String process1 = engineRule.deploy(TWO_PRIORITISED_EXTERNAL_TASKS_PROCESS).get(0).getId();

    ExternalTaskClient client = null;

    try {
      // given
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);

      client = ExternalTaskClient.create()
          .baseUrl(" " + BASE_URL + " ")
          .maxTasks(3)
          // when
          .usePriority(true)
          .orderByCreateTime().desc()
          .build();

      client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
          .handler(handler)
          .open();

      clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 4);

      // then
      assertThat(handler.getHandledTasks()).hasSize(4);

      var tasks = handler.getHandledTasks();

      // tasks are fetched ordered by priority; when priority is equal, create time DESC is used
      assertThat(tasks.get(0).getPriority()).isEqualTo(EXTERNAL_TASK_PRIORITY + 1000L);
      assertThat(tasks.get(1).getPriority()).isEqualTo(EXTERNAL_TASK_PRIORITY + 1000L);

      // given the same priority (elements 0 & 1), their date should be Descending ordered
      assertThat(tasks.get(0).getCreateTime()).isAfterOrEqualsTo(tasks.get(1).getCreateTime());

      assertThat(tasks.get(2).getPriority()).isEqualTo(EXTERNAL_TASK_PRIORITY);
      assertThat(tasks.get(3).getPriority()).isEqualTo(EXTERNAL_TASK_PRIORITY);

      // given the same priority (elements 2 & 3), their date should be Descending ordered
      assertThat(tasks.get(2).getCreateTime()).isAfterOrEqualsTo(tasks.get(3).getCreateTime());
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldUseCreateTimeAscOnPriorityEquality() {
    String process1 = engineRule.deploy(TWO_PRIORITISED_EXTERNAL_TASKS_PROCESS).get(0).getId();

    ExternalTaskClient client = null;

    try {
      // given
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);
      engineRule.startProcessInstance(process1);
      incrementClock(60_000);

      client = ExternalTaskClient.create()
          .baseUrl(" " + BASE_URL + " ")
          .maxTasks(3)
          // when
          .usePriority(true)
          .orderByCreateTime().asc()
          .build();

      client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
          .handler(handler)
          .open();

      clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() == 4);

      assertThat(handler.getHandledTasks()).hasSize(4);

      var tasks = handler.getHandledTasks();

      // then

      // tasks are fetched ordered by priority; when priority is equal, create time ASC is used
      assertThat(tasks.get(0).getPriority()).isEqualTo(EXTERNAL_TASK_PRIORITY + 1000L);
      assertThat(tasks.get(1).getPriority()).isEqualTo(EXTERNAL_TASK_PRIORITY + 1000L);

      // given the same priority (elements 0 & 1), their date should be Ascending ordered
      assertThat(tasks.get(0).getCreateTime()).isBeforeOrEqualsTo(tasks.get(1).getCreateTime());

      assertThat(tasks.get(2).getPriority()).isEqualTo(EXTERNAL_TASK_PRIORITY);
      assertThat(tasks.get(3).getPriority()).isEqualTo(EXTERNAL_TASK_PRIORITY);

      // given the same priority (elements 2 & 3), their date should be Ascending ordered
      assertThat(tasks.get(2).getCreateTime()).isBeforeOrEqualsTo(tasks.get(3).getCreateTime());
    }
    finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void shouldThrowExceptionOnSubscribeWithNullOrderConfig() {
    // when
    assertThatThrownBy(() -> ExternalTaskClient.create()
        .baseUrl("baseUrl")
        .orderByCreateTime()
        .build()
    ) // then
        .isInstanceOf(ExternalTaskClientException.class)
        .hasMessage("Invalid query: call asc() or desc() after using orderByXX()");
  }

  @Test
  public void shouldThrowExceptionOnInvalidOrderConfig() {
    // when
    assertThatThrownBy(() -> ExternalTaskClient.create()
        .baseUrl("baseUrl")
        .orderByCreateTime()
        .desc()
        .desc()
        .build()
    ) // then
        .isInstanceOf(ExternalTaskClientException.class)
        .hasMessage("Invalid query: can specify only one direction desc() or asc() for an ordering constraint");
  }

  @Test
  public void shouldThrowExceptionOnMissingOrderbyConfig() {
    // when
    assertThatThrownBy(() -> ExternalTaskClient.create()
        .baseUrl("baseUrl")
        .asc()
    ) // then
        .isInstanceOf(ExternalTaskClientException.class)
        .hasMessage("Invalid query: You should call any of the orderBy methods first before specifying a direction");
  }

  static class MyPojo {

    protected String id;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

  }

  public void incrementClock(long seconds) {
    long time = ClockUtil.getCurrentTime().getTime();
    ClockUtil.setCurrentTime(new Date(time + seconds * 1000));
  }

}
