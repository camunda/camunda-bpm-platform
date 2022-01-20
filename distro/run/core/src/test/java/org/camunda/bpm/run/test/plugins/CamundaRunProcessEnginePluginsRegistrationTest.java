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
package org.camunda.bpm.run.test.plugins;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.run.CamundaBpmRun;
import org.camunda.bpm.run.property.CamundaBpmRunProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { CamundaBpmRun.class })
@ActiveProfiles(profiles = { "test-new-plugins" }, inheritProfiles = true)
public class CamundaRunProcessEnginePluginsRegistrationTest {

  @Autowired
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Autowired
  protected CamundaBpmRunProperties properties;

  protected List<ProcessEnginePlugin> plugins;

  @Before
  public void setUp() {
    this.plugins = processEngineConfiguration.getProcessEnginePlugins();
  }

  @Test
  public void shouldPickUpAllPluginConfigurations() {
    // given a CamundaBpmRunProperties instance
    String pluginOne = "org.camunda.bpm.run.test.plugins.TestFirstPlugin";
    String pluginTwo = "org.camunda.bpm.run.test.plugins.TestSecondPlugin";

    // then
    // assert that any plugin configuration properties were mapped properly
    assertThat(properties.getProcessEnginePlugins()).hasSize(2);
    assertThat(properties.getProcessEnginePlugins().keySet())
        .contains(pluginOne, pluginTwo);

    Map<String, Object> firstPluginMap = properties.getProcessEnginePlugins().get(pluginOne);
    assertThat(firstPluginMap).hasSize(2);
    assertThat(firstPluginMap.keySet()).contains("parameterOne", "parameterTwo");
    assertThat(firstPluginMap.values()).contains("valueOne", true);

    Map<String, Object> secondPluginMap = properties.getProcessEnginePlugins().get(pluginTwo);
    assertThat(secondPluginMap).hasSize(3);
    assertThat(secondPluginMap.keySet())
        .contains("parameterOne", "parameterTwo", "parameterThree");
    assertThat(secondPluginMap.values()).contains(1.222, false, 123);
  }

  @Test
  public void shouldRegisterYamlDefinedPluginsWithProcessEngine() {
    // given a yaml config file defining process engine plugins

    // then
    // there is a single composite plugin
    assertThat(plugins).hasSize(1).hasOnlyElementsOfType(CompositeProcessEnginePlugin.class);
    CompositeProcessEnginePlugin compositePlugin =
        (CompositeProcessEnginePlugin) plugins.get(0);

    // the composite plugin contains all of the registered plugins
    List<ProcessEnginePlugin> registeredPlugins = compositePlugin.getPlugins();
    List<Class> classList = registeredPlugins.stream().map(ProcessEnginePlugin::getClass)
        .collect(Collectors.toList());
    assertThat(classList).contains(TestFirstPlugin.class, TestSecondPlugin.class);
  }

  @Test
  public void shouldInitializeRegisteredPlugins() {
    // given
    assertThat(plugins).hasSize(1).hasOnlyElementsOfType(CompositeProcessEnginePlugin.class);
    List<ProcessEnginePlugin> registeredPlugins =
        ((CompositeProcessEnginePlugin) plugins.get(0)).getPlugins();

    // then
    // the test plugins are correctly initialized
    TestFirstPlugin firstPlugin = (TestFirstPlugin) registeredPlugins.stream()
        .filter(plugin -> plugin instanceof TestFirstPlugin).findFirst().get();
    assertThat(firstPlugin.getParameterOne()).isEqualTo("valueOne");
    assertThat(firstPlugin.getParameterTwo()).isTrue();

    TestSecondPlugin secondPlugin = (TestSecondPlugin) registeredPlugins.stream()
        .filter(plugin -> plugin instanceof TestSecondPlugin).findFirst().get();
    assertThat(secondPlugin.getParameterOne()).isEqualTo(1.222);
    assertThat(secondPlugin.getParameterTwo()).isFalse();
    assertThat(secondPlugin.getParameterThree()).isEqualTo(123);
  }

}