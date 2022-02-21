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
package org.camunda.bpm.engine.rest.util;

import static org.camunda.bpm.engine.rest.util.EngineUtil.getProcessEngineProvider;

import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.telemetry.PlatformTelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

public class WebApplicationUtil {

  public static void setApplicationServer(String serverInfo) {
    if (serverInfo != null && !serverInfo.isEmpty() ) {
      // set the application server info globally for all engines in the container
      if (PlatformTelemetryRegistry.getApplicationServer() == null) {
        PlatformTelemetryRegistry.setApplicationServer(serverInfo);
      }
    }
  }

  public static void setLicenseKey(LicenseKeyDataImpl licenseKeyData) {
    if (licenseKeyData != null) {
      ProcessEngineProvider processEngineProvider = getProcessEngineProvider();
      for (String engineName : processEngineProvider.getProcessEngineNames()) {
        if (engineName != null) {
          ManagementServiceImpl managementService = (ManagementServiceImpl) processEngineProvider.getProcessEngine(engineName).getManagementService();
          managementService.setLicenseKeyForTelemetry(licenseKeyData);
        }
      }
    }
  }

  /**
   * Adds the web application name to the telemetry data of the engine.
   *
   * @param engineName
   *          the engine for which the web application usage should be indicated
   * @param webapp
   *          the web application that is used with the engine
   * @return whether the web application was successfully added or not
   */
  public static boolean setWebapp(String engineName, String webapp) {
    ProcessEngineProvider processEngineProvider = getProcessEngineProvider();
    ManagementServiceImpl managementService = (ManagementServiceImpl) processEngineProvider.getProcessEngine(engineName).getManagementService();
    return managementService.addWebappToTelemetry(webapp);
  }
}
