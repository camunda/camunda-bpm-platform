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
package org.camunda.bpm.engine.rest.openapi.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.ProcessInstanceApi;
import org.openapitools.client.model.CountResultDto;
import org.openapitools.client.model.ProcessInstanceQueryDto;
import org.openapitools.client.model.SuspensionStateDto;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class ProcessInstanceTest {

  private static final String ENGINE_REST_PROCESS_INSTANCE = "/engine-rest/process-instance";

  final ProcessInstanceApi api = new ProcessInstanceApi();

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8080);

  @Test
  public void shouldQueryProcessInstancesCount() throws ApiException {
    // given
    stubFor(
        post(urlEqualTo(ENGINE_REST_PROCESS_INSTANCE + "/count"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{ \"count\": 3 }"))
        );

    // when
    ProcessInstanceQueryDto processInstanceQueryDto = new ProcessInstanceQueryDto();
    processInstanceQueryDto.setActive(true);
    CountResultDto count = api.queryProcessInstancesCount(processInstanceQueryDto);

    // then
    assertThat(count.getCount()).isEqualTo(3);
    verify(postRequestedFor(urlEqualTo(ENGINE_REST_PROCESS_INSTANCE + "/count"))
        .withRequestBody(equalToJson("{ \"active\": true }"))
        .withHeader("Content-Type",  equalTo("application/json; charset=UTF-8"))
        );

  }

  @Test
  public void shouldUpdateSuspensionStateById() throws ApiException {
    // given
    String id = "anProcessInstanceId";
    stubFor(
        put(urlEqualTo(ENGINE_REST_PROCESS_INSTANCE + "/" + id + "/suspended"))
        .willReturn(aResponse().withStatus(204))
        );

    // when
    SuspensionStateDto dto = new SuspensionStateDto();
    dto.setSuspended(true);
    api.updateSuspensionStateById(id, dto);

    // then no error
    verify(putRequestedFor(urlEqualTo(ENGINE_REST_PROCESS_INSTANCE + "/" + id + "/suspended"))
        .withRequestBody(equalToJson("{ \"suspended\": true }"))
        );
  }

}
