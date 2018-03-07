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
package org.camunda.bpm.client.impl;

import org.camunda.bpm.client.CamundaClientException;
import org.camunda.bpm.client.LockedTask;
import org.camunda.bpm.client.LockedTaskHandler;
import org.camunda.bpm.client.LockedTaskService;
import org.camunda.bpm.client.impl.dto.request.TaskTopicRequestDto;
import org.camunda.bpm.client.impl.engineclient.EngineClient;
import org.camunda.bpm.client.impl.engineclient.EngineClientException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Tassilo Weidner
 */
public class WorkerManager implements Runnable {

  private static final WorkerManagerLogger LOG = ClientLogger.WORKER_MANAGER_LOGGER;

  private EngineClient engineClient;
  private List<WorkerSubscriptionImpl> subscriptions;
  private Thread thread;
  private boolean isRunning;

  WorkerManager(EngineClient engineClient) {
    this.engineClient = engineClient;
    this.thread = new Thread(this, WorkerManager.class.getSimpleName());
    this.isRunning = true;
    this.subscriptions = new CopyOnWriteArrayList<WorkerSubscriptionImpl>();

    this.thread.start();
  }

  public void run() {
    while (isRunning) {
      acquire();
    }
  }

  private void acquire() {
    List<TaskTopicRequestDto> taskTopicRequests = new ArrayList<TaskTopicRequestDto>();
    Map<String, LockedTaskHandler> lockedTasksHandlers = new HashMap<String, LockedTaskHandler>();

    for (WorkerSubscriptionImpl subscription : subscriptions) {
      String topicName = subscription.getTopicName();
      long lockDuration = subscription.getLockDuration();
      TaskTopicRequestDto taskTopicRequest = new TaskTopicRequestDto(topicName, lockDuration);
      taskTopicRequests.add(taskTopicRequest);

      LockedTaskHandler lockedTaskHandler = subscription.getLockedTaskHandler();
      lockedTasksHandlers.put(topicName, lockedTaskHandler);
    }

    if (!subscriptions.isEmpty()) {
      List<LockedTask> lockedTasks = Collections.emptyList();
      try {
        lockedTasks = engineClient.fetchAndLock(taskTopicRequests);
      } catch (EngineClientException e) {
        LOG.exceptionWhilePerformingFetchAndLock(e);
      }

      for (LockedTask lockedTask : lockedTasks) {
        String topicName = lockedTask.getTopicName();
        LockedTaskHandler lockedTaskHandler = lockedTasksHandlers.get(topicName);
        LockedTaskService lockedTaskService = new LockedTaskServiceImpl(lockedTask.getId(), engineClient);
        try {
          lockedTaskHandler.execute(lockedTask, lockedTaskService);
        } catch (CamundaClientException e) {
          LOG.exceptionOnLockedTaskServiceMethodInvocation(e);
        } catch (Throwable e) {
          LOG.exceptionWhileExecutingLockedTaskHandler(e);
        }
      }
    }
  }

  void shutdown() {
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

  void addSubscription(WorkerSubscriptionImpl subscription) {
    subscriptions.add(subscription);
  }

  EngineClient getEngineClient() {
    return engineClient;
  }

  List<WorkerSubscriptionImpl> getSubscriptions() {
    return subscriptions;
  }

}
