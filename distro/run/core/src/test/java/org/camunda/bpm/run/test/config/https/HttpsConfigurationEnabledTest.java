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
package org.camunda.bpm.run.test.config.https;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.run.CamundaBpmRun;
import org.camunda.bpm.run.test.AbstractRestTest;
import org.camunda.bpm.run.test.util.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;

@SpringBootTest(classes = { CamundaBpmRun.class }, webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(profiles = { "test-https-enabled" }, inheritProfiles = true)
public class HttpsConfigurationEnabledTest extends AbstractRestTest {
  
  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Before
  public void init() throws Exception {
    TestUtils.trustSelfSignedSSL();
  }

  @Test
  public void shouldConnectWithHttps() {
    // given
    String url = "https://localhost:" + localPort + CONTEXT_PATH + "/task";

    // when
    ResponseEntity<List> response = testRestTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null), List.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldNotRedirect() {
    // given
    String url = "http://localhost:" + 8080 + CONTEXT_PATH + "/task";

    // then
    exceptionRule.expect(ResourceAccessException.class);
    exceptionRule.expectMessage("Connection refused");

    // then
    ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null), String.class);
  }
}
