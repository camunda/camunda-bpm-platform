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
import org.camunda.bpm.client.impl.engineclient.EngineClientLogger;
import org.camunda.commons.logging.BaseLogger;

import java.net.UnknownHostException;

/**
 * @author Tassilo Weidner
 */
public class ClientLogger extends BaseLogger {

  public static final String PROJECT_CODE = "CAMCLIENT";
  public static final String PROJECT_LOGGER = "org.camunda.bpm.client";

  public static final ClientLogger CLIENT_LOGGER =
    createLogger(ClientLogger.class, PROJECT_CODE, PROJECT_LOGGER, "01");

  public static final EngineClientLogger ENGINE_CLIENT_LOGGER =
    createLogger(EngineClientLogger.class, PROJECT_CODE, PROJECT_LOGGER, "02");

  public static final WorkerManagerLogger WORKER_MANAGER_LOGGER =
    createLogger(WorkerManagerLogger.class, PROJECT_CODE, PROJECT_LOGGER, "03");

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

}
