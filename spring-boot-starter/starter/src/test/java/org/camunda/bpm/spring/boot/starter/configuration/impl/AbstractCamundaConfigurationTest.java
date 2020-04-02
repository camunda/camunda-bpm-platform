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
package org.camunda.bpm.spring.boot.starter.configuration.impl;

import org.junit.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil.join;

public class AbstractCamundaConfigurationTest {

  @Test
  public void joinLists() {
    assertThat(join(asList("a"), asList("b"))).containsExactly("a", "b");
    assertThat(join(null, asList("b"))).containsExactly("b");
    assertThat(join(new ArrayList<String>(), asList("b"))).containsExactly("b");
    assertThat(join(asList("a"), null)).containsExactly("a");
    assertThat(join(asList("a"), new ArrayList<String>())).containsExactly("a");
    assertThat(join(null, null)).isEmpty();
  }

}
