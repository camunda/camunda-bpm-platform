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
package org.camunda.bpm.engine.impl.telemetry.reporter;

import java.util.Timer;

import org.apache.http.client.HttpClient;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;

public class TelemetryReporter {

  // send report every 24 hours
  protected long reportingIntervalInSeconds = 24 * 60 * 60;

  protected TelemetrySendingTask telemetrySendingTask;
  protected Timer timer;

  protected CommandExecutor commandExecutor;
  protected String telemetryEndpoint;
  protected Data data;
  protected HttpClient httpClient;

  protected boolean stopped;

  public TelemetryReporter(CommandExecutor commandExecutor,
                           String telemetryEndpoint,
                           Data data,
                           HttpClient httpClient) {
    this.commandExecutor = commandExecutor;
    this.telemetryEndpoint = telemetryEndpoint;
    this.data = data;
    this.httpClient = httpClient;
    initTelemetrySendingTask();
  }

  protected void initTelemetrySendingTask() {
    telemetrySendingTask = new TelemetrySendingTask(commandExecutor,
                                                    telemetryEndpoint,
                                                    data,
                                                    httpClient);
  }

  public void start() {
    if (stopped) {
      // if the reporter was already stopped another task should be scheduled
      initTelemetrySendingTask();
    }
    timer = new Timer("Camunda Telemetry Reporter", true);
    long reportingIntervalInMillis =  reportingIntervalInSeconds * 1000;

    timer.scheduleAtFixedRate(telemetrySendingTask, reportingIntervalInMillis, reportingIntervalInMillis);
  }

  public void stop() {
    if (timer != null) {
      // cancel the timer
      timer.cancel();
      timer = null;
      // collect and send manually for the last time
      reportNow();
    }
    stopped = true;
  }

  public void reportNow() {
    if (telemetrySendingTask != null) {
      telemetrySendingTask.run();
    }
  }

  public long getReportingIntervalInSeconds() {
    return reportingIntervalInSeconds;
  }

  public TelemetrySendingTask getTelemetrySendingTask() {
    return telemetrySendingTask;
  }

  public void setTelemetrySendingTask(TelemetrySendingTask telemetrySendingTask) {
    this.telemetrySendingTask = telemetrySendingTask;
  }

  public String getTelemetryEndpoint() {
    return telemetryEndpoint;
  }

}
