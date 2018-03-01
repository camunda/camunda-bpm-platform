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
import org.camunda.bpm.client.CamundaClientBuilder;

/**
 * @author Tassilo Weidner
 */
public class CamundaClientBuilderImpl implements CamundaClientBuilder {

  private String endpointUrl;

  public CamundaClientBuilder endpointUrl(String endpointUrl) {
    this.endpointUrl = endpointUrl;
    return this;
  }

  public CamundaClient build() {
    return new CamundaClientImpl(this);
  }

  String getEndpointUrl() {
    return endpointUrl;
  }

}
