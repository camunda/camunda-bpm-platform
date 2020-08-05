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
package org.camunda.bpm.engine.impl.telemetry;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;

public class TelemetryLogger extends ProcessEngineLogger {

  public void startTelemetrySendingTask() {
    logDebug(
        "001", "Start telemetry sending task.");
  }

  public void exceptionWhileSendingTelemetryData(Exception e) {
    logDebug(
        "002", "'{}' exception occurred while sending telemetry data with message: {}", 
        e.getClass().getCanonicalName(),
        e.getMessage());
  }

  public void unexpectedResponseWhileSendingTelemetryData(int responseCode) {
    logDebug(
        "003", "Unexpected response while sending telemetry data. Status code: {}", responseCode);
  }

  public void unexpectedResponseWhileSendingTelemetryData() {
    logDebug(
        "004", "Unexpected 'null' response while sending telemetry data.");
  }

  public void telemetryDataSent(String data) {
    logDebug(
        "005", "Telemetry data sent: {}", data);
  }

  public void databaseTelemetryPropertyMissingInfo(boolean telemetryEnabled) {
    logInfo(
        "006",
        "`camunda.telemetry.enabled` property is missing in the database, creating the property with value: {}",
        Boolean.toString(telemetryEnabled));
  }

  public void databaseTelemetryPropertyMissingInfo() {
    logInfo(
        "007",
        "`camunda.telemetry.enabled` property is missing in the database");
  }

  public void telemetryDisabled() {
    logDebug(
        "008", "Sending telemetry is disabled.");
  }

  public void schedulingTaskFails(String message) {
    logDebug(
        "009", "An exception occured during scheduling telemetry task: {}", message);
  }

  public void unableToConfigureHttpConnectorWarning() {
    logWarn(
        "010","The http connector used to send telemetry is `null`, telemetry data will not be sent.");
  }

  public void unexpectedExceptionDuringHttpConnectorConfiguration(Exception e) {
    logDebug(
        "011", "'{}' exception occurred while configuring http connector with message: {}", 
        e.getClass().getCanonicalName(),
        e.getMessage());
  }
}
