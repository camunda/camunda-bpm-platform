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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { FilterTestApp.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "camunda.bpm.webapp.headerSecurity.hstsDisabled=false",
    "camunda.bpm.webapp.headerSecurity.hstsMaxAge=8",
    "camunda.bpm.webapp.headerSecurity.hstsIncludeSubdomainsDisabled=false"
})
public class HstsIT {

  @Rule
  public HttpClientRule httpClientRule;

  @LocalServerPort
  public int port;

  @Before
  public void assignRule() {
    httpClientRule = new HttpClientRule(port);
  }

  @Test
  public void shouldConfigureHsts() {
    // given

    // when
    httpClientRule.performRequest();

    // then
    assertThat(httpClientRule.getHeader("Strict-Transport-Security"))
        .isEqualTo("max-age=8; includeSubDomains");
  }

}
