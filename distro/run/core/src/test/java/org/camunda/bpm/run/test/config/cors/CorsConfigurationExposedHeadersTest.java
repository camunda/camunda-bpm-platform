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

import java.util.List;

import org.camunda.bpm.run.property.CamundaBpmRunCorsProperty;
import org.camunda.bpm.run.test.AbstractRestTest;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Note: To run this test via an IDE you must set the system property
 * {@code sun.net.http.allowRestrictedHeaders} to {@code true}.
 * (e.g. System.setProperty("sun.net.http.allowRestrictedHeaders", "true");)
 *
 * @see https://jira.camunda.com/browse/CAM-11290
 */
@ActiveProfiles(profiles = { "test-cors-enabled" }, inheritProfiles = true)
@TestPropertySource(properties = {CamundaBpmRunCorsProperty.PREFIX + ".exposed-headers=X-CUSTOM-HEADER-ALLOWED"})
public class CorsConfigurationExposedHeadersTest extends AbstractRestTest {

  @Test
  public void shouldProvideResponseWithExposedHeaders() {
    // given
    // same origin
    String origin = "http://localhost:" + localPort;

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.ORIGIN, origin);
    headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name());

    // when
    ResponseEntity<List> response = testRestTemplate
        .exchange(CONTEXT_PATH + "/task",
                  HttpMethod.OPTIONS,
                  new HttpEntity<>(headers),
                  List.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getAccessControlExposeHeaders()).containsExactly("X-CUSTOM-HEADER-ALLOWED");
  }

}