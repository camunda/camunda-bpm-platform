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

import org.camunda.bpm.spring.boot.starter.webapp.WebappTestApp;
import org.camunda.bpm.spring.boot.starter.webapp.filter.util.HttpClientRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.ContentSecurityPolicyProvider.HEADER_DEFAULT_VALUE;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.ContentSecurityPolicyProvider.HEADER_NAME;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.ContentSecurityPolicyProvider.HEADER_NONCE_PLACEHOLDER;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = { WebappTestApp.class },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "camunda.bpm.webapp.applicationPath=" + ChangedAppPathIT.MY_APP_PATH
})
public class ChangedAppPathIT {

  protected static final String MY_APP_PATH = "/my/application/path";

  @Rule
  public HttpClientRule httpClientRule = new HttpClientRule();

  @LocalServerPort
  public int port;

  @Autowired
  protected TestRestTemplate restClient;

  @Test
  public void shouldCheckPresenceOfCsrfPreventionFilter() {
    // given

    // when
    httpClientRule.performRequest("http://localhost:" + port + MY_APP_PATH +
        "/app/tasklist/default");

    // then
    String xsrfCookieValue = httpClientRule.getXsrfCookie();
    String xsrfTokenHeader = httpClientRule.getXsrfTokenHeader();

    assertThat(xsrfCookieValue).matches("XSRF-TOKEN=[A-Z0-9]{32};" +
        "Path=" + MY_APP_PATH + ";SameSite=Lax");
    assertThat(xsrfTokenHeader).matches("[A-Z0-9]{32}");

    assertThat(xsrfCookieValue).contains(xsrfTokenHeader);
  }

  @Test
  public void shouldCheckPresenceOfRedirection() {
    // given

    // when
    httpClientRule.performRequest("http://localhost:" + port + "/");

    // then
    assertThat(httpClientRule.getHeader("Location")).isEqualTo("http://localhost:" + port +
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
        .get(HEADER_NAME);

    String expectedHeaderPattern = HEADER_DEFAULT_VALUE.replace(HEADER_NONCE_PLACEHOLDER, "'nonce-([-_a-zA-Z\\d]*)'");
    assertThat(contentSecurityPolicyHeaders).anyMatch(val -> val.matches(expectedHeaderPattern));
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
        "/lib/deps.js", String.class);

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
