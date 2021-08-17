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

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.quarkus.engine.extension.impl.CamundaEngineConfig;
import org.camunda.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CamundaEngineConfigTest {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .withConfigurationResource("job-executor-application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

  @Inject
  CamundaEngineConfig config;

  @Test
  public void shouldLoadJobExecutorThreadPoolProperties() {
    // given a custom application.properties file

    // then
    assertThat(config.jobExecutor.threadPool.maxPoolSize).isEqualTo(12);
    assertThat(config.jobExecutor.threadPool.queueSize).isEqualTo(5);
  }

  @Test
  public void shouldLoadJobAcquisitionProperties() {
    // given a custom application.properties file

    // then
    assertThat(config.jobExecutor.maxJobsPerAcquisition).isEqualTo(5);
    assertThat(config.jobExecutor.lockTimeInMillis).isEqualTo(500000);
    assertThat(config.jobExecutor.waitTimeInMillis).isEqualTo(7000);
    assertThat(config.jobExecutor.maxWait).isEqualTo(65000);
    assertThat(config.jobExecutor.backoffTimeInMillis).isEqualTo(5);
    assertThat(config.jobExecutor.maxBackoff).isEqualTo(5);
    assertThat(config.jobExecutor.backoffDecreaseThreshold).isEqualTo(120);
    assertThat(config.jobExecutor.waitIncreaseFactor).isEqualTo(3);
  }
}
