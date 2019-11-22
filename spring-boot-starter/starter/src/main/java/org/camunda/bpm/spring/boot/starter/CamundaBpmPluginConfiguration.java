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

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.spring.boot.starter.spin.CamundaJacksonFormatConfiguratorJSR310;
import org.camunda.bpm.spring.boot.starter.spin.CamundaJacksonFormatConfiguratorJdk8;
import org.camunda.bpm.spring.boot.starter.spin.CamundaJacksonFormatConfiguratorParameterNames;
import org.camunda.bpm.spring.boot.starter.spin.SpringBootSpinProcessEnginePlugin;
import org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaBpmPluginConfiguration {

  /*
     When `camunda-spin-dataformat-all` is used as a dependency,
     SpinDataFormatConfigurationJSR310, SpinDataFormatConfigurationParameterNames
     and SpinDataFormatConfigurationJdk8 are not used. The `camunda-spin-dataformat-all`
     artifact comes with a shaded Jackson ObjectMapper (prefixed with `spinjar`),
     which breaks auto-configuration for Jackson Java 8 modules.
  */

  @ConditionalOnClass({JacksonJsonDataFormat.class, JavaTimeModule.class})
  @ConditionalOnMissingClass("spinjar.com.fasterxml.jackson.databind.ObjectMapper")
  @Configuration
  static class SpinDataFormatConfigurationJSR310 {

    @Bean
    @ConditionalOnMissingBean(name = "spinDataFormatConfiguratorJSR310")
    public static CamundaJacksonFormatConfiguratorJSR310 spinDataFormatConfiguratorJSR310() {
      return new CamundaJacksonFormatConfiguratorJSR310();
    }

  }

  @ConditionalOnClass({JacksonJsonDataFormat.class, ParameterNamesModule.class})
  @ConditionalOnMissingClass("spinjar.com.fasterxml.jackson.databind.ObjectMapper")
  @Configuration
  static class SpinDataFormatConfigurationParameterNames {

    @Bean
    @ConditionalOnMissingBean(name = "spinDataFormatConfiguratorParameterNames")
    public static CamundaJacksonFormatConfiguratorParameterNames spinDataFormatConfiguratorParameterNames() {
      return new CamundaJacksonFormatConfiguratorParameterNames();
    }

  }

  @ConditionalOnClass({JacksonJsonDataFormat.class, Jdk8Module.class})
  @ConditionalOnMissingClass("spinjar.com.fasterxml.jackson.databind.ObjectMapper")
  @Configuration
  static class SpinDataFormatConfigurationJdk8 {

    @Bean
    @ConditionalOnMissingBean(name = "spinDataFormatConfiguratorJdk8")
    public static CamundaJacksonFormatConfiguratorJdk8 spinDataFormatConfiguratorJdk8() {
      return new CamundaJacksonFormatConfiguratorJdk8();
    }

  }

  @ConditionalOnClass(SpinProcessEnginePlugin.class)
  @Configuration
  static class SpinConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "spinProcessEnginePlugin")
    public static ProcessEnginePlugin spinProcessEnginePlugin() {
      return new SpringBootSpinProcessEnginePlugin();
    }

  }

  @ConditionalOnClass(ConnectProcessEnginePlugin.class)
  @Configuration
  static class ConnectConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "connectProcessEnginePlugin")
    public static ProcessEnginePlugin connectProcessEnginePlugin() {
      return new ConnectProcessEnginePlugin();
    }
  }
}
