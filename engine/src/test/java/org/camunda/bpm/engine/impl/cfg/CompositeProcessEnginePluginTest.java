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
package org.camunda.bpm.engine.impl.cfg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngine;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class CompositeProcessEnginePluginTest {

  private static final ProcessEnginePlugin PLUGIN_A = processEnginePlugin("PluginA");
  private static final ProcessEnginePlugin PLUGIN_B = processEnginePlugin("PluginB");
  private static final InOrder ORDER = inOrder(PLUGIN_A, PLUGIN_B);

  private static final ProcessEngineConfigurationImpl CONFIGURATION = mock(ProcessEngineConfigurationImpl.class);
  private static final ProcessEngine ENGINE = mock(ProcessEngine.class);

  @Test
  public void addPlugin() throws Exception {
    CompositeProcessEnginePlugin composite = new CompositeProcessEnginePlugin(PLUGIN_A);

    assertThat(composite.getPlugins()).hasSize(1);
    assertThat(composite.getPlugins().get(0)).isEqualTo(PLUGIN_A);

    composite.addProcessEnginePlugin(PLUGIN_B);
    assertThat(composite.getPlugins()).hasSize(2);
    assertThat(composite.getPlugins().get(1)).isEqualTo(PLUGIN_B);

  }

  @Test
  public void addPlugins() throws Exception {
    CompositeProcessEnginePlugin composite = new CompositeProcessEnginePlugin(PLUGIN_A);
    composite.addProcessEnginePlugins(Arrays.asList(PLUGIN_B));

    assertThat(composite.getPlugins()).hasSize(2);
    assertThat(composite.getPlugins().get(0)).isEqualTo(PLUGIN_A);
    assertThat(composite.getPlugins().get(1)).isEqualTo(PLUGIN_B);

  }

  @Test
  public void allPluginsOnPreInit() throws Exception {
    new CompositeProcessEnginePlugin(PLUGIN_A, PLUGIN_B).preInit(CONFIGURATION);

    ORDER.verify(PLUGIN_A).preInit(CONFIGURATION);
    ORDER.verify(PLUGIN_B).preInit(CONFIGURATION);
  }

  @Test
  public void allPluginsOnPostInit() throws Exception {
    new CompositeProcessEnginePlugin(PLUGIN_A, PLUGIN_B).postInit(CONFIGURATION);

    ORDER.verify(PLUGIN_A).postInit(CONFIGURATION);
    ORDER.verify(PLUGIN_B).postInit(CONFIGURATION);
  }

  @Test
  public void allPluginsOnPostProcessEngineBuild() throws Exception {
    new CompositeProcessEnginePlugin(PLUGIN_A, PLUGIN_B).postProcessEngineBuild(ENGINE);

    ORDER.verify(PLUGIN_A).postProcessEngineBuild(ENGINE);
    ORDER.verify(PLUGIN_B).postProcessEngineBuild(ENGINE);
  }

  @Test
  public void verifyToString() throws Exception {
    assertThat(new CompositeProcessEnginePlugin(PLUGIN_A, PLUGIN_B).toString()).isEqualTo("CompositeProcessEnginePlugin[PluginA, PluginB]");
  }

  private static ProcessEnginePlugin processEnginePlugin(final String name) {
    ProcessEnginePlugin plugin = Mockito.mock(ProcessEnginePlugin.class);
    when(plugin.toString()).thenReturn(name);

    return plugin;
  }
}
