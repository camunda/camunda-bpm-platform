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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.sql.SQLException;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.quarkus.engine.extension.CamundaEngineConfig;
import org.camunda.bpm.quarkus.engine.extension.QuarkusProcessEngineConfiguration;
import org.camunda.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CamundaEngineProgrammaticAndConfigFileTest {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .withConfigurationResource("org/camunda/bpm/quarkus/engine/test/config/mixed-application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

  @Inject
  CamundaEngineConfig config;

  @Inject
  ProcessEngine processEngine;

  @ApplicationScoped
  static class EngineConfigurer {

    @Produces
    public QuarkusProcessEngineConfiguration engineConfiguration() {
      QuarkusProcessEngineConfiguration engineConfig = new QuarkusProcessEngineConfiguration();
      engineConfig.setProcessEngineName("customEngine");
      engineConfig.setJobExecutorActivate(false);
      engineConfig.setHistory("full");

      return engineConfig;
    }

  }

  @Test
  public void shouldUseProvidedConfigurationAndConfigProperties() throws SQLException {
    // given
    // a .properties file with process engine and job executor configuration

    // when
    ProcessEngineConfigurationImpl configuration
        = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    JobExecutor jobExecutor = configuration.getJobExecutor();

    // then
    // assert provided configuration
    assertThat(configuration.getProcessEngineName()).isEqualTo("customEngine");
    assertThat(configuration.isJobExecutorActivate()).isFalse();
    // assert engine config properties
    assertThat(configuration.isCmmnEnabled()).isFalse();
    assertThat(configuration.isDmnEnabled()).isFalse();
    assertThat(configuration.getHistory()).isEqualTo("none");
    // assert job executor properties
    assertThat(jobExecutor.getMaxJobsPerAcquisition()).isEqualTo(5);
    assertThat(jobExecutor.getLockTimeInMillis()).isEqualTo(500000);
    assertThat(jobExecutor.getWaitTimeInMillis()).isEqualTo(7000);
    assertThat(jobExecutor.getMaxWait()).isEqualTo(65000);
    assertThat(jobExecutor.getBackoffTimeInMillis()).isEqualTo(5);
    // assert correct thread pool config
    assertThat(config.jobExecutor().threadPool().maxPoolSize()).isEqualTo(12);
    assertThat(config.jobExecutor().threadPool().queueSize()).isEqualTo(5);
    // assert correct datasource
    assertThat(config.datasource()).hasValue("camunda");
    assertThat(configuration.getDataSource().getConnection()).asString().contains("h2:mem:camunda");
  }
}