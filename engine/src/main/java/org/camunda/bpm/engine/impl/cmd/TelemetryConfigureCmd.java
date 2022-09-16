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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.telemetry.TelemetryLogger;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.camunda.bpm.engine.impl.util.TelemetryUtil;

public class TelemetryConfigureCmd implements Command<Void> {

  protected static final TelemetryLogger LOG = ProcessEngineLogger.TELEMETRY_LOGGER;

  protected static final String TELEMETRY_PROPERTY = "camunda.telemetry.enabled";

  protected boolean telemetryEnabled;

  public TelemetryConfigureCmd(boolean telemetryEnabled) {
    this.telemetryEnabled = telemetryEnabled;
  }

  public Void execute(CommandContext commandContext) {

    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkCamundaAdminOrPermission(CommandChecker::checkConfigureTelemetry);


    commandContext.runWithoutAuthorization(() -> {
      toggleTelemetry(commandContext);
      return null;
    });


    return null;
  }

  protected void toggleTelemetry(CommandContext commandContext) {

    Boolean currentValue = new IsTelemetryEnabledCmd().execute(commandContext);

    new SetPropertyCmd(TELEMETRY_PROPERTY, Boolean.toString(telemetryEnabled)).execute(commandContext);

    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();

    boolean isReportedActivated = processEngineConfiguration.isTelemetryReporterActivate();
    TelemetryReporter telemetryReporter = processEngineConfiguration.getTelemetryReporter();

    // telemetry enabled or set for the first time
    if (currentValue == null || (!currentValue.booleanValue() && telemetryEnabled)) {
      if (isReportedActivated) {
        telemetryReporter.reschedule();
      }
    }

    // reset collected data when telemetry is enabled
    if(telemetryEnabled && !processEngineConfiguration.getTelemetryRegistry().isTelemetryLocallyActivated()) {
      // reset data collection time frame only if telemetry was disabled and is now enabled
      processEngineConfiguration.getTelemetryReporter().getTelemetrySendingTask().updateDataCollectionStartDate();
    }
    // we don't want to send data that has been collected before consent was given
    TelemetryUtil.toggleLocalTelemetry(
        telemetryEnabled,
        processEngineConfiguration.getTelemetryRegistry(),
        processEngineConfiguration.getMetricsRegistry());
  }

}
