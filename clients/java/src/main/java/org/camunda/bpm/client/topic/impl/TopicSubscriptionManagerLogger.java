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
import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;

/**
 * @author Tassilo Weidner
 */
public class TopicSubscriptionManagerLogger extends ExternalTaskClientLogger {

  protected void exceptionWhilePerformingFetchAndLock(EngineClientException e) {
    logError(
      "001", "Exception while fetch and lock tasks '{}'", e);
  }

  protected void exceptionWhileExecutingExternalTaskHandler(Throwable e) {
    logError(
      "002", "Exception while executing external task handler '{}'", e);
  }

  protected void exceptionWhileShuttingDown(InterruptedException e) {
    logError(
      "003", "Exception while shutting down '{}'", e);
  }

  protected void exceptionOnExternalTaskServiceMethodInvocation(ExternalTaskClientException e) {
    logError(
      "004", "Exception on external task service method invocation '{}'", e);
  }

  protected void exceptionWhileDeserializingVariables(Throwable e) {
    logError(
      "005", "Exception while deserializing variables '{}'", e);
  }

  protected void exceptionWhileDeserializingVariables(String message) {
    logError(
      "006", "Exception while deserializing variables '{}'", message);
  }

}
