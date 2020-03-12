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
package org.camunda.bpm.spring.boot.starter.spin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;
import org.camunda.spin.DataFormats;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.camunda.spin.spi.DataFormatConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.SpringFactoriesLoader;

public class SpringBootSpinProcessEnginePlugin extends SpinProcessEnginePlugin {

  @Autowired
  protected Optional<CamundaJacksonFormatConfiguratorJSR310> dataFormatConfiguratorJsr310;

  @Autowired
  protected Optional<CamundaJacksonFormatConfiguratorParameterNames> dataFormatConfiguratorParameterNames;

  @Autowired
  protected Optional<CamundaJacksonFormatConfiguratorJdk8> dataFormatConfiguratorJdk8;

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    ClassLoader classloader = ClassLoaderUtil.getClassloader(SpringBootSpinProcessEnginePlugin.class);
    loadSpringBootDataFormats(classloader);
  }

  protected void loadSpringBootDataFormats(ClassLoader classloader) {
    List<DataFormatConfigurator> configurators = new ArrayList<>();

    // add the auto-config Jackson Java 8 module configurators
    dataFormatConfiguratorJsr310.ifPresent(configurator -> configurators.add(configurator));
    dataFormatConfiguratorParameterNames.ifPresent(configurator -> configurators.add(configurator));
    dataFormatConfiguratorJdk8.ifPresent(configurator -> configurators.add(configurator));

    // next, add any configurators defined in the spring.factories file
    configurators.addAll(SpringFactoriesLoader.loadFactories(DataFormatConfigurator.class, classloader));

    DataFormats.loadDataFormats(classloader, configurators);
  }
}
