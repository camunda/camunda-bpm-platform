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
package org.camunda.bpm.spring.boot.starter.webapp.filter.headersec.it;

import org.camunda.bpm.spring.boot.starter.webapp.filter.util.HttpClientRule;
import org.camunda.bpm.spring.boot.starter.webapp.filter.util.FilterTestApp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.ContentSecurityPolicyProvider.HEADER_DEFAULT_VALUE;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.ContentSecurityPolicyProvider.HEADER_NAME;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.ContentSecurityPolicyProvider.HEADER_NONCE_PLACEHOLDER;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { FilterTestApp.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HttpHeaderSecurityDefaultsIT {

  @Rule
  public HttpClientRule httpClientRule;

  @LocalServerPort
  public int port;

  @Before
  public void assignRule() {
    httpClientRule = new HttpClientRule(port);
  }

  @Test
  public void shouldCheckDefaultOfXssProtectionHeader() {
    // given

    // when
    httpClientRule.performRequest();

    // then
    assertThat(httpClientRule.getHeader("X-XSS-Protection")).isEqualTo("1; mode=block");
  }

  @Test
  public void shouldCheckDefaultOfContentSecurityPolicyHeader() {
    // given

    // when
    httpClientRule.performRequest();

    // then
    String expectedHeaderPattern = HEADER_DEFAULT_VALUE.replace(HEADER_NONCE_PLACEHOLDER, "'nonce-([-_a-zA-Z\\d]*)'");
    assertThat(httpClientRule.getHeader(HEADER_NAME)).matches(expectedHeaderPattern);
  }

  @Test
  public void shouldCheckDefaultOfContentTypeOptions() {
    // given

    // when
    httpClientRule.performRequest();

    // then
    assertThat(httpClientRule.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
  }

}
