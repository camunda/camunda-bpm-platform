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
package org.camunda.bpm.client.topic.impl;

import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.topic.impl.dto.TopicRequestDto;

import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class TopicSubscriptionManagerLogger extends ExternalTaskClientLogger {

  protected void exceptionWhilePerformingFetchAndLock(EngineClientException e) {
    logError(
      "001", "Exception while fetching and locking task.", e);
  }

  public void exceptionWhileExecutingExternalTaskHandler(String topicName, Throwable e) {
    logError(
      "002",
      String.format("Exception while executing external task handler '%s'.", topicName), e);
  }

  protected void exceptionWhileShuttingDown(InterruptedException e) {
    logError(
      "003", "Exception while shutting down:", e);
  }

  protected void exceptionOnExternalTaskServiceMethodInvocation(String topicName, ExternalTaskClientException e) {
    logError(
      "004",
      String.format("Exception on external task service method invocation for topic '%s':", topicName), e);
  }

  protected void exceptionWhileExecutingBackoffStrategyMethod(Throwable e) {
    logError(
      "005", "Exception while executing back off strategy method.", e);
  }

  protected void exceptionWhileAcquiringTasks(Throwable e) {
    logError(
      "006", "Exception while acquiring tasks.", e);
  }

  public void taskHandlerIsNull(String topicName) {
    logError(
      "007",
      String.format("Task handler is null for topic '%s'.", topicName));
  }

  protected void fetchAndLock(List<TopicRequestDto> subscriptions) {
    logDebug(
      "008",
      String.format("Fetch and lock new external tasks for %d topics", subscriptions.size()));
  }

}
