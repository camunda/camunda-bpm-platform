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
package org.camunda.commons.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.MDC;

public class MdcAccessTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    MDC.clear();
  }

  @After
  public void tearDown() {
    MDC.clear();
  }

  @Test
  public void shouldPutValueToMdc() {
    // when
    MdcAccess.put("foo", "bar");
    // then
    assertThat(MDC.getCopyOfContextMap()).hasSize(1);
    assertThat(MDC.get("foo")).isEqualTo("bar");
  }

  @Test
  public void shouldPutNullValueToMdc() {
    // given
    MDC.put("foo", "bar");
    // when
    MdcAccess.put("foo", null);
    // then
    assertThat(MDC.get("foo")).isEqualTo(null);
  }

  @Test
  public void shouldNotPutNullKeyToMdc() {
    // then
    thrown.expect(IllegalArgumentException.class);
    // when
    MdcAccess.put(null, "bar");
  }

  @Test
  public void shouldGetValueFromMdc() {
    // given
    MDC.put("foo", "bar");
    // when
    String value = MdcAccess.get("foo");
    // then
    assertThat(value).isEqualTo("bar");
  }

  @Test
  public void shouldNotGetNullKeyFromMdc() {
    // then
    thrown.expect(IllegalArgumentException.class);
    // when
    MdcAccess.get(null);
  }

  @Test
  public void shouldRemoveFromMdc() {
    // given
    MDC.put("foo", "bar");
    MDC.put("baz", "fooz");
    // when
    MdcAccess.remove("foo");
    // then
    assertThat(MDC.getCopyOfContextMap()).hasSize(1);
    assertThat(MDC.get("baz")).isEqualTo("fooz");
  }

  @Test
  public void shouldNotRemoveNullKeyFromMdc() {
    // then
    thrown.expect(IllegalArgumentException.class);
    // when
    MdcAccess.remove(null);
  }

}
