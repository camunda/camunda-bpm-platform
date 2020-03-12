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
package org.camunda.bpm.spring.boot.starter.property.csrf;

import org.camunda.bpm.spring.boot.starter.property.CsrfProperties;
import org.camunda.bpm.spring.boot.starter.property.ParsePropertiesHelper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultCookieTest extends ParsePropertiesHelper {

  @Test
  public void shouldCheckSecureCookie() {
    // given

    // when
    CsrfProperties properties = webapp.getCsrf();

    // then
    assertThat(properties.isEnableSecureCookie()).isFalse();
    assertThat(properties.getInitParams()).doesNotContainKey("enableSecureCookie");
  }

  @Test
  public void shouldCheckSameSiteCookie() {
    // given

    // when
    CsrfProperties properties = webapp.getCsrf();

    // then
    assertThat(properties.isEnableSameSiteCookie()).isTrue();
    assertThat(properties.getInitParams()).doesNotContainKey("enableSecureCookie");
  }

  @Test
  public void shouldCheckSameSiteCookieOption() {
    // given

    // when
    CsrfProperties properties = webapp.getCsrf();

    // then
    assertThat(properties.getSameSiteCookieOption()).isNull();
    assertThat(properties.getInitParams()).doesNotContainKey("sameSiteCookieOption");
  }

  @Test
  public void shouldCheckSameSiteCookieValue() {
    // given

    // when
    CsrfProperties properties = webapp.getCsrf();

    // then
    assertThat(properties.getSameSiteCookieValue()).isNull();
    assertThat(properties.getInitParams()).doesNotContainKey("sameSiteCookieValue");
  }

  @Test
  public void shouldCheckDefaultCookieName() {
    // given

    // when
    CsrfProperties properties = webapp.getCsrf();

    // then
    assertThat(properties.getCookieName()).isNull();
    assertThat(properties.getInitParams()).doesNotContainKey("cookieName");
  }

}
