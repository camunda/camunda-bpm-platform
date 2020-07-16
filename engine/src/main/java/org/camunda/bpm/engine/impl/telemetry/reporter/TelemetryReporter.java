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

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.telemetry.TelemetryLogger;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.connect.httpclient.HttpConnector;

public class TelemetryReporter {

  protected static final TelemetryLogger LOG = ProcessEngineLogger.TELEMETRY_LOGGER;

  // send report every 24 hours
  protected long reportingIntervalInSeconds = 24 * 60 * 60;

  protected TelemetrySendingTask telemetrySendingTask;
  protected Timer timer;

  protected CommandExecutor commandExecutor;
  protected String telemetryEndpoint;
  protected Data data;
  protected HttpConnector http;

  protected boolean stopped;

  public TelemetryReporter(CommandExecutor commandExecutor,
                           String telemetryEndpoint,
                           Data data,
                           HttpConnector http) {
    this.commandExecutor = commandExecutor;
    this.telemetryEndpoint = telemetryEndpoint;
    this.data = data;
    this.http = http;
    initTelemetrySendingTask();
  }

  protected void initTelemetrySendingTask() {
    telemetrySendingTask = new TelemetrySendingTask(commandExecutor,
                                                    telemetryEndpoint,
                                                    data,
                                                    http);
  }

  public synchronized void start() {
    if (stopped) {
      // if the reporter was already stopped another task should be scheduled
      initTelemetrySendingTask();
    }
    if (timer == null) { // initialize timer if only the the timer is not scheduled yet
      timer = new Timer("Camunda BPM Runtime Telemetry Reporter", true);
      long reportingIntervalInMillis =  reportingIntervalInSeconds * 1000;

      try {
        timer.scheduleAtFixedRate(telemetrySendingTask, reportingIntervalInMillis, reportingIntervalInMillis);
      } catch (Exception e) {
        LOG.schedulingTaskFails(e.getMessage());
      }
    }
  }

  public synchronized void stop() {
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
