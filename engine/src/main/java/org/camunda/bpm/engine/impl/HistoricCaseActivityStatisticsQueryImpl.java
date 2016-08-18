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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricCaseActivityStatistics;
import org.camunda.bpm.engine.history.HistoricCaseActivityStatisticsQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author smirnov
 *
 */
public class HistoricCaseActivityStatisticsQueryImpl extends AbstractQuery<HistoricCaseActivityStatisticsQuery, HistoricCaseActivityStatistics> implements
    HistoricCaseActivityStatisticsQuery {

  private static final long serialVersionUID = 1L;

  protected String caseDefinitionId;

  public HistoricCaseActivityStatisticsQueryImpl(String caseDefinitionId, CommandExecutor commandExecutor) {
    super(commandExecutor);
    this.caseDefinitionId = caseDefinitionId;
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return
      commandContext
        .getHistoricStatisticsManager()
        .getHistoricStatisticsCountGroupedByCaseActivity(this);
  }

  public List<HistoricCaseActivityStatistics> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return
      commandContext
        .getHistoricStatisticsManager()
        .getHistoricStatisticsGroupedByCaseActivity(this, page);
  }

  protected void checkQueryOk() {
    super.checkQueryOk();
    ensureNotNull("No valid case definition id supplied", "caseDefinitionId", caseDefinitionId);
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

}
