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

import org.camunda.bpm.client.exception.BpmnErrorException;
import org.camunda.bpm.client.exception.CamundaClientException;
import org.camunda.bpm.client.exception.CompleteTaskException;
import org.camunda.bpm.client.exception.ExtendLockException;
import org.camunda.bpm.client.exception.TaskFailureException;
import org.camunda.bpm.client.exception.UnlockTaskException;
import org.camunda.bpm.client.topic.impl.TopicSubscriptionManagerLogger;
import org.camunda.commons.logging.BaseLogger;

import java.net.UnknownHostException;

/**
 * @author Tassilo Weidner
 */
public class ExternalTaskClientLogger extends BaseLogger {

  public static final String PROJECT_CODE = "CAMUNDA_EXTERNAL_TASK_CLIENT";
  public static final String PROJECT_LOGGER = "org.camunda.bpm.client";

  public static final ExternalTaskClientLogger CLIENT_LOGGER =
    createLogger(ExternalTaskClientLogger.class, PROJECT_CODE, PROJECT_LOGGER, "01");

  public static final EngineClientLogger ENGINE_CLIENT_LOGGER =
    createLogger(EngineClientLogger.class, PROJECT_CODE, PROJECT_LOGGER, "02");

  public static final TopicSubscriptionManagerLogger WORKER_MANAGER_LOGGER =
    createLogger(TopicSubscriptionManagerLogger.class, PROJECT_CODE, PROJECT_LOGGER, "03");

  public CamundaClientException endpointUrlNullException() {
    return new CamundaClientException(exceptionMessage(
      "001", "Endpoint URL cannot be null or an empty string"));
  }

  public CamundaClientException cannotGetHostnameException(UnknownHostException e) {
    return new CamundaClientException(exceptionMessage(
      "002", "Cannot get hostname", e));
  }

  public CamundaClientException topicNameNullException() {
    return new CamundaClientException(exceptionMessage(
      "003", "Topic name cannot be null"));
  }

  public CamundaClientException lockDurationIsNotGreaterThanZeroException() {
    return new CamundaClientException(exceptionMessage(
      "004", "Lock duration is not greater than 0"));
  }

  public CamundaClientException lockedTaskHandlerNullException() {
    return new CamundaClientException(exceptionMessage(
      "005", "Locked task handler cannot be null"));
  }

  public CamundaClientException topicNameAlreadySubscribedException() {
    return new CamundaClientException(exceptionMessage(
      "006", "Topic name has already been subscribed"));
  }

  public UnlockTaskException unlockTaskException(EngineClientException e) {
    return new UnlockTaskException(exceptionMessage(
      "007", "Exception while unlocking task '{}'", e));
  }

  public CompleteTaskException completeTaskException(EngineClientException e) {
    return new CompleteTaskException(exceptionMessage(
      "008", "Exception while completing task '{}'", e));
  }

  public TaskFailureException taskFailureException(EngineClientException e) {
    return new TaskFailureException(exceptionMessage(
      "009", "Exception while notifying task failure '{}'", e));
  }

  public BpmnErrorException bpmnErrorException(EngineClientException e) {
    return new BpmnErrorException(exceptionMessage(
      "010", "Exception while notifying bpmn error '{}'", e));
  }

  public ExtendLockException extendLockException(EngineClientException e) {
    return new ExtendLockException(exceptionMessage(
      "011", "Exception while extending lock '{}'", e));
  }

}
