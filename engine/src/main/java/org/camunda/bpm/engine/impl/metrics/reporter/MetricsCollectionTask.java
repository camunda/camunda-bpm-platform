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

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.persistence.entity.MeterLogEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 *
 * @author Daniel Meyer
 *
 */
public class MetricsCollectionTask extends TimerTask {

  protected final static Logger log = Logger.getLogger(MetricsCollectionTask.class.getName());

  protected MetricsRegistry metricsRegistry;
  protected CommandExecutor commandExecutor;

  public MetricsCollectionTask(MetricsRegistry metricsRegistry, CommandExecutor commandExecutor) {
    this.metricsRegistry = metricsRegistry;
    this.commandExecutor = commandExecutor;
  }

  public void run() {
    try {
      collectMetrics();
    }
    catch(Exception e) {
      try {
        log.log(Level.WARNING, "Could not collect and log metrics", e);
      } catch (Exception ex) {
        // ignore if log can't be written
      }
    }
  }

  protected void collectMetrics() {

    final List<MeterLogEntity> logs = new ArrayList<MeterLogEntity>();
    for (Meter meter : metricsRegistry.getMeters().values()) {
      logs.add(new MeterLogEntity(meter.getName(),
          meter.getAndClear(),
          ClockUtil.getCurrentTime()));
    }

    commandExecutor.execute(new Command<Void>() {

      public Void execute(CommandContext commandContext) {
        for (MeterLogEntity meterLogEntity : logs) {
          commandContext.getMeterLogManager().insert(meterLogEntity);
        }
        return null;
      }
    });
  }

}
