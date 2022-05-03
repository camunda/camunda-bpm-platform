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

import org.camunda.bpm.webapp.impl.util.HeaderRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.ContentSecurityPolicyProvider.HEADER_DEFAULT_VALUE;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.ContentSecurityPolicyProvider.HEADER_NAME;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.ContentSecurityPolicyProvider.HEADER_NONCE_PLACEHOLDER;

public class ContentSecurityPolicyTest {

  public static final Pattern NONCE_PATTERN = Pattern.compile("'nonce-([a-zA-Z\\d]*)'");

  @Rule
  public HeaderRule headerRule = new HeaderRule();

  @Test
  public void shouldConfigureEnabledByDefault() {
    // given
    headerRule.startServer("web.xml", "headersec");

    // when
    headerRule.performRequest();

    // then
    final String[] actualCSPHeader = headerRule.getHeader(HEADER_NAME).split("[ ;]");
    final String[] expectedCSPHeader = HEADER_DEFAULT_VALUE.split("[ ;]");
    assertThat(actualCSPHeader.length).isEqualTo(expectedCSPHeader.length);
    for (int i = 0; i < actualCSPHeader.length; i++) {
      final String expected = expectedCSPHeader[i];
      final String actual = actualCSPHeader[i];

      if (expected.equals(HEADER_NONCE_PLACEHOLDER)) {
        final Matcher matcher = NONCE_PATTERN.matcher(actual);
        assertThat(matcher.matches()).isTrue();
        assertThat(matcher.group(1).length()).isGreaterThanOrEqualTo(8);
      } else {
        assertThat(actual).isEqualTo(expected);
      }
    }
  }

  @Test
  public void shouldConfigureDisabled() {
    // given
    headerRule.startServer("csp/disabled_web.xml", "headersec");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.headerExists(HEADER_NAME)).isFalse();
  }

  @Test
  public void shouldConfigureDisabledIgnoreCase() {
    // given
    headerRule.startServer("csp/disabled_ignore_case_web.xml", "headersec");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.headerExists(HEADER_NAME)).isFalse();
  }

  @Test
  public void shouldConfigureCustomValue() {
    // given
    headerRule.startServer("csp/custom_value_web.xml", "headersec");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getHeader(HEADER_NAME))
      .isEqualTo("base-uri 'self'; default-src 'self' 'unsafe-inline'; img-src 'self' data:; block-all-mixed-content");
  }

}
