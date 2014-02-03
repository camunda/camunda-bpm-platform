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
package org.camunda.bpm.engine.impl;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 *
 * @author Roman Smirnov
 *
 */
public class HistoricActivityStatisticsQueryImpl extends AbstractQuery<HistoricActivityStatisticsQuery, HistoricActivityStatistics> implements HistoricActivityStatisticsQuery {

  private static final long serialVersionUID = 1L;

  protected String processDefinitionId;

  protected boolean includeFinished;
  protected boolean includeCanceled;
  protected boolean includeCompleteScope;

  public HistoricActivityStatisticsQueryImpl(String processDefinitionId, CommandExecutor commandExecutor) {
    super(commandExecutor);
    this.processDefinitionId = processDefinitionId;
  }

  public HistoricActivityStatisticsQuery includeFinished() {
    includeFinished = true;
    return this;
  }

  public HistoricActivityStatisticsQuery includeCanceled() {
    includeCanceled = true;
    return this;
  }

  public HistoricActivityStatisticsQuery includeCompleteScope() {
    includeCompleteScope = true;
    return this;
  }

  public HistoricActivityStatisticsQuery orderByActivityId() {
    return orderBy(HistoricActivityStatisticsQueryProperty.ACTIVITY_ID_);
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return
      commandContext
        .getHistoricStatisticsManager()
        .getHistoricStatisticsCountGroupedByActivity(this);
  }

  public List<HistoricActivityStatistics> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return
      commandContext
        .getHistoricStatisticsManager()
        .getHistoricStatisticsGroupedByActivity(this, page);
  }

  protected void checkQueryOk() {
    super.checkQueryOk();
    if (processDefinitionId == null) {
      throw new ProcessEngineException("No valid process definition id supplied.");
    }
  }

  // getters /////////////////////////////////////////////////

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public boolean isIncludeFinished() {
    return includeFinished;
  }

  public boolean isIncludeCanceled() {
    return includeCanceled;
  }

  public boolean isIncludeCompleteScope() {
    return includeCompleteScope;
  }

}
