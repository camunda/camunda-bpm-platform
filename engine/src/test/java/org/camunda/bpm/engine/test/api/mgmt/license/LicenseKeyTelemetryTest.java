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
package org.camunda.bpm.engine.test.api.mgmt.license;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class LicenseKeyTelemetryTest {

  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(testRule).around(engineRule);

  ProcessEngine processEngine;
  ProcessEngineConfigurationImpl processEngineConfiguration;
  ManagementService managementService;
  TelemetryRegistry telemetryRegistry;

  @Before
  public void init() {
    processEngine = engineRule.getProcessEngine();
    processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    managementService = processEngine.getManagementService();
    telemetryRegistry = processEngineConfiguration.getTelemetryRegistry();
  }

  @After
  public void tearDown() {
    managementService.deleteLicenseKey();
    telemetryRegistry.clear();
  }

  @Test
  public void shouldSetLicenseKeyInTelemetryRegistry() {
    // given
    String licenseKey = "testLicenseKey";

    // when
    managementService.setLicenseKey(licenseKey);

    // then
    assertThat(telemetryRegistry.getLicenseKey().getRaw()).isEqualTo(licenseKey);
  }

  @Test
  public void shouldNotOverrideSameLicenseKeyInTelemetryRegistry() {
    // given
    String licenseKey = "testLicenseKey";
    LicenseKeyDataImpl licenseKeyData = new LicenseKeyDataImpl("customer", null, null, null, null, licenseKey);
    telemetryRegistry.setLicenseKey(licenseKeyData);

    // when
    managementService.setLicenseKey(licenseKey);

    // then
    assertThat(telemetryRegistry.getLicenseKey()).isEqualTo(licenseKeyData);
  }

  @Test
  public void shouldNotOverrideSameMultipartLicenseKeyInTelemetryRegistry() {
    // given
    String licenseKey = "signature;testLicenseKey;more;data";
    LicenseKeyDataImpl licenseKeyData = new LicenseKeyDataImpl("customer", null, null, null, null, "testLicenseKey;more;data");
    telemetryRegistry.setLicenseKey(licenseKeyData);

    // when
    managementService.setLicenseKey(licenseKey);

    // then
    assertThat(telemetryRegistry.getLicenseKey()).isEqualTo(licenseKeyData);
  }

  @Test
  public void shouldRemoveLicenseKeyFromTelemetryRegistry() {
    // given
    LicenseKeyDataImpl licenseKeyData = new LicenseKeyDataImpl("customer", null, null, null, null, "testLicenseKey");
    telemetryRegistry.setLicenseKey(licenseKeyData);

    // when
    managementService.deleteLicenseKey();

    // then
    assertThat(telemetryRegistry.getLicenseKey()).isNull();
  }
}
