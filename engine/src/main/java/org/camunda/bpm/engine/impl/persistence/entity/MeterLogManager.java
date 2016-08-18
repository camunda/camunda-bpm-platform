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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.metrics.MetricsQueryImpl;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.Metric;

/**
 * @author Daniel Meyer
 *
 */
public class MeterLogManager extends AbstractManager {

  public static final String SELECT_METER_INTERVAL = "selectMeterLogAggregatedByTimeInterval";
  public static final String SELECT_METER_SUM = "selectMeterLogSum";
  public static final String DELETE_ALL_METER = "deleteAllMeterLogEntries";
  public static final String DELETE_ALL_METER_BY_TIMESTAMP_AND_REPORTER = "deleteMeterLogEntriesByTimestampAndReporter";

  public void insert(MeterLogEntity meterLogEntity) {
    getDbEntityManager()
     .insert(meterLogEntity);
  }

  public Long executeSelectSum(MetricsQueryImpl query) {
    Long result = (Long) getDbEntityManager().selectOne(SELECT_METER_SUM, query);
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

  public List<Metric> executeSelectInterval(MetricsQueryImpl query) {
    return getDbEntityManager().selectList(SELECT_METER_INTERVAL, query);
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
    getDbEntityManager().delete(MeterLogEntity.class, DELETE_ALL_METER, null);
  }

  public void deleteByTimestampAndReporter(Date timestamp, String reporter) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("timestamp", timestamp);
    parameters.put("reporter", reporter);
    getDbEntityManager().delete(MeterLogEntity.class, DELETE_ALL_METER_BY_TIMESTAMP_AND_REPORTER, parameters);
  }

}
