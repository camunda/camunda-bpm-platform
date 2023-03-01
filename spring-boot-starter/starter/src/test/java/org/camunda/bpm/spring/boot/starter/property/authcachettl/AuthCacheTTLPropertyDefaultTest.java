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
package org.camunda.bpm.spring.boot.starter.property.authcachettl;

import org.camunda.bpm.spring.boot.starter.property.ParsePropertiesHelper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthCacheTTLPropertyDefaultTest extends ParsePropertiesHelper {

  @Test
  public void shouldDefaultToEnabledAndTTLFiveMinutes() {
    // given

    // when
    boolean enabled = webapp.getAuth().getCache().isTtlEnabled();
    long ttl = webapp.getAuth().getCache().getTimeToLive();

    // then
    assertThat(enabled).isTrue();
    assertThat(ttl).isEqualTo(1_000 * 60 * 5);
  }

}
