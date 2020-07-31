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
package org.camunda.bpm.run.test.config.cors;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.run.test.AbstractRestTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Note: To run this test via an IDE you must set the system property
 * {@code sun.net.http.allowRestrictedHeaders} to {@code true}. (e.g.
 * System.setProperty("sun.net.http.allowRestrictedHeaders", "true");)
 * 
 * @see https://jira.camunda.com/browse/CAM-11290
 */
@ActiveProfiles(profiles = { "test-cors-enabled" }, inheritProfiles = true)
public class CorsAccessControlHeadersTest extends AbstractRestTest {

  @Autowired
  ProcessEngine processEngine;

  @Test
  public void shouldRespondWithAccessControlHeaders() {
    // given
    // preflight request
    String origin = "http://other.origin";

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.HOST, "localhost");
    headers.add(HttpHeaders.ORIGIN, origin);
    headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.PUT.name());
    headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, HttpHeaders.ORIGIN);

    // when
    ResponseEntity<String> response = testRestTemplate.exchange(CONTEXT_PATH + "/task", HttpMethod.OPTIONS, new HttpEntity<>(headers), String.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getAccessControlAllowMethods()).containsExactlyInAnyOrder(HttpMethod.GET, HttpMethod.POST, HttpMethod.HEAD,
        HttpMethod.OPTIONS, HttpMethod.PUT, HttpMethod.DELETE);
    assertThat(response.getHeaders().getAccessControlAllowHeaders()).containsExactlyInAnyOrder("origin", "accept", "x-requested-with", "content-type",
        "access-control-request-method", "access-control-request-headers");
  }
}