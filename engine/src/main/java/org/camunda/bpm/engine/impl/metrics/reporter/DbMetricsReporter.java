/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.metrics.reporter;


import java.util.Timer;

import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;

/**
 * @author Daniel Meyer
 *
 */
public class DbMetricsReporter {

  protected MetricsRegistry metricsRegistry;
  protected CommandExecutor commandExecutor;

  // log every 15 minutes...
  protected long reportingIntervalInSeconds = 60 * 15;

  protected MetricsCollectionTask metricsCollectionTask;
  private Timer timer;

  public DbMetricsReporter(MetricsRegistry metricsRegistry, CommandExecutor commandExecutor) {
    this.metricsRegistry = metricsRegistry;
    this.commandExecutor = commandExecutor;
    initMetricsColletionTask();
  }

  protected void initMetricsColletionTask() {
    metricsCollectionTask = new MetricsCollectionTask(metricsRegistry, commandExecutor);
  }

  public void start() {
    timer = new Timer("Camunda Metrics Reporter", true);
    long reportingIntervalInMillis = reportingIntervalInSeconds * 1000;

    timer.scheduleAtFixedRate(metricsCollectionTask,
        reportingIntervalInMillis,
        reportingIntervalInMillis);
  }

  public void stop() {
    if(timer != null) {
      // cancel the timer
      timer.cancel();
      timer = null;
      // collect and log manually for the last time
      reportNow();
    }
  }

  public void reportNow() {
    if(metricsCollectionTask != null) {
      metricsCollectionTask.run();
    }
  }

  public long getReportingIntervalInSeconds() {
    return reportingIntervalInSeconds;
  }

  public void setReportingIntervalInSeconds(long reportingIntervalInSeconds) {
    this.reportingIntervalInSeconds = reportingIntervalInSeconds;
  }

  public MetricsRegistry getMetricsRegistry() {
    return metricsRegistry;
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public MetricsCollectionTask getMetricsCollectionTask() {
    return metricsCollectionTask;
  }

  public void setMetricsCollectionTask(MetricsCollectionTask metricsCollectionTask) {
    this.metricsCollectionTask = metricsCollectionTask;
  }

}
