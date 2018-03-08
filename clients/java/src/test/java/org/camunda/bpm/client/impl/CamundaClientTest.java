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

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.exception.CamundaClientException;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.camunda.bpm.client.helper.MockProvider.ENDPOINT_URL;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Tassilo Weidner
 */
public class CamundaClientTest {

  @Test
  public void shouldSucceedAfterSanitizingEndpointUrl() {
    // given & when
    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(ENDPOINT_URL + " / / / ")
      .build();

    ExternalTaskClientImpl camundaClientImpl = ((ExternalTaskClientImpl) camundaClient);
    EngineClient engineClient = camundaClientImpl.getWorkerManager().getEngineClient();

    // then
    assertThat(engineClient.getEndpointUrl(), is(ENDPOINT_URL));
    assertFalse(engineClient.getWorkerId().isEmpty());
  }

  @Test
  public void shouldThrowExceptionDueToEndpointUrlIsEmpty() {
    // given & When
    try {
      ExternalTaskClient.create()
        .endpointUrl("")
        .build();

      fail("No CamundaClientException thrown!");
    } catch (CamundaClientException e) {
      // then
      assertThat(e.getMessage(), containsString("Endpoint URL cannot be null or an empty string"));
    }
  }

  @Test
  public void shouldThrowExceptionDueToEndpointUrlIsNull() {
    // given & When
    try {
      ExternalTaskClient.create()
        .endpointUrl(null)
        .build();

      fail("No CamundaClientException thrown!");
    } catch (CamundaClientException e) {
      // then
      assertThat(e.getMessage(), containsString("Endpoint URL cannot be null or an empty string"));
    }
  }

  @Test
  public void shouldThrowExceptionDueToUnknownHostname() throws UnknownHostException {
    // given
    ExternalTaskClientBuilderImpl camundaClientBuilder = spy(ExternalTaskClientBuilderImpl.class);
    when(camundaClientBuilder.getEndpointUrl()).thenReturn(ENDPOINT_URL);
    when(camundaClientBuilder.getHostname()).thenThrow(UnknownHostException.class);

    try {
      // when
      camundaClientBuilder.checkHostname();

      fail("No CamundaClientException thrown!");
    } catch (CamundaClientException e) {
      // then
      assertThat(e.getMessage(), containsString("Cannot get hostname"));
    }
  }

}
