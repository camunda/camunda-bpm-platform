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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@TestConfiguration
public class AdditionalCammundaBpmConfigurations {

  @Bean
  public ProcessEnginePlugin beforeStandardConfiguration() {
    return new BeforeStandardConfiguration();
  }

  @Bean
  public ProcessEnginePlugin afterStandardConfiguration() {
    return new AfterStandardConfiguration();
  }

  @Order(Ordering.DEFAULT_ORDER - 1)
  public static class BeforeStandardConfiguration extends AbstractProcessEnginePlugin {

    static boolean PROCESSED = false;

    @Override
    public void preInit(ProcessEngineConfigurationImpl configuration) {
      assertNull(configuration.getDataSource());
      PROCESSED = true;
    }
  }

  @Order(Ordering.DEFAULT_ORDER + 1)
  public static class AfterStandardConfiguration extends  AbstractProcessEnginePlugin {

    static boolean PROCESSED = false;

    @Override
    public void preInit(ProcessEngineConfigurationImpl configuration) {
      assertNotNull(configuration.getDataSource());
      PROCESSED = true;
    }
  }
}
