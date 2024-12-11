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
package org.camunda.bpm.quarkus.engine.test.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.quarkus.engine.extension.CamundaEngineConfig;
import org.camunda.bpm.quarkus.engine.extension.QuarkusProcessEngineConfiguration;
import org.camunda.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CamundaEngineConfigurationConfigTest {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .withConfigurationResource("org/camunda/bpm/quarkus/engine/test/config/" +
                                     "process-engine-config-application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

  @Inject
  CamundaEngineConfig config;

  @Inject
  ProcessEngine processEngine;

  @Test
  public void shouldLoadProcessEngineConfigurationProperties() {
    // given a custom application.properties file

    // then
    assertThat(config.genericConfig().get("cmmn-enabled")).isEqualTo("false");
    assertThat(config.genericConfig().get("dmn-enabled")).isEqualTo("false");
    assertThat(config.genericConfig().get("history")).isEqualTo("none");
  }

  @Test
  public void shouldApplyProcessEngineConfigurationProperties() {
    // given
    // a ProcessEngineConfiguration instance
    QuarkusProcessEngineConfiguration configuration
        = (QuarkusProcessEngineConfiguration) processEngine.getProcessEngineConfiguration();

    // then
    assertThat(configuration.isCmmnEnabled()).isEqualTo(false);
    assertThat(configuration.isDmnEnabled()).isEqualTo(false);
    assertThat(configuration.getHistory()).isEqualTo("none");
  }

}