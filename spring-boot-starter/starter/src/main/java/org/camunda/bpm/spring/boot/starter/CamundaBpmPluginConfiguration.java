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

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaBpmPluginConfiguration {

  @ConditionalOnClass(SpinProcessEnginePlugin.class)
  @Configuration
  static class SpinConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "spinProcessEnginePlugin")
    public static ProcessEnginePlugin spinProcessEnginePlugin() {
      return new SpinProcessEnginePlugin();
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
