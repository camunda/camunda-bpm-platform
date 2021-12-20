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
package org.camunda.spin.plugin.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

public class SpinProcessEnginePluginConfigurationTest {

  @Rule
  public ProcessEngineBootstrapRule bootstrapRule
      = new ProcessEngineBootstrapRule("custom.camunda.cfg.xml");

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Test
  public void shouldSetCustomSpinPluginProperties() {

    // when
    List<ProcessEnginePlugin> pluginList =
        engineRule.getProcessEngineConfiguration().getProcessEnginePlugins();

    // then
    assertThat(pluginList).hasOnlyElementsOfType(SpinProcessEnginePlugin.class).hasSize(1);
    SpinProcessEnginePlugin spinProcessEnginePlugin = (SpinProcessEnginePlugin) pluginList.get(0);
    assertThat(spinProcessEnginePlugin.isEnableXxeProcessing()).isTrue();
    assertThat(spinProcessEnginePlugin.isEnableSecureXmlProcessing()).isFalse();

  }

}