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
package org.camunda.bpm.dmn.engine.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngine;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.junit.Rule;
import org.junit.Test;

public class DmnEngineRuleTest {

  @Rule
  public DmnEngineRule engineRule = new DmnEngineRule();

  @Rule
  public DmnEngineRule nullEngineRule = new DmnEngineRule(null);

  @Rule
  public DmnEngineRule customEngineRule = new DmnEngineRule(initConfiguration());

  public DmnEngineConfiguration customConfiguration;

  public DmnEngineConfiguration initConfiguration() {
    customConfiguration = new DefaultDmnEngineConfiguration();
    return customConfiguration;
  }

  @Test
  public void shouldCreateDefaultDmnEngineWithoutConfiguration() {
    DmnEngine dmnEngine = engineRule.getDmnEngine();
    assertThat(dmnEngine)
      .isInstanceOf(DefaultDmnEngine.class)
      .isNotNull();
  }

  @Test
  public void shouldCreateDefaultDmnEngineWithNullConfiguration() {
    DmnEngine dmnEngine = nullEngineRule.getDmnEngine();
    assertThat(dmnEngine)
      .isInstanceOf(DefaultDmnEngine.class)
      .isNotNull();
  }

  @Test
  public void shouldCreateEngineFromCustomConfiguration() {
    DmnEngine dmnEngine = customEngineRule.getDmnEngine();
    assertThat(dmnEngine)
      .isNotNull();

    assertThat(dmnEngine.getConfiguration())
      .isEqualTo(customConfiguration);
  }

}
