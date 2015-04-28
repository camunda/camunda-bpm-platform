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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.db.PermissionCheck;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.DeploymentStatistics;
import org.camunda.bpm.engine.management.DeploymentStatisticsQuery;

public class DeploymentStatisticsQueryImpl extends AbstractQuery<DeploymentStatisticsQuery, DeploymentStatistics>
implements DeploymentStatisticsQuery {

  protected static final long serialVersionUID = 1L;
  protected boolean includeFailedJobs = false;
  protected boolean includeIncidents = false;
  protected String includeIncidentsForType;

  // for internal use
  protected List<PermissionCheck> processInstancePermissionChecks = new ArrayList<PermissionCheck>();
  protected List<PermissionCheck> jobPermissionChecks = new ArrayList<PermissionCheck>();
  protected List<PermissionCheck> incidentPermissionChecks = new ArrayList<PermissionCheck>();

  public DeploymentStatisticsQueryImpl(CommandExecutor executor) {
    super(executor);
  }

  public DeploymentStatisticsQuery includeFailedJobs() {
    includeFailedJobs = true;
    return this;
  }

  public DeploymentStatisticsQuery includeIncidents() {
    includeIncidents = true;
    return this;
  }

  public DeploymentStatisticsQuery includeIncidentsForType(String incidentType) {
    this.includeIncidentsForType = incidentType;
    return this;
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return
        commandContext
          .getStatisticsManager()
          .getStatisticsCountGroupedByDeployment(this);
  }

  @Override
  public List<DeploymentStatistics> executeList(CommandContext commandContext,
      Page page) {
    checkQueryOk();
    return
      commandContext
        .getStatisticsManager()
        .getStatisticsGroupedByDeployment(this, page);
  }

  public boolean isFailedJobsToInclude() {
    return includeFailedJobs;
  }

  public boolean isIncidentsToInclude() {
    return includeIncidents || includeIncidentsForType != null;
  }

  protected void checkQueryOk() {
    super.checkQueryOk();
    if (includeIncidents && includeIncidentsForType != null) {
      throw new ProcessEngineException("Invalid query: It is not possible to use includeIncident() and includeIncidentForType() to execute one query.");
    }
  }

  // getter/setter for authorization check

  public List<PermissionCheck> getProcessInstancePermissionChecks() {
    return processInstancePermissionChecks;
  }

  public void setProcessInstancePermissionChecks(List<PermissionCheck> processInstancePermissionChecks) {
    this.processInstancePermissionChecks = processInstancePermissionChecks;
  }

  public void addProcessInstancePermissionCheck(PermissionCheck permissionCheck) {
    processInstancePermissionChecks.add(permissionCheck);
  }

  public List<PermissionCheck> getJobPermissionChecks() {
    return jobPermissionChecks;
  }

  public void setJobPermissionChecks(List<PermissionCheck> jobPermissionChecks) {
    this.jobPermissionChecks = jobPermissionChecks;
  }

  public void addJobPermissionCheck(PermissionCheck permissionCheck) {
    jobPermissionChecks.add(permissionCheck);
  }

  public List<PermissionCheck> getIncidentPermissionChecks() {
    return incidentPermissionChecks;
  }

  public void setIncidentPermissionChecks(List<PermissionCheck> incidentPermissionChecks) {
    this.incidentPermissionChecks = incidentPermissionChecks;
  }

  public void addIncidentPermissionCheck(PermissionCheck permissionCheck) {
    incidentPermissionChecks.add(permissionCheck);
  }

}
