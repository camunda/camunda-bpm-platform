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
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.task.impl.ExternalTaskServiceImpl;
import org.camunda.bpm.client.topic.impl.dto.TopicRequestDto;

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

  protected static final TopicSubscriptionManagerLogger LOG = ExternalTaskClientLogger.WORKER_MANAGER_LOGGER;

  protected EngineClient engineClient;
  protected List<TopicSubscriptionImpl> subscriptions;

  protected boolean isRunning;
  protected Thread thread;

  protected List<TopicRequestDto> taskTopicRequests = new ArrayList<>();
  protected Map<String, ExternalTaskHandler> lockedTasksHandlers = new HashMap<>();

  public TopicSubscriptionManager(EngineClient engineClient) {
    this.engineClient = engineClient;
    this.subscriptions = new CopyOnWriteArrayList<>();
    this.isRunning = true;

    this.thread = new Thread(this, TopicSubscriptionManager.class.getSimpleName());
    this.thread.start();
  }

  public void run() {
    while (isRunning) {
      acquire();
    }
  }

  protected void acquire() {
    
    taskTopicRequests.clear();
    lockedTasksHandlers.clear();

    subscriptions.forEach(subscription -> {
      TopicRequestDto taskTopicRequest = TopicRequestDto.fromTopicSubscription(subscription);
      taskTopicRequests.add(taskTopicRequest);

      String topicName = subscription.getTopicName();
      ExternalTaskHandler lockedTaskHandler = subscription.getLockedTaskHandler();
      lockedTasksHandlers.put(topicName, lockedTaskHandler);
    });

    if (!taskTopicRequests.isEmpty()) {
      List<ExternalTask> externalTasks = Collections.emptyList();

      try {
        externalTasks = engineClient.fetchAndLock(taskTopicRequests);
      } catch (EngineClientException e) {
        LOG.exceptionWhilePerformingFetchAndLock(e);
      }

      externalTasks.forEach(externalTask -> {
        String topicName = externalTask.getTopicName();
        ExternalTaskHandler taskHandler = lockedTasksHandlers.get(topicName);
        ExternalTaskService service = new ExternalTaskServiceImpl(externalTask.getId(), engineClient);

        try {
          taskHandler.execute(externalTask, service);
        } catch (ExternalTaskClientException e) {
          LOG.exceptionOnLockedTaskServiceMethodInvocation(e);
        } catch (Throwable e) {
          LOG.exceptionWhileExecutingLockedTaskHandler(e);
        }
      });

    }
  }

  public void shutdown() {
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

  public List<TopicSubscriptionImpl> getSubscriptions() {
    return subscriptions;
  }

}
