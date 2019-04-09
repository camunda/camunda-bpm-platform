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
import org.camunda.bpm.webapp.impl.security.filter.headersec.util.HeaderSecRule;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.XssProtectionOption.BLOCK;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.XssProtectionOption.SANITIZE;
import static org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.XssProtectionProvider.HEADER_NAME;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Tassilo Weidner
 */
public class XssProtectionTest {

  @Rule
  public HeaderSecRule headerSecRule = new HeaderSecRule();

  @Test
  public void shouldConfigureEnabledByDefault() {
    // given
    headerSecRule.startServer("web.xml");

    // when
    headerSecRule.performRequest();

    // then
    assertThat(headerSecRule.getHeader(HEADER_NAME), is(BLOCK.getHeaderValue()));
  }

  @Test
  public void shouldConfigureDisabled() {
    // given
    headerSecRule.startServer("xss/disabled_web.xml");

    // when
    headerSecRule.performRequest();

    // then
    assertThat(headerSecRule.headerExists(HEADER_NAME), is(false));
  }

  @Test
  public void shouldConfigureDisabledIgnoreCase() {
    // given
    headerSecRule.startServer("xss/disabled_ignore_case_web.xml");

    // when
    headerSecRule.performRequest();

    // then
    assertThat(headerSecRule.headerExists(HEADER_NAME), is(false));
  }

  @Test
  public void shouldConfigureCustomValue() {
    // given
    headerSecRule.startServer("xss/custom_value_web.xml");

    // when
    headerSecRule.performRequest();

    // then
    assertThat(headerSecRule.getHeader(HEADER_NAME), is("aCustomValue"));
  }

  @Test
  public void shouldConfigureOptionSanitize() {
    // given
    headerSecRule.startServer("xss/option_sanitize_web.xml");

    // when
    headerSecRule.performRequest();

    // then
    assertThat(headerSecRule.getHeader(HEADER_NAME), is(SANITIZE.getHeaderValue()));
  }

  @Test
  public void shouldConfigureOptionSanitizeIgnoreCase() {
    // given
    headerSecRule.startServer("xss/option_sanitize_ignore_case_web.xml");

    // when
    headerSecRule.performRequest();

    // then
    assertThat(headerSecRule.getHeader(HEADER_NAME), is(SANITIZE.getHeaderValue()));
  }

  @Test
  public void shouldConfigureOptionBlock() {
    // given
    headerSecRule.startServer("xss/option_block_web.xml");

    // when
    headerSecRule.performRequest();

    // then
    assertThat(headerSecRule.getHeader(HEADER_NAME), is(BLOCK.getHeaderValue()));
  }

  @Test
  public void shouldThrowExceptionOnSetBothParamsOptionAndValue() {
    // given
    headerSecRule.startServer("xss/option_and_value_web.xml");

    // when
    headerSecRule.performRequest();

    Throwable expectedException = headerSecRule.getException();

    // then
    assertThat(expectedException, instanceOf(ProcessEngineException.class));
    assertThat(expectedException.getMessage(), is("XssProtectionProvider: cannot set both xssProtectionValue and xssProtectionOption."));
  }

  @Test
  public void shouldThrowExceptionOnNonExistingOption() {
    // given
    headerSecRule.startServer("xss/option_non_existing_web.xml");

    Throwable expectedException = headerSecRule.getException();

    // then
    assertThat(expectedException, instanceOf(ProcessEngineException.class));
    assertThat(expectedException.getMessage(), is("XssProtectionProvider: cannot set non-existing option foo for xssProtectionOption."));
  }

}
