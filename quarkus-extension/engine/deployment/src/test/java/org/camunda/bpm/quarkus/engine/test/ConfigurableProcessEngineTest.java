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
package org.camunda.bpm.quarkus.engine.test;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.quarkus.engine.extension.QuarkusProcessEngineConfiguration;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurableProcessEngineTest {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                                    .addClass(MyConfig.class))
      .withConfigurationResource("application.properties");

  @ApplicationScoped
  static class MyConfig {

    @Produces
    public QuarkusProcessEngineConfiguration customEngineConfig() {

      QuarkusProcessEngineConfiguration engineConfig = new QuarkusProcessEngineConfiguration();
      engineConfig.setProcessEngineName("customEngine");
      engineConfig.setJobExecutorActivate(false);

      return engineConfig;
    }

  }

  @Inject
  public ProcessEngine processEngine;

  @Test
  public void shouldProvideCustomEmbeddedProcessEngine() {
    // given a custom process engine configuration

    // then
    // an embedded process engine is available
    assertThat(processEngine).isNotNull();
    // with a custom name
    assertThat(processEngine.getName()).isEqualTo("customEngine");
  }

}
