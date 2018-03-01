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
import org.camunda.bpm.client.WorkerSubscription;
import org.camunda.bpm.client.WorkerSubscriptionBuilder;

/**
 * @author Tassilo Weidner
 */
public class WorkerSubscriptionBuilderImpl implements WorkerSubscriptionBuilder {

  private String topicName;
  private long lockDuration;
  private WorkerManager workerManager;

  public WorkerSubscriptionBuilderImpl(String topicName, WorkerManager workerManager) {
    this.topicName = topicName;
    this.workerManager = workerManager;
  }

  public WorkerSubscriptionBuilder lockDuration(long lockDuration) {
    this.lockDuration = lockDuration;
    return this;
  }

  public WorkerSubscription execute() {
    if (topicName == null) {
      throw new CamundaClientException("Topic name cannot be null");
    }

    if (lockDuration <= 0L) {
      throw new CamundaClientException("Lock duration is not greater than 0");
    }

    WorkerSubscriptionImpl subscription = new WorkerSubscriptionImpl(topicName, lockDuration);
    workerManager.addSubscription(subscription);
    return subscription;
  }

}
