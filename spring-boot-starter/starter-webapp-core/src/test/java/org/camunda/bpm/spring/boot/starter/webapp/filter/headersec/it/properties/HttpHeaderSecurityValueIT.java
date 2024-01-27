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
package org.camunda.bpm.spring.boot.starter.webapp.filter.headersec.it.properties;

import org.camunda.bpm.spring.boot.starter.webapp.filter.util.HttpClientRule;
import org.camunda.bpm.spring.boot.starter.webapp.filter.util.FilterTestApp;
import org.junit.Before;
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
  "camunda.bpm.webapp.headerSecurity.xssProtectionValue=aValue",
  "camunda.bpm.webapp.headerSecurity.contentSecurityPolicyValue=aValue",
  "camunda.bpm.webapp.headerSecurity.contentTypeOptionsValue=aValue",
  "camunda.bpm.webapp.headerSecurity.hstsDisabled=false",
  "camunda.bpm.webapp.headerSecurity.hstsValue=aValue"
})
@DirtiesContext
public class HttpHeaderSecurityValueIT {

  @Rule
  public HttpClientRule httpClientRule;

  @LocalServerPort
  public int port;

  @Before
  public void assignRule() {
    httpClientRule = new HttpClientRule(port);
  }

  @Test
  public void shouldCheckValueOfXssProtectionHeader() {
    // given

    // when
    httpClientRule.performRequest();

    // then
    assertThat(httpClientRule.getHeader("X-XSS-Protection")).isEqualTo("aValue");
  }

  @Test
  public void shouldCheckValueOfContentSecurityPolicyHeader() {
    // given

    // when
    httpClientRule.performRequest();

    // then
    assertThat(httpClientRule.getHeader("Content-Security-Policy")).isEqualTo("aValue");
  }

  @Test
  public void shouldCheckValueOfContentTypeOptions() {
    // given

    // when
    httpClientRule.performRequest();

    // then
    assertThat(httpClientRule.getHeader("X-Content-Type-Options")).isEqualTo("aValue");
  }

  @Test
  public void shouldCheckValueOfHsts() {
    // given

    // when
    httpClientRule.performRequest();

    // then
    assertThat(httpClientRule.getHeader("Strict-Transport-Security")).isEqualTo("aValue");
  }

}
