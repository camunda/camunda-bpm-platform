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
package org.camunda.bpm.spring.boot.starter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin;
import org.camunda.spin.plugin.impl.SpinObjectValueSerializer;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CamundaBpmAutoConfigurationIT {

  @Autowired
  ProcessEngineConfigurationImpl processEngineConfiguration;

  @Autowired
  private ApplicationContext appContext;

  @Test
  public void ensureProcessEngineServicesAreExposedAsBeans() {
    for (Class<?> classToCheck : getProcessEngineServicesClasses()) {
      Object bean = appContext.getBean(classToCheck);
      assertNotNull(classToCheck + " must be exposed as @Bean. Check configuration", bean);
      String beanName = convertToBeanName(classToCheck);
      assertSame(classToCheck + " must be exposed as '" + beanName + "'. Check configuration", bean, appContext.getBean(beanName));
    }

  }

  @Test
  public void ensureSpinProcessEnginePluginIsCorrectlyLoaded() {
    // given
    List<ProcessEnginePlugin> plugins = processEngineConfiguration.getProcessEnginePlugins();
    List<TypedValueSerializer<?>> serializers = processEngineConfiguration.getVariableSerializers().getSerializers();

    if (plugins.get(0) instanceof CompositeProcessEnginePlugin) {
      plugins = ((CompositeProcessEnginePlugin) plugins.get(0)).getPlugins();
    }

    boolean isJacksonJsonDataFormat = serializers.stream().anyMatch(s ->
        s instanceof SpinObjectValueSerializer
        && s.getSerializationDataformat().equals("application/json"));

    // then
    assertThat(plugins.stream().anyMatch(plugin -> plugin instanceof SpinProcessEnginePlugin)).isTrue();
    assertThat(isJacksonJsonDataFormat).isTrue();
  }

  @Test
  public void ensureConnectProcessEnginePluginIsCorrectlyLoaded() {
    // given
    List<ProcessEnginePlugin> plugins = processEngineConfiguration.getProcessEnginePlugins();

    if (plugins.get(0) instanceof CompositeProcessEnginePlugin) {
      plugins = ((CompositeProcessEnginePlugin) plugins.get(0)).getPlugins();
    }

    // then
    assertThat(plugins.stream().anyMatch(plugin -> plugin instanceof ConnectProcessEnginePlugin)).isTrue();
  }

  private String convertToBeanName(Class<?> beanClass) {
    return StringUtils.uncapitalize(beanClass.getSimpleName());
  }

  private List<Class<?>> getProcessEngineServicesClasses() {
    List<Class<?>> classes = new ArrayList<Class<?>>();
    for (Method method : ProcessEngineServices.class.getMethods()) {
      classes.add(method.getReturnType());
    }
    return classes;
  }

}
