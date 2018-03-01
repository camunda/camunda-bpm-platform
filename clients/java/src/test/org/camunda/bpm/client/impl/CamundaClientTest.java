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
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Tassilo Weidner
 */
public class CamundaClientTest {

  private static String ENDPOINT_URL = "http://localhost:8080/engine-rest";

  @Test
  public void shouldSucceedDueToExistingEndpointUrl() {
    // given & when
    CamundaClient camundaClient = CamundaClient.create()
      .endpointUrl(ENDPOINT_URL)
      .build();

    CamundaClientImpl camundaClientImpl = ((CamundaClientImpl) camundaClient);
    RestRequestExecutor restRequestExecutor = camundaClientImpl.getWorkerManager().getRequestExecutor();

    // then
    assertThat(restRequestExecutor.getUrl(), is(ENDPOINT_URL));
    assertFalse(restRequestExecutor.getWorkerId().isEmpty());
  }

  @Test
  public void shouldThrowExceptionDueToEndpointUrlisEmpty() {
    // given & When
    try {
      CamundaClient.create()
        .endpointUrl("")
        .build();

      fail("No CamundaClientException thrown!");
    } catch (CamundaClientException e) {
      // then
      assertThat(e.getMessage(), is("Endpoint URL cannot be empty"));
    }
  }

  @Test
  public void shouldThrowExceptionDueToEndpointUrlIsNull() {
    // given & When
    try {
      CamundaClient.create()
        .endpointUrl(null)
        .build();

      fail("No CamundaClientException thrown!");
    } catch (CamundaClientException e) {
      // then
      assertThat(e.getMessage(), is("Endpoint URL cannot be empty"));
    }
  }

  @Test
  public void shouldThrowExceptionDueToUnknownHostname() throws UnknownHostException {
    // given
    CamundaClientBuilderImpl camundaClientBuilder = mock(CamundaClientBuilderImpl.class);
    when(camundaClientBuilder.getEndpointUrl()).thenReturn(ENDPOINT_URL);

    CamundaClientImpl camundaClient = spy(new CamundaClientImpl());
    when(camundaClient.getHostname()).thenThrow(UnknownHostException.class);

    try {
      // when
      camundaClient.checkHostname();

      fail("No CamundaClientException thrown!");
    } catch (CamundaClientException e) {
      // then
      assertThat(e.getMessage(), is("Cannot get hostname"));
    }
  }

}
