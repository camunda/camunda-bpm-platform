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
package org.camunda.bpm.webapp.impl.security.filter.headersec;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.webapp.impl.util.HeaderRule;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.StrictTransportSecurityProvider.HEADER_NAME;

public class StrictTransportSecurityTest {

  @Rule
  public HeaderRule headerRule = new HeaderRule();

  @Test
  public void shouldConfigureDisabledByDefault() {
    // given
    headerRule.startServer("web.xml", "headersec");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getHeader(HEADER_NAME)).isNull();
  }

  @Test
  public void shouldConfigureEnabled() {
    // given
    headerRule.startServer("hsts/enabled_web.xml", "headersec");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.headerExists(HEADER_NAME)).isTrue();
  }

  @Test
  public void shouldConfigureEnabledIgnoreCase() {
    // given
    headerRule.startServer("hsts/enabled_ignore_case_web.xml", "headersec");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.headerExists(HEADER_NAME)).isTrue();
  }

  @Test
  public void shouldConfigureCustomValue() {
    // given
    headerRule.startServer("hsts/custom_value_web.xml", "headersec");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getHeader(HEADER_NAME)).isEqualTo("aCustomValue");
  }

  @Test
  public void shouldThrowExceptionWhenConfiguringCustomValueAndMaxAge() {
    // given
    headerRule.startServer("hsts/max_age_and_value_web.xml", "headersec");

    // when
    headerRule.performRequest();

    Throwable expectedException = headerRule.getException();

    // then
    assertThat(expectedException)
      .isInstanceOf(ProcessEngineException.class)
      .hasMessage("StrictTransportSecurityProvider: cannot set hstsValue " +
        "in conjunction with hstsMaxAge or hstsIncludeSubdomainsDisabled.");
  }

  @Test
  public void shouldConfigureIncludeSubdomains() {
    // given
    headerRule.startServer("hsts/include_subdomains_web.xml", "headersec");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getHeader(HEADER_NAME)).isEqualTo("max-age=31536000; includeSubDomains");
  }

  @Test
  public void shouldConfigureIncludeSubdomainsAndMaxAge() {
    // given
    headerRule.startServer("hsts/include_subdomains_max_age_web.xml", "headersec");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getHeader(HEADER_NAME)).isEqualTo("max-age=47; includeSubDomains");
  }

  @Test
  public void shouldConfigureMaxAge() {
    // given
    headerRule.startServer("hsts/max_age_web.xml", "headersec");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getHeader(HEADER_NAME)).isEqualTo("max-age=47");
  }

}
