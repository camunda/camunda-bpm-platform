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
package org.camunda.bpm.spring.boot.starter.configuration.impl.custom;

import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.test.helper.StandaloneInMemoryTestConfiguration;
import org.camunda.bpm.spring.boot.starter.util.CamundaBpmVersion;
import org.camunda.bpm.spring.boot.starter.util.CamundaBpmVersionTest;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class EnterLicenseKeyConfigurationTest {

  protected static final URL LICENSE_FILE_URL = EnterLicenseKeyConfiguration.class.getClassLoader().getResource("camunda-license-dummy.txt");

  @Rule
  public ProcessEngineRule processEngineRule = new StandaloneInMemoryTestConfiguration().rule();

  @Test
  public void shouldLoadStringFromUrl() throws Exception {
    // given
    EnterLicenseKeyConfiguration licenseKeyConfiguration = new EnterLicenseKeyConfiguration();

    // when
    final String licenseKey = licenseKeyConfiguration
      .readLicenseKeyFromUrl(LICENSE_FILE_URL)
      .orElse(null);

    // then
    assertValidLicenseKeyFormat(licenseKey);
  }

  @Test
  public void shouldSaveLicenseKey() throws Exception {
    // given
    EnterLicenseKeyConfiguration enterLicenseKeyConfiguration = createEnterLicenseKeyConfiguration();

    // when
    enterLicenseKeyConfiguration.postProcessEngineBuild(processEngineRule.getProcessEngine());

    // then
    String licenseKey = processEngineRule.getManagementService()
                                         .getProperties()
                                         .get("camunda-license-key");
    assertValidLicenseKeyFormat(licenseKey);
  }

  protected void assertValidLicenseKeyFormat(String licenseKey) {
    assertThat(licenseKey)
      .isNotNull()
      .startsWith("1234567890")
      .endsWith("Github;unlimited")
      .doesNotContain("\n");
  }

  protected EnterLicenseKeyConfiguration createEnterLicenseKeyConfiguration() {

    CamundaBpmProperties properties = new CamundaBpmProperties();
    properties.setLicenseFile(LICENSE_FILE_URL);

    CamundaBpmVersion version = CamundaBpmVersionTest.camundaBpmVersion("123-ee");
    EnterLicenseKeyConfiguration enterLicenseKeyConfiguration = new EnterLicenseKeyConfiguration();
    ReflectionTestUtils.setField(enterLicenseKeyConfiguration, "version", version);
    ReflectionTestUtils.setField(enterLicenseKeyConfiguration, "camundaBpmProperties", properties);

    return enterLicenseKeyConfiguration;
  }
}
