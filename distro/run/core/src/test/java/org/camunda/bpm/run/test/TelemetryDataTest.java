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
package org.camunda.bpm.run.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.telemetry.CamundaIntegration;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.run.CamundaBpmRun;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { CamundaBpmRun.class }, webEnvironment = WebEnvironment.DEFINED_PORT)
public class TelemetryDataTest {

  @Autowired
  ProcessEngine engine;

  @Test
  public void shouldAddCamundaIntegration() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();

    // then
    Data telemetryData = processEngineConfiguration.getTelemetryData();
    Map<String, Object> camundaIntegration = telemetryData.getProduct().getInternals().getCamundaIntegration();
    assertThat(camundaIntegration.size()).isEqualTo(2);
    assertThat((boolean) camundaIntegration.get(CamundaIntegration.CAMUNDA_BPM_RUN)).isTrue();
    assertThat((boolean) camundaIntegration.get(CamundaIntegration.SPRING_BOOT_STARTER)).isTrue();
  }
}
