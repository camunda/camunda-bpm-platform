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

import org.camunda.bpm.client.LockedTaskHandler;
import org.camunda.bpm.client.WorkerSubscription;
import org.camunda.bpm.client.WorkerSubscriptionBuilder;

import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class WorkerSubscriptionBuilderImpl implements WorkerSubscriptionBuilder {

  private static final ClientLogger LOG = ClientLogger.CLIENT_LOGGER;

  private String topicName;
  private long lockDuration;
  private LockedTaskHandler lockedTaskHandler;
  private WorkerManager workerManager;

  WorkerSubscriptionBuilderImpl(String topicName, WorkerManager workerManager) {
    this.topicName = topicName;
    this.workerManager = workerManager;
  }

  public WorkerSubscriptionBuilder lockDuration(long lockDuration) {
    this.lockDuration = lockDuration;
    return this;
  }

  public WorkerSubscriptionBuilder handler(LockedTaskHandler lockedTaskHandler) {
    this.lockedTaskHandler = lockedTaskHandler;
    return this;
  }

  public WorkerSubscription open() {
    if (topicName == null) {
      throw LOG.topicNameNullException();
    }

    if (lockDuration <= 0L) {
      throw LOG.lockDurationIsNotGreaterThanZeroException();
    }

    if (lockedTaskHandler == null) {
      throw LOG.lockedTaskHandlerNullException();
    }

    checkTopicNameAlreadySubscribed();

    WorkerSubscriptionImpl subscription = new WorkerSubscriptionImpl(topicName, lockDuration, lockedTaskHandler);
    workerManager.addSubscription(subscription);
    return subscription;
  }

  private void checkTopicNameAlreadySubscribed() {
    List<WorkerSubscriptionImpl> subscriptions = workerManager.getSubscriptions();
    for(WorkerSubscriptionImpl subscription : subscriptions) {
      if (subscription.getTopicName().equals(topicName)) {
        throw LOG.topicNameAlreadySubscribedException();
      }
    }
  }

}
