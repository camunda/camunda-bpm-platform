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
package org.camunda.bpm.spring.boot.starter.property.headersec;

import org.camunda.bpm.spring.boot.starter.property.HeaderSecurityProperties;
import org.camunda.bpm.spring.boot.starter.property.ParsePropertiesHelper;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
  "camunda.bpm.webapp.headerSecurity.xssProtectionValue=aValue",
  "camunda.bpm.webapp.headerSecurity.contentSecurityPolicyValue=aValue",
  "camunda.bpm.webapp.headerSecurity.contentTypeOptionsValue=aValue",
  "camunda.bpm.webapp.headerSecurity.hstsValue=aValue"
})
public class HttpHeaderSecurityValueTest extends ParsePropertiesHelper {

  @Test
  public void shouldCheckXssProtectionValue() {
    HeaderSecurityProperties properties = webapp.getHeaderSecurity();

    assertThat(properties.getXssProtectionValue()).isEqualTo("aValue");
    assertThat(properties.getInitParams()).containsEntry("xssProtectionValue", "aValue");
  }

  @Test
  public void shouldCheckContentSecurityPolicyValue() {
    HeaderSecurityProperties properties = webapp.getHeaderSecurity();

    assertThat(properties.getContentSecurityPolicyValue()).isEqualTo("aValue");
    assertThat(properties.getInitParams()).containsEntry("contentSecurityPolicyValue", "aValue");
  }

  @Test
  public void shouldCheckContentTypeOptionsValue() {
    HeaderSecurityProperties properties = webapp.getHeaderSecurity();

    assertThat(properties.getContentTypeOptionsValue()).isEqualTo("aValue");
    assertThat(properties.getInitParams()).containsEntry("contentTypeOptionsValue", "aValue");
  }

  @Test
  public void shouldCheckHstsValue() {
    // given
    
    // when
    HeaderSecurityProperties properties = webapp.getHeaderSecurity();

    // then
    assertThat(properties.getHstsValue()).isEqualTo("aValue");
    assertThat(properties.getInitParams()).containsEntry("hstsValue", "aValue");
  }

}
