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

import org.camunda.bpm.client.CamundaClient;
import org.camunda.bpm.client.CamundaClientException;
import org.camunda.bpm.client.WorkerSubscriptionBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * @author Tassilo Weidner
 */
public class CamundaClientImpl extends CamundaClient {

  private WorkerManager workerManager;

  protected CamundaClientImpl () {

  }

  protected CamundaClientImpl(CamundaClientBuilderImpl clientBuilder) {
    String endpointUrl = clientBuilder.getEndpointUrl();
    if (endpointUrl == null || endpointUrl.isEmpty()) {
      throw new CamundaClientException("Endpoint URL cannot be empty");
    }

    String hostname = checkHostname();
    String workerId = hostname + UUID.randomUUID();
    RestRequestExecutor requestExecutor = new RestRequestExecutor(workerId, endpointUrl);
    workerManager = new WorkerManager(requestExecutor);
  }

  public WorkerSubscriptionBuilder subscribe(String topicName) {
    return new WorkerSubscriptionBuilderImpl(topicName, workerManager);
  }

  protected String checkHostname() {
    String hostname;
    try {
      hostname = getHostname();
    } catch (UnknownHostException e) {
      throw new CamundaClientException("Cannot get hostname", e);
    }

    return hostname;
  }

  protected String getHostname() throws UnknownHostException {
    return InetAddress.getLocalHost().getHostName();
  }

  protected WorkerManager getWorkerManager() {
    return workerManager;
  }

}
