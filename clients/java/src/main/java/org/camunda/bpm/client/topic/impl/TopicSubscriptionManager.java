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
package org.camunda.bpm.client.topic.impl;

import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.impl.EngineClient;
import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.impl.variable.VariableMappers;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.camunda.bpm.client.task.impl.ExternalTaskServiceImpl;
import org.camunda.bpm.client.task.impl.dto.TypedValueDto;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.impl.dto.TopicRequestDto;
import org.camunda.bpm.engine.variable.VariableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Tassilo Weidner
 */
public class TopicSubscriptionManager implements Runnable {

  protected static final TopicSubscriptionManagerLogger LOG = ExternalTaskClientLogger.TOPIC_SUBSCRIPTION_MANAGER_LOGGER;

  protected EngineClient engineClient;
  protected List<TopicSubscription> subscriptions;

  protected boolean isRunning;
  protected Thread thread;

  protected VariableMappers variableMappers;

  public TopicSubscriptionManager(EngineClient engineClient, VariableMappers variableMappers) {
    this.engineClient = engineClient;
    this.subscriptions = new CopyOnWriteArrayList<>();
    this.isRunning = true;

    this.thread = new Thread(this, TopicSubscriptionManager.class.getSimpleName());
    this.thread.start();

    this.variableMappers = variableMappers;
  }

  public void run() {
    while (isRunning) {
      acquire();
    }
  }

  protected void acquire() {
    List<TopicRequestDto> taskTopicRequests = new ArrayList<>();
    Map<String, ExternalTaskHandler> externalTaskHandlers = new HashMap<>();

    subscriptions.forEach(subscription -> {
      TopicRequestDto taskTopicRequest = TopicRequestDto.fromTopicSubscription(subscription);
      taskTopicRequests.add(taskTopicRequest);

      String topicName = subscription.getTopicName();
      ExternalTaskHandler externalTaskHandler = subscription.getExternalTaskHandler();
      externalTaskHandlers.put(topicName, externalTaskHandler);
    });

    if (!taskTopicRequests.isEmpty()) {
      List<ExternalTask> externalTasks = Collections.emptyList();

      try {
        externalTasks = engineClient.fetchAndLock(taskTopicRequests);
      } catch (EngineClientException e) {
        LOG.exceptionWhilePerformingFetchAndLock(e);
      }

      externalTasks.forEach(externalTask -> {
        Map<String, TypedValueDto> variableDtoMap = ((ExternalTaskImpl) externalTask).getVariables();
        VariableMap variableMap = null;

        boolean variablesDeserialized = false;
        try {

          variableMap = variableMappers.deserializeVariables(variableDtoMap);
          variablesDeserialized = true;

        } catch (Throwable e) {
          LOG.exceptionWhileDeserializingVariables(e);
        }

        if (variablesDeserialized) {
          ((ExternalTaskImpl) externalTask).setVariableMappers(variableMappers);
          ((ExternalTaskImpl) externalTask).setLocalVariableMap(variableMap);

          String topicName = externalTask.getTopicName();
          ExternalTaskHandler taskHandler = externalTaskHandlers.get(topicName);
          ExternalTaskService service = new ExternalTaskServiceImpl(externalTask.getId(), engineClient);

          try {
            taskHandler.execute(externalTask, service);
          } catch (ExternalTaskClientException e) {
            LOG.exceptionOnExternalTaskServiceMethodInvocation(e);
          } catch (Throwable e) {
            LOG.exceptionWhileExecutingExternalTaskHandler(e);
          }
        } // else: skip handler execution
      });

    }
  }

  public void stop() {
    if (!isRunning) {
      return;
    }

    isRunning = false;

    try {
      thread.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOG.exceptionWhileShuttingDown(e);
    }
  }

  protected void subscribe(TopicSubscriptionImpl subscription) {
    subscriptions.add(subscription);
  }

  protected void unsubscribe(TopicSubscriptionImpl subscription) {
    subscriptions.remove(subscription);
  }

  public EngineClient getEngineClient() {
    return engineClient;
  }

  public List<TopicSubscription> getSubscriptions() {
    return subscriptions;
  }

}
