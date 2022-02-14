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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.LicenseCmd;
import org.camunda.bpm.engine.test.api.resources.GetByteArrayCommand;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class LicenseKeyTest {

  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(testRule).around(engineRule);

  ProcessEngine processEngine;
  ProcessEngineConfigurationImpl processEngineConfiguration;
  ManagementService managementService;
  IdentityService identityService;

  @Before
  public void init() {
    processEngine = engineRule.getProcessEngine();
    processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    managementService = processEngine.getManagementService();
    identityService = processEngine.getIdentityService();
  }

  @After
  public void tearDown() {
    managementService.deleteLicenseKey();
  }

  @Test
  public void shouldSetLicenseKey() {
    // given
    String licenseKey = "testLicenseKey";

    // when
    managementService.setLicenseKey(licenseKey);
    Map<String, String> properties = managementService.getProperties();
    String licenseKeyLegacyProperty = properties.get(LicenseCmd.LICENSE_KEY_PROPERTY_NAME);
    String licenseByteArrayId = properties.get(LicenseCmd.LICENSE_KEY_BYTE_ARRAY_ID);

    // then
    assertThat(licenseKeyLegacyProperty).isNull();
    assertThat(licenseByteArrayId).isNotNull();
    // make sure a newly set license is not stored in properties...
    assertThat(licenseByteArrayId).isNotEqualTo(licenseKey);
    // ...but in the byte array table, referenced by the property
    assertThat(processEngineConfiguration.getCommandExecutorTxRequired().execute(new GetByteArrayCommand(licenseByteArrayId)).getBytes())
        .isEqualTo(licenseKey.getBytes());
  }

  @Test
  public void shouldGetLicenseKey() {
    // given
    String licenseKey = "testLicenseKey";

    // when
    managementService.setLicenseKey(licenseKey);
    String storedLicenseKey = managementService.getLicenseKey();

    // then
    assertThat(storedLicenseKey).isEqualTo(licenseKey);
  }

  @Test
  public void shouldGetLegacyLicenseKey() {
    // given
    String legacyLicenseKey = "testLegacyLicenseKey";
    managementService.setProperty(LicenseCmd.LICENSE_KEY_PROPERTY_NAME, legacyLicenseKey);

    // when
    String storedLegacyLicenseKey = managementService.getLicenseKey();

    // then
    assertThat(storedLegacyLicenseKey).isEqualTo(legacyLicenseKey);
  }

  @Test
  public void shouldGetNullWithNoLicenseKeySet() {
    // given
    // no license key

    // when
    String storedLegacyLicenseKey = managementService.getLicenseKey();

    // then
    assertThat(storedLegacyLicenseKey).isNull();
  }

  @Test
  public void shouldDeleteLicenseKey() {
    // given
    String licenseKey = "testLicenseKey";

    // when
    managementService.setLicenseKey(licenseKey);
    String licenseByteArrayId = managementService.getProperties().get(LicenseCmd.LICENSE_KEY_BYTE_ARRAY_ID);
    managementService.deleteLicenseKey();
    String storedLicenseKey = managementService.getLicenseKey();

    // then
    // make sure license key is removed
    assertThat(storedLicenseKey).isNull();
    assertThat(managementService.getProperties().containsKey(LicenseCmd.LICENSE_KEY_BYTE_ARRAY_ID)).isFalse();
    assertThat(processEngineConfiguration.getCommandExecutorTxRequired().execute(new GetByteArrayCommand(licenseByteArrayId))).isNull();
  }

  @Test
  public void shouldDeleteLegacyLicenseKey() {
    // given
    String legacyLicenseKey = "testLegacyLicenseKey";
    managementService.setProperty(LicenseCmd.LICENSE_KEY_PROPERTY_NAME, legacyLicenseKey);

    // when
    managementService.deleteLicenseKey();
    String storedLicenseKey = managementService.getLicenseKey();

    // then
    // make sure license key is removed
    assertThat(storedLicenseKey).isNull();
    assertThat(managementService.getProperties().containsKey(LicenseCmd.LICENSE_KEY_PROPERTY_NAME)).isFalse();
  }

  @Test
  public void shouldIgnoreDeleteWithNoLicenseKeySet() {
    // given
    // no license key
    String noLicenseKey = managementService.getLicenseKey();

    // when
    managementService.deleteLicenseKey();
    String noLicenseKeyAfterDelete = managementService.getLicenseKey();

    // then
    // there was never a license key present and no exception was thrown
    assertThat(noLicenseKey).isNull();
    assertThat(noLicenseKeyAfterDelete).isNull();
  }

  @Test
  public void shouldUpdateLicenseKey() {
    // given
    String licenseKey = "testLicenseKey";
    String licenseKey2 = "testLicenseKey2";

    // when
    managementService.setLicenseKey(licenseKey);
    String storedLicenseKey = managementService.getLicenseKey();
    managementService.setLicenseKey(licenseKey2);
    String storedLicenseKey2 = managementService.getLicenseKey();

    // then
    assertThat(storedLicenseKey).isEqualTo(licenseKey);
    assertThat(storedLicenseKey2).isEqualTo(licenseKey2);
    assertThat(storedLicenseKey).isNotEqualTo(storedLicenseKey2);
  }

  @Test
  public void shouldUpdateLegacyLicenseKey() {
    // given
    String legacyLicenseKey = "testLegacyLicenseKey";
    String licenseKey = "testLicenseKey";
    managementService.setProperty(LicenseCmd.LICENSE_KEY_PROPERTY_NAME, legacyLicenseKey);

    // when
    String storedLegacyLicense = managementService.getProperties().get(LicenseCmd.LICENSE_KEY_PROPERTY_NAME);
    managementService.setLicenseKey(licenseKey);
    String storedByteArrayId = managementService.getProperties().get(LicenseCmd.LICENSE_KEY_BYTE_ARRAY_ID);
    byte[] licenseBytes = processEngineConfiguration.getCommandExecutorTxRequired().execute(new GetByteArrayCommand(storedByteArrayId)).getBytes();

    // then
    // legacy license is removed from properties
    assertThat(managementService.getProperties().get(LicenseCmd.LICENSE_KEY_PROPERTY_NAME)).isNull();
    // license is stored in properties table
    assertThat(legacyLicenseKey).isEqualTo(storedLegacyLicense);
    // after update, a reference to a byte array table entry is stored in
    // properties
    assertThat(storedByteArrayId).isNotEqualTo(legacyLicenseKey);
    // license is stored in byte array table
    assertThat(licenseBytes).isEqualTo(licenseKey.getBytes());
  }

  @Test
  public void shouldUpdateDuplicateLicenseKey() {
    // given
    String licenseKey = "testLicenseKey";

    // when
    managementService.setLicenseKey(licenseKey);
    String storedLicenseKey = managementService.getLicenseKey();
    String storedByteArrayId = managementService.getProperties().get(LicenseCmd.LICENSE_KEY_BYTE_ARRAY_ID);
    managementService.setLicenseKey(licenseKey);
    String storedLicenseKey2 = managementService.getLicenseKey();
    String storedByteArrayId2 = managementService.getProperties().get(LicenseCmd.LICENSE_KEY_BYTE_ARRAY_ID);

    // then
    assertThat(storedLicenseKey).isEqualTo(licenseKey);
    assertThat(storedLicenseKey2).isEqualTo(licenseKey);
    assertThat(storedByteArrayId).isNotEqualTo(storedByteArrayId2);
  }

  @Test
  public void shouldThrowExceptionWhenSetNullLicenseKey() {
    // given
    String licenseKey = null;

    // when/then
    assertThatThrownBy(() -> managementService.setLicenseKey(licenseKey))
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("licenseKey is null");

    // and
    String storedLicenseKey = managementService.getLicenseKey();
    assertThat(storedLicenseKey).isNull();
  }
}
