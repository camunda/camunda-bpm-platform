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
package org.camunda.bpm.engine.rest.impl;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.rest.TelemetryRestService;
import org.camunda.bpm.engine.rest.dto.TelemetryConfigurationDto;
import org.camunda.bpm.engine.rest.dto.telemetry.TelemetryDataDto;
import org.camunda.bpm.engine.telemetry.TelemetryData;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TelemetryRestServiceImpl extends AbstractRestProcessEngineAware implements TelemetryRestService {

  public TelemetryRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public void configureTelemetry(TelemetryConfigurationDto dto) {
    boolean enableTelemetry = dto.isEnableTelemetry();

    ManagementService managementService = getProcessEngine().getManagementService();

    managementService.toggleTelemetry(enableTelemetry);
  }

  @Override
  public TelemetryConfigurationDto getTelemetryConfiguration() {
    ManagementService managementService = getProcessEngine().getManagementService();

    Boolean telemetryEnabled = managementService.isTelemetryEnabled();
    return new TelemetryConfigurationDto(telemetryEnabled);
  }

  @Override
  public TelemetryDataDto getTelemetryData() {
    ManagementService managementService = getProcessEngine().getManagementService();
    TelemetryData data = managementService.getTelemetryData();

    return TelemetryDataDto.fromEngineDto(data);
  }
}
