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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Date;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.MetricsQuery;

/**
 * @author Daniel Meyer
 *
 */
public class MetricsQueryImpl implements Serializable, Command<Object>, MetricsQuery {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected Date startDate;
  protected Date endDate;
  protected MetricsQueryType queryType;

  protected transient CommandExecutor commandExecutor;

  public MetricsQueryImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public MetricsQueryImpl name(String name) {
    ensureNotNull("name", name);
    this.name = name;
    return this;
  }

  public MetricsQueryImpl startDate(Date startDate) {
    ensureNotNull("startDate", startDate);
    this.startDate = startDate;
    return this;
  }

  public MetricsQueryImpl endDate(Date endDate) {
    ensureNotNull("endDate", endDate);
    this.endDate = endDate;
    return this;
  }

  public long sum() {
    queryType = MetricsQueryType.SUM;
    return (Long) commandExecutor.execute(this);
  }

  public Object execute(CommandContext commandContext) {
    switch (queryType) {
    case SUM:
      return commandContext.getMeterLogManager()
          .executeSelectSum(this);
    }
    throw new ProcessEngineException("Incorrect query type. Must be one of "+MetricsQueryType.values());
  }

  public Date getStartDate() {
    return startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public String getName() {
    return name;
  }

}
