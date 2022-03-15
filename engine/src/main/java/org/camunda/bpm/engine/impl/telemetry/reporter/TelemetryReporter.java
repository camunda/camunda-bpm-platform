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
import org.camunda.bpm.engine.impl.cmd.IsTelemetryEnabledCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.telemetry.TelemetryLogger;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.TelemetryDataImpl;
import org.camunda.connect.spi.Connector;
import org.camunda.connect.spi.ConnectorRequest;

public class TelemetryReporter {

  protected static final TelemetryLogger LOG = ProcessEngineLogger.TELEMETRY_LOGGER;

  protected long reportingIntervalInSeconds;
  /**
   * Report after 5 minutes the first time so that we get an initial ping
   * quickly. 5 minutes delay so that other modules (e.g. those collecting the app
   * server name) can contribute their data.
   */
  public static long DEFAULT_INIT_REPORT_DELAY_SECONDS = 5 * 60;
  /**
   * Report after 3 hours for the first time so that other modules (e.g. those
   * collecting the app server name) can contribute their data and test cases
   * which accidentally enable reporting are very unlikely to send data.
   */
  public static long EXTENDED_INIT_REPORT_DELAY_SECONDS = 3 * 60 * 60;

  protected TelemetrySendingTask telemetrySendingTask;
  protected Timer timer;

  protected CommandExecutor commandExecutor;
  protected String telemetryEndpoint;
  protected int telemetryRequestRetries;
  protected TelemetryDataImpl data;
  protected Connector<? extends ConnectorRequest<?>> httpConnector;
  protected TelemetryRegistry telemetryRegistry;
  protected MetricsRegistry metricsRegistry;
  protected int telemetryRequestTimeout;

  public TelemetryReporter(CommandExecutor commandExecutor,
                           String telemetryEndpoint,
                           int telemetryRequestRetries,
                           long telemetryReportingPeriod,
                           TelemetryDataImpl data,
                           Connector<? extends ConnectorRequest<?>> httpConnector,
                           TelemetryRegistry telemetryRegistry,
                           MetricsRegistry metricsRegistry,
                           int telemetryRequestTimeout) {
    this.commandExecutor = commandExecutor;
    this.telemetryEndpoint = telemetryEndpoint;
    this.telemetryRequestRetries = telemetryRequestRetries;
    this.reportingIntervalInSeconds = telemetryReportingPeriod;
    this.data = data;
    this.httpConnector = httpConnector;
    this.telemetryRegistry = telemetryRegistry;
    this.metricsRegistry = metricsRegistry;
    this.telemetryRequestTimeout = telemetryRequestTimeout;
    initTelemetrySendingTask();
  }

  protected void initTelemetrySendingTask() {
    telemetrySendingTask = new TelemetrySendingTask(commandExecutor,
                                                    telemetryEndpoint,
                                                    telemetryRequestRetries,
                                                    data,
                                                    httpConnector,
                                                    telemetryRegistry,
                                                    metricsRegistry,
                                                    telemetryRequestTimeout);
  }

  public synchronized void start() {
    if (!isScheduled()) { // initialize timer only if not scheduled yet
      initTelemetrySendingTask();

      timer = new Timer("Camunda BPM Runtime Telemetry Reporter", true);
      long reportingIntervalInMillis =  reportingIntervalInSeconds * 1000;
      long initialReportingDelay = getInitialReportingDelaySeconds() * 1000;

      try {
        timer.scheduleAtFixedRate(telemetrySendingTask, initialReportingDelay, reportingIntervalInMillis);
      } catch (Exception e) {
        timer = null;
        throw LOG.schedulingTaskFails(e);
      }
    }
  }

  public synchronized void reschedule() {
    stop(false);
    start();
  }

  public synchronized void stop() {
    stop(true);
  }

  public synchronized void stop(boolean report) {
    if (isScheduled()) {
      // cancel the timer
      timer.cancel();
      timer = null;

      if (report) {
        // collect and send manually for the last time
        reportNow();
      }
    }
  }

  public void reportNow() {
    if (telemetrySendingTask != null) {
      telemetrySendingTask.run();
    }
  }

  public boolean isScheduled() {
    return timer != null;
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

  public Connector<? extends ConnectorRequest<?>> getHttpConnector() {
    return httpConnector;
  }

  public long getInitialReportingDelaySeconds() {
    Boolean enabled = commandExecutor.execute(new IsTelemetryEnabledCmd());
    return enabled == null ? EXTENDED_INIT_REPORT_DELAY_SECONDS : DEFAULT_INIT_REPORT_DELAY_SECONDS;
  }

}
