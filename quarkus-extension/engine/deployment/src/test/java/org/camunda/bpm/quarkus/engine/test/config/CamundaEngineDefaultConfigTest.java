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

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.quarkus.engine.extension.QuarkusProcessEngineConfiguration;
import org.camunda.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE;

public class CamundaEngineDefaultConfigTest {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

  @Inject
  public ProcessEngine processEngine;

  @ApplicationScoped
  static class EngineConfigurer {

    @Produces
    public QuarkusProcessEngineConfiguration engineConfiguration() {
      return new QuarkusProcessEngineConfiguration();
    }

  }

  @Test
  public void shouldApplyDefaults() {
    // given
    // a ProcessEngineConfiguration instance
    QuarkusProcessEngineConfiguration configuration
        = (QuarkusProcessEngineConfiguration) processEngine.getProcessEngineConfiguration();

    // then
    assertThat(configuration.isJobExecutorActivate()).isTrue();

    assertThat(configuration.getJdbcUrl()).isNull();
    assertThat(configuration.getJdbcUsername()).isNull();
    assertThat(configuration.getJdbcPassword()).isNull();
    assertThat(configuration.getJdbcDriver()).isNull();
    assertThat(configuration.getDatabaseSchemaUpdate()).isEqualTo(DB_SCHEMA_UPDATE_TRUE);
    assertThat(configuration.isTransactionsExternallyManaged()).isTrue();

    assertThat(configuration.getIdGenerator()).isInstanceOf(StrongUuidGenerator.class);

    assertThat(configuration.getHistory()).isEqualTo("full");
    assertThat(configuration.getHistoryLevel()).isEqualTo(HistoryLevel.HISTORY_LEVEL_FULL);
  }

}