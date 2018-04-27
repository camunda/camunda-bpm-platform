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
package org.camunda.bpm.client.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.client.util.ProcessModels.BPMN_ERROR_EXTERNAL_TASK_PROCESS;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_FOO;
import static org.camunda.bpm.client.util.PropertyUtil.DEFAULT_PROPERTIES_PATH;
import static org.camunda.bpm.client.util.PropertyUtil.loadProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.camunda.bpm.client.backoff.BackoffStrategy;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.util.PropertyUtil;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
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
      assertThat(handler.getHandledTasks().size()).isEqualTo(1);
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
      assertThat(handler.getHandledTasks().size()).isEqualTo(1);
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

}
