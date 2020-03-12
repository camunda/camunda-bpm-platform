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
package org.camunda.bpm.spring.boot.starter.configuration.id;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class PrefixedUuidGeneratorTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void prefixed_uuid() throws Exception {

    final String id = new PrefixedUuidGenerator("foo").getNextId();
    assertThat(id).startsWith("foo-");
    assertThat(id.split("-")).hasSize(6);
  }

  @Test
  public void fails_on_null() throws Exception {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("spring.application.name");

    new PrefixedUuidGenerator(null);
  }
}
