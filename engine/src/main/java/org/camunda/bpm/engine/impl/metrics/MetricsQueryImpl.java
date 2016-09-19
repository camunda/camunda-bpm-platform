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
package org.camunda.bpm.engine.impl.metrics;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.camunda.bpm.engine.management.MetricIntervalValue;

/**
 * @author Daniel Meyer
 *
 */
public class MetricsQueryImpl extends ListQueryParameterObject implements Serializable, Command<Object>, MetricsQuery {

  public static final int DEFAULT_LIMIT_SELECT_INTERVAL = 200;
  public static final long DEFAULT_SELECT_INTERVAL = 15 * 60;

  private static final long serialVersionUID = 1L;

  protected String name;
  protected String reporter;
  protected Date startDate;
  protected Date endDate;
  protected Long startDateMilliseconds;
  protected Long endDateMilliseconds;
  protected Long interval;

  protected transient CommandExecutor commandExecutor;

  public MetricsQueryImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    this.maxResults = DEFAULT_LIMIT_SELECT_INTERVAL;
    this.interval = DEFAULT_SELECT_INTERVAL;
  }

  public MetricsQueryImpl name(String name) {
    this.name = name;
    return this;
  }

  public MetricsQuery reporter(String reporter) {
    this.reporter = reporter;
    return this;
  }

  public MetricsQueryImpl startDate(Date startDate) {
    this.startDate = startDate;
    this.startDateMilliseconds = startDate.getTime();
    return this;
  }

  public MetricsQueryImpl endDate(Date endDate) {
    this.endDate = endDate;
    this.endDateMilliseconds = endDate.getTime();
    return this;
  }

  /**
   * Contains the command implementation which should be executed either
   * metric sum or select metric grouped by time interval.
   *
   * Note: this enables to quit with the enum distinction
   */
  protected Command<Object> callback;

  @Override
  public List<MetricIntervalValue> interval() {
    callback = new Command() {
      @Override
      public Object execute(CommandContext commandContext) {
        return commandContext.getMeterLogManager()
          .executeSelectInterval(MetricsQueryImpl.this);
      }
    };

    return (List<MetricIntervalValue>) commandExecutor.execute(this);
  }

  @Override
  public List<MetricIntervalValue> interval(long interval) {
    this.interval = interval;
    return interval();
  }

  public long sum() {
    callback = new Command() {
      @Override
      public Object execute(CommandContext commandContext) {
        return commandContext.getMeterLogManager()
          .executeSelectSum(MetricsQueryImpl.this);
      }
    };

    return (Long) commandExecutor.execute(this);
  }

  @Override
  public Object execute(CommandContext commandContext) {
    if (callback != null) {
      return callback.execute(commandContext);
    }
    throw new ProcessEngineException("Query can't be executed. Use either sum or interval to query the metrics.");
  }

  @Override
  public MetricsQuery offset(int offset) {
    setFirstResult(offset);
    return this;
  }

  @Override
  public MetricsQuery limit(int maxResults) {
    setMaxResults(maxResults);
    return this;
  }

  @Override
  public void setMaxResults(int maxResults) {
    if (maxResults > DEFAULT_LIMIT_SELECT_INTERVAL) {
      throw new ProcessEngineException("Metrics interval query row limit can't be set larger than " + DEFAULT_LIMIT_SELECT_INTERVAL + '.');
    }
    this.maxResults = maxResults;
  }

  public Date getStartDate() {
    return startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public Long getStartDateMilliseconds() {
    return startDateMilliseconds;
  }

  public Long getEndDateMilliseconds() {
    return endDateMilliseconds;
  }

  public String getName() {
    return name;
  }

  public String getReporter() {
    return reporter;
  }

  public Long getInterval() {
    if (interval == null) {
      return DEFAULT_SELECT_INTERVAL;
    }
    return interval;
  }

  @Override
  public int getMaxResults() {
    if (maxResults > DEFAULT_LIMIT_SELECT_INTERVAL) {
      return DEFAULT_LIMIT_SELECT_INTERVAL;
    }
    return super.getMaxResults();
  }

}
