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
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.telemetry.TelemetryLogger;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;

public class TelemetryConfigureCmd implements Command<Object> {

  protected static final TelemetryLogger LOG = ProcessEngineLogger.TELEMETRY_LOGGER;

  protected static final String TELEMETRY_PROPERTY = "camunda.telemetry.enabled";

  protected boolean telemetryEnabled;

  public TelemetryConfigureCmd(boolean telemetryEnabled) {
    this.telemetryEnabled = telemetryEnabled;
  }

  public Object execute(CommandContext commandContext) {

    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkCamundaAdmin();

    PropertyEntity telemetryProperty = commandContext.getPropertyManager().findPropertyById(TELEMETRY_PROPERTY);
    if (telemetryProperty != null) {
      telemetryProperty.setValue(Boolean.toString(telemetryEnabled));
    } else {
      LOG.databaseTelemetryPropertyMissingInfo(telemetryEnabled);
      telemetryProperty = new PropertyEntity(TELEMETRY_PROPERTY, Boolean.toString(telemetryEnabled));
      commandContext.getPropertyManager().insert(telemetryProperty);
    }

    TelemetryReporter telemetryReporter = commandContext.getProcessEngineConfiguration().getTelemetryReporter();
    if (telemetryEnabled) {
      telemetryReporter.start();
    } else {
      telemetryReporter.stop();
    }

    return null;
  }

}
