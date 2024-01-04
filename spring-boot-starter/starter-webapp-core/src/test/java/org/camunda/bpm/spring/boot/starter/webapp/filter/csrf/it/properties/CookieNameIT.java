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
package org.camunda.bpm.spring.boot.starter.webapp.filter.csrf.it.properties;

import org.camunda.bpm.spring.boot.starter.property.WebappProperty;
import org.camunda.bpm.spring.boot.starter.webapp.filter.util.HttpClientRule;
import org.camunda.bpm.spring.boot.starter.webapp.filter.util.FilterTestApp;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { FilterTestApp.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
  "camunda.bpm.webapp.csrf.cookieName=myFancyCookieName"
})
@DirtiesContext
public class CookieNameIT {

  @Rule
  public HttpClientRule httpClientRule = new HttpClientRule();

  @LocalServerPort
  public int port;

  @Test
  public void shouldChangeCookieName() {
    // given

    // when
    httpClientRule.performRequest("http://localhost:" + port + "/camunda/app/tasklist/default");

    String xsrfCookieValue = httpClientRule.getCookie("myFancyCookieName");
    String xsrfTokenHeader = httpClientRule.getXsrfTokenHeader();

    // then
    assertThat(xsrfCookieValue).matches("myFancyCookieName=[A-Z0-9]{32};" +
        "Path=" + WebappProperty.DEFAULT_APP_PATH + ";SameSite=Lax");
    assertThat(xsrfTokenHeader).matches("[A-Z0-9]{32}");

    assertThat(xsrfCookieValue).contains(xsrfTokenHeader);
  }

}
