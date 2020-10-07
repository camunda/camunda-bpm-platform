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

import java.util.Set;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.telemetry.CamundaIntegration;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServer;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = {TestApplication.class},
  webEnvironment = WebEnvironment.RANDOM_PORT
)
public class TelemetryNonPaIT extends AbstractCamundaAutoConfigurationIT {

  @Test
  public void shouldSubmitApplicationServerData() {
    TelemetryRegistry telemetryRegistry = processEngine.getProcessEngineConfiguration().getTelemetryRegistry();

    // then
    ApplicationServer applicationServer = telemetryRegistry.getApplicationServer();
    assertThat(applicationServer).isNotNull();
    assertThat(applicationServer.getVendor()).isEqualTo("Apache Tomcat");
    assertThat(applicationServer.getVersion()).isNotNull();
  }

  @Test
  public void shouldAddCamundaIntegration() {
    // given default configuration
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    // then
    Data telemetryData = processEngineConfiguration.getTelemetryData();
    Set<String> camundaIntegration = telemetryData.getProduct().getInternals().getCamundaIntegration();
    assertThat(camundaIntegration.size()).isOne();
    assertThat(camundaIntegration).containsExactly(CamundaIntegration.SPRING_BOOT_STARTER);
  }

}
