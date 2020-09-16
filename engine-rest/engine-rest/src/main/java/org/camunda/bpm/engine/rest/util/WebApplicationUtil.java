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

import java.util.function.Consumer;
import java.util.function.Function;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyData;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

public class WebApplicationUtil {

  public static void setApplicationServer(String serverInfo) {
    if (serverInfo != null && !serverInfo.isEmpty() ) {
      addToTelemetryRegistry(t -> t.setApplicationServer(serverInfo), t -> t.getApplicationServer() == null);
    }
  }

  public static void setLicenseKey(LicenseKeyData licenseKeyData) {
    if (licenseKeyData != null) {
      addToTelemetryRegistry(t -> t.setLicenseKey(licenseKeyData), t -> true);
    }
  }

  protected static void addToTelemetryRegistry(Consumer<TelemetryRegistry> addTelemetryData, Function<TelemetryRegistry, Boolean> addTelemetryDataFilter) {
    ProcessEngineProvider processEngineProvider = getProcessEngineProvider();
    for (String engineName : processEngineProvider.getProcessEngineNames()) {
      TelemetryRegistry telemetryRegistry = getTelemetryRegistry(processEngineProvider, engineName);
      if (telemetryRegistry != null && addTelemetryDataFilter.apply(telemetryRegistry)) {
          addTelemetryData.accept(telemetryRegistry);
      }
    }
  }

  protected static TelemetryRegistry getTelemetryRegistry(ProcessEngineProvider processEngineProvider, String engineName) {
    ProcessEngine processEngine = processEngineProvider.getProcessEngine(engineName);
    ProcessEngineConfiguration configuration = processEngine.getProcessEngineConfiguration();
    return configuration.getTelemetryRegistry();
  }
}
