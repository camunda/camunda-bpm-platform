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
package org.camunda.bpm.spring.boot.starter.webapp.apppath;

import org.camunda.bpm.spring.boot.starter.webapp.TestApplication;
import org.camunda.bpm.spring.boot.starter.webapp.filter.util.HeaderRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = { TestApplication.class },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "camunda.bpm.webapp.applicationPath=" + ChangedAppPathIT.MY_APP_PATH
})
public class ChangedAppPathIT {

  protected static final String MY_APP_PATH = "/my/application/path";

  @Rule
  public HeaderRule headerRule = new HeaderRule();

  @LocalServerPort
  public int port;

  @Autowired
  protected TestRestTemplate restClient;

  @Test
  public void shouldCheckPresenceOfCsrfPreventionFilter() {
    // given

    // when
    headerRule.performRequest("http://localhost:" + port + MY_APP_PATH +
        "/app/tasklist/default");

    // then
    String xsrfCookieValue = headerRule.getXsrfCookieValue();
    String xsrfTokenHeader = headerRule.getXsrfTokenHeader();

    assertThat(xsrfCookieValue).matches("XSRF-TOKEN=[A-Z0-9]{32};" +
        "Path=" + MY_APP_PATH + ";SameSite=Lax");
    assertThat(xsrfTokenHeader).matches("[A-Z0-9]{32}");

    assertThat(xsrfCookieValue).contains(xsrfTokenHeader);
  }

  @Test
  public void shouldCheckPresenceOfRedirection() {
    // given

    // when
    headerRule.performRequest("http://localhost:" + port + "/");

    // then
    assertThat(headerRule.getHeader("Location")).isEqualTo("http://localhost:" + port +
        MY_APP_PATH + "/app/");
  }

  @Test
  public void shouldCheckPresenceOfHeaderSecurityFilter() {
    // given

    // when
    ResponseEntity<String> response = restClient.getForEntity(MY_APP_PATH +
        "/app/tasklist/default", String.class);

    // then
    List<String> contentSecurityPolicyHeaders = response.getHeaders()
        .get("Content-Security-Policy");

    assertThat(contentSecurityPolicyHeaders).containsExactly("base-uri 'self'");
  }

  @Test
  public void shouldCheckPresenceOfCacheControlFilter() {
    // given

    // when
    ResponseEntity<String> response = restClient.getForEntity(MY_APP_PATH +
        "/app/admin/styles/styles.css", String.class);

    // then
    List<String> cacheControlHeaders = response.getHeaders()
        .get("Cache-Control");

    assertThat(cacheControlHeaders).containsExactly("no-cache");
  }

  @Test
  public void shouldCheckPresenceOfRestApi() {
    // given

    // when
    ResponseEntity<String> response = restClient.getForEntity(MY_APP_PATH +
        "/api/engine/engine/", String.class);

    // then
    assertThat(response.getBody()).isEqualTo("[{\"name\":\"default\"}]");
  }

  @Test
  public void shouldCheckPresenceOfSecurityFilter() {
    // given

    // when
    ResponseEntity<String> response = restClient.getForEntity(MY_APP_PATH +
        "/api/engine/engine/default/group/count", String.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldCheckPresenceOfLibResources() {
    // given

    // when
    ResponseEntity<String> response = restClient.getForEntity(MY_APP_PATH +
        "/lib/require.js", String.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldCheckPresenceOfAppResources() {
    // given

    // when
    ResponseEntity<String> response = restClient.getForEntity(MY_APP_PATH +
        "/app/admin/styles/user-styles.css", String.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldCheckPresenceOfApiResources() {
    // given

    // when
    ResponseEntity<String> response = restClient.getForEntity(MY_APP_PATH +
        "/api/admin/plugin/adminPlugins/static/app/plugin.css", String.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

}
