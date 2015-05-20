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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.Date;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.metrics.MetricsQueryImpl;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * @author Daniel Meyer
 *
 */
public class MeterLogManager extends AbstractManager {

  public void insert(MeterLogEntity meterLogEntity) {
    getDbEntityManager()
     .insert(meterLogEntity);
  }

  public Long executeSelectSum(MetricsQueryImpl query) {
    Long result = (Long) getDbEntityManager().selectOne("selectMeterLogSum", query);
    result = result != null ? result : 0;

    if(shouldAddCurrentUnloggedCount(query)) {
      // add current unlogged count
      Meter meter = Context.getProcessEngineConfiguration()
        .getMetricsRegistry()
        .getMeterByName(query.getName());
      if(meter != null) {
        result += meter.get();
      }
    }

    return result;
  }

  protected boolean shouldAddCurrentUnloggedCount(MetricsQueryImpl query) {

    long reportingIntervalInSeconds = Context.getProcessEngineConfiguration()
      .getDbMetricsReporter()
      .getReportingIntervalInSeconds();

    return query.getName() != null
        && (query.getEndDate() == null
        || query.getEndDate().getTime() >= ClockUtil.getCurrentTime().getTime() - (1000 * reportingIntervalInSeconds));

  }

  public void deleteAll() {
    getDbEntityManager().delete(MeterLogEntity.class, "deleteAllMeterLogEntries", null);
  }

  public void deleteByTimestamp(Date timestamp) {
    getDbEntityManager().delete(MeterLogEntity.class, "deleteMeterLogEntriesByTimestamp", timestamp);
  }

}
