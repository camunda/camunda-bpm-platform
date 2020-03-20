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


import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.camunda.bpm.spring.boot.starter.util.CamundaBpmVersion;
import org.springframework.beans.factory.annotation.Autowired;

public class EnterLicenseKeyConfiguration extends AbstractCamundaConfiguration {

  protected static final String DEFAULT_LICENSE_FILE = "camunda-license.txt";
  private static final String LICENSE_HEADER_FOOTER_REGEX = "(?i)[-\\s]*(BEGIN|END)\\s*(OPTIMIZE|CAMUNDA|CAMUNDA\\s*BPM)\\s*LICENSE\\s*KEY[-\\s]*";

  @Autowired
  protected CamundaBpmVersion version;

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    // if version is not enterprise
    if (!version.isEnterprise()) {
      return;
    }

    // try to load the license from the provided URL
    URL fileUrl = camundaBpmProperties.getLicenseFile();
    Optional<String> licenseKey = readLicenseKeyFromUrl(fileUrl);

    // if not, try to find the license key on the classpath
    if (!licenseKey.isPresent()) {
      fileUrl = EnterLicenseKeyConfiguration.class.getClassLoader().getResource(DEFAULT_LICENSE_FILE);
      licenseKey = readLicenseKeyFromUrl(fileUrl);
    }

    // if no license key is provided, return
    if (!licenseKey.isPresent()) {
      return;
    }

    // if a license key is already present in the database, return
    Optional<String> existingLicenseKey = Optional.ofNullable(getLicenseKey(processEngine));
    if (existingLicenseKey.isPresent()) {
      return;
    }

    Optional<String> finalLicenseKey = licenseKey;
    ProcessEngineConfigurationImpl processEngineConfiguration =
      (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    processEngineConfiguration.getCommandExecutorTxRequired().execute((Command<Void>) commandContext -> {
      setLicenseKey(processEngine, finalLicenseKey.get());
      return null;
    });

    LOG.enterLicenseKey(fileUrl);
  }

  protected Optional<String> readLicenseKeyFromUrl(URL licenseFileUrl) {
    if (licenseFileUrl == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(new Scanner(licenseFileUrl.openStream(), "UTF-8").useDelimiter("\\A"))
        .filter(Scanner::hasNext).map(Scanner::next)
        .map(s -> s.replaceAll(LICENSE_HEADER_FOOTER_REGEX, ""))
        .map(s -> s.replaceAll("\\n", ""))
        .map(String::trim);
    } catch (IOException e) {
      LOG.enterLicenseKeyFailed(licenseFileUrl, e);
      return Optional.empty();
    }
  }

  protected String getLicenseKey(ProcessEngine processEngine) {
    return processEngine.getManagementService().getLicenseKey();
  }

  private void setLicenseKey(ProcessEngine processEngine, String license) {
    processEngine.getManagementService().setLicenseKey(license);
  }
}
