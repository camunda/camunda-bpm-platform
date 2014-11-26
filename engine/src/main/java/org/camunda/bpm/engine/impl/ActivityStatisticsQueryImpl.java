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
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

public class ActivityStatisticsQueryImpl extends
    AbstractQuery<ActivityStatisticsQuery, ActivityStatistics> implements ActivityStatisticsQuery{

  protected static final long serialVersionUID = 1L;
  protected boolean includeFailedJobs = false;
  protected String processDefinitionId;
  protected boolean includeIncidents;
  protected String includeIncidentsForType;
  
  public ActivityStatisticsQueryImpl(String processDefinitionId, CommandExecutor executor) {
    super(executor);
    this.processDefinitionId = processDefinitionId;
  }
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return 
      commandContext
        .getStatisticsManager()
        .getStatisticsCountGroupedByActivity(this);
  }

  public List<ActivityStatistics> executeList(
      CommandContext commandContext, Page page) {
    checkQueryOk();
    return 
      commandContext
        .getStatisticsManager()
        .getStatisticsGroupedByActivity(this, page);
  }
  
  public ActivityStatisticsQuery includeFailedJobs() {
    includeFailedJobs = true;
    return this;
  }

  public ActivityStatisticsQuery includeIncidents() {
    includeIncidents = true;
    return this;
  }

  public ActivityStatisticsQuery includeIncidentsForType(String incidentType) {
    this.includeIncidentsForType = incidentType;
    return this;
  }
  
  public boolean isFailedJobsToInclude() {
    return includeFailedJobs;
  }
  
  public boolean isIncidentsToInclude() {
    return includeIncidents || includeIncidentsForType != null;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  protected void checkQueryOk() {
    super.checkQueryOk();
    ensureNotNull("No valid process definition id supplied", "processDefinitionId", processDefinitionId);
    if (includeIncidents && includeIncidentsForType != null) {
      throw new ProcessEngineException("Invalid query: It is not possible to use includeIncident() and includeIncidentForType() to execute one query.");
    }
  }
}
