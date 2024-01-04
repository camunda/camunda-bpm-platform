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
package org.camunda.bpm.spring.boot.starter.webapp.filter.csrf.it;

import org.camunda.bpm.spring.boot.starter.property.WebappProperty;
import org.camunda.bpm.spring.boot.starter.webapp.filter.util.HttpClientRule;
import org.camunda.bpm.spring.boot.starter.webapp.filter.util.FilterTestApp;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URLConnection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { FilterTestApp.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"server.error.include-message=always"})
@DirtiesContext
public class CsrfPreventionIT {

  @Rule
  public HttpClientRule httpClientRule = new HttpClientRule();

  @LocalServerPort
  public int port;

  @Test
  public void shouldSetCookieWebapp() {
    httpClientRule.performRequest("http://localhost:" + port + "/camunda/app/tasklist/default");

    String xsrfCookieValue = httpClientRule.getXsrfCookie();
    String xsrfTokenHeader = httpClientRule.getXsrfTokenHeader();

    assertThat(xsrfCookieValue).matches("XSRF-TOKEN=[A-Z0-9]{32};" +
        "Path=" + WebappProperty.DEFAULT_APP_PATH + ";SameSite=Lax");
    assertThat(xsrfTokenHeader).matches("[A-Z0-9]{32}");

    assertThat(xsrfCookieValue).contains(xsrfTokenHeader);
  }

  @Test
  public void shouldSetCookieWebappRest() {
    httpClientRule.performRequest("http://localhost:" + port + "/camunda/api/engine/engine/");

    String xsrfCookieValue = httpClientRule.getXsrfCookie();
    String xsrfTokenHeader = httpClientRule.getXsrfTokenHeader();

    assertThat(xsrfCookieValue).matches("XSRF-TOKEN=[A-Z0-9]{32};" +
        "Path=" + WebappProperty.DEFAULT_APP_PATH + ";SameSite=Lax");
    assertThat(xsrfTokenHeader).matches("[A-Z0-9]{32}");

    assertThat(xsrfCookieValue).contains(xsrfTokenHeader);
  }

  @Test
  public void shouldRejectModifyingRequest() {
    // given

    // when
    URLConnection urlConnection = httpClientRule.performPostRequest("http://localhost:" + port +
            "/camunda/api/admin/auth/user/default/login/welcome", "Content-Type",
        "application/x-www-form-urlencoded");

    try {
      urlConnection.getContent();
      fail("Exception expected!");
    } catch (IOException e) {
      // then
      assertThat(e).hasMessageContaining("Server returned HTTP response code: 403 for URL");
      assertThat(httpClientRule.getHeaderXsrfToken()).isEqualTo("Required");
      assertThat(httpClientRule.getErrorResponseContent()).contains("CSRFPreventionFilter: Token provided via HTTP Header is absent/empty.");
    }

  }

}
