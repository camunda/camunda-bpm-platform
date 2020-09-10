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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

public class TelemetryLogger extends ProcessEngineLogger {

  public void startTelemetrySendingTask() {
    logDebug(
        "001", "Start telemetry sending task.");
  }

  public void exceptionWhileSendingTelemetryData(Exception e) {
    logWarn("002",
        "Could not send telemetry data. Reason: {} with message '{}'. Set this logger to DEBUG/FINE for the full stacktrace.",
        e.getClass().getSimpleName(),
        e.getMessage());
    logDebug(
        "003", "{} occurred while sending telemetry data.",
        e.getClass().getCanonicalName(),
        e);
  }

  public ProcessEngineException unexpectedResponseWhileSendingTelemetryData(int responseCode) {
    return new ProcessEngineException(
      exceptionMessage("004", "Unexpected response code {} when sending telemetry data", responseCode));
  }

  public void unexpectedResponseWhileSendingTelemetryData() {
    logDebug(
        "005", "Unexpected 'null' response while sending telemetry data.");
  }

  public void sendingTelemetryData(String data) {
    logDebug(
        "006", "Sending telemetry data: {}", data);
  }

  public void databaseTelemetryPropertyMissingInfo(boolean telemetryEnabled) {
    logInfo(
        "007",
        "`camunda.telemetry.enabled` property is missing in the database, creating the property with value: {}",
        Boolean.toString(telemetryEnabled));
  }

  public void databaseTelemetryPropertyMissingInfo() {
    logInfo(
        "008",
        "`camunda.telemetry.enabled` property is missing in the database");
  }

  public void telemetryDisabled() {
    logDebug(
        "009", "Sending telemetry is disabled.");
  }

  public ProcessEngineException schedulingTaskFails(Exception e) {
    return new ProcessEngineException(
        exceptionMessage("010", "Cannot schedule the telemetry task."), e);
  }

  public void schedulingTaskFailsOnEngineStart(Exception e) {
    logWarn("013",
        "Could not start telemetry task. Reason: {} with message '{}'. Set this logger to DEBUG/FINE for the full stacktrace.",
        e.getClass().getSimpleName(),
        e.getMessage());
    logDebug(
        "014", "{} occurred while starting the telemetry task.",
        e.getClass().getCanonicalName(),
        e);
  }

  public void unableToConfigureHttpConnectorWarning() {
    logWarn(
        "011","The http connector used to send telemetry is `null`, telemetry data will not be sent.");
  }

  public void unexpectedExceptionDuringHttpConnectorConfiguration(Exception e) {
    logDebug(
        "012", "'{}' exception occurred while configuring http connector with message: {}",
        e.getClass().getCanonicalName(),
        e.getMessage());
  }

  public void unexpectedResponseSuccessCode(int statusCode) {
    logDebug(
        "015", "Telemetry request was sent, but received an unexpected response success code: {}", statusCode);
  }


  public void telemetrySentSuccessfully() {
    logDebug(
        "016", "Telemetry request was successful.");
  }
}
