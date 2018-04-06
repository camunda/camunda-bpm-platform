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

import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscription;

import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class TopicSubscriptionImpl implements TopicSubscription {

  protected String topicName;
  protected Long lockDuration;
  protected ExternalTaskHandler externalTaskHandler;
  protected TopicSubscriptionManager topicSubscriptionManager;
  protected List<String> variableNames;

  public TopicSubscriptionImpl(String topicName, Long lockDuration, ExternalTaskHandler externalTaskHandler,
                               TopicSubscriptionManager topicSubscriptionManager, List<String> variableNames) {
    this.topicName = topicName;
    this.lockDuration = lockDuration;
    this.externalTaskHandler = externalTaskHandler;
    this.topicSubscriptionManager = topicSubscriptionManager;
    this.variableNames = variableNames;
  }

  public String getTopicName() {
    return topicName;
  }

  public Long getLockDuration() {
    return lockDuration;
  }

  public ExternalTaskHandler getExternalTaskHandler() {
    return externalTaskHandler;
  }

  @Override
  public void close() {
    topicSubscriptionManager.unsubscribe(this);
  }

  public List<String> getVariableNames() {
    return variableNames;
  }

}
