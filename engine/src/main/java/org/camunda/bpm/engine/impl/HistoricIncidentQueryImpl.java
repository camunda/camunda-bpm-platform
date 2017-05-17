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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.history.IncidentState;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricIncidentQueryImpl extends AbstractVariableQueryImpl<HistoricIncidentQuery, HistoricIncident> implements HistoricIncidentQuery {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String incidentType;
  protected String incidentMessage;
  protected String executionId;
  protected String activityId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String causeIncidentId;
  protected String rootCauseIncidentId;
  protected String configuration;
  protected IncidentState incidentState;
  protected String[] tenantIds;
  protected String[] jobDefinitionIds;

  public HistoricIncidentQueryImpl() {
  }

  public HistoricIncidentQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricIncidentQuery incidentId(String incidentId) {
    ensureNotNull("incidentId", incidentId);
    this.id = incidentId;
    return this;
  }

  public HistoricIncidentQuery incidentType(String incidentType) {
    ensureNotNull("incidentType", incidentType);
    this.incidentType = incidentType;
    return this;
  }

  public HistoricIncidentQuery incidentMessage(String incidentMessage) {
    ensureNotNull("incidentMessage", incidentMessage);
    this.incidentMessage = incidentMessage;
    return this;
  }

  public HistoricIncidentQuery executionId(String executionId) {
    ensureNotNull("executionId", executionId);
    this.executionId = executionId;
    return this;
  }

  public HistoricIncidentQuery activityId(String activityId) {
    ensureNotNull("activityId", activityId);
    this.activityId = activityId;
    return this;
  }

  public HistoricIncidentQuery processInstanceId(String processInstanceId) {
    ensureNotNull("processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricIncidentQuery processDefinitionId(String processDefinitionId) {
    ensureNotNull("processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public HistoricIncidentQuery causeIncidentId(String causeIncidentId) {
    ensureNotNull("causeIncidentId", causeIncidentId);
    this.causeIncidentId = causeIncidentId;
    return this;
  }

  public HistoricIncidentQuery rootCauseIncidentId(String rootCauseIncidentId) {
    ensureNotNull("rootCauseIncidentId", rootCauseIncidentId);
    this.rootCauseIncidentId = rootCauseIncidentId;
    return this;
  }

  public HistoricIncidentQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    return this;
  }

  public HistoricIncidentQuery configuration(String configuration) {
    ensureNotNull("configuration", configuration);
    this.configuration = configuration;
    return this;
  }

  public HistoricIncidentQuery jobDefinitionIdIn(String... jobDefinitionIds) {
    ensureNotNull("jobDefinitionIds", (Object[]) jobDefinitionIds);
    this.jobDefinitionIds = jobDefinitionIds;
    return this;
  }

  public HistoricIncidentQuery open() {
    if (incidentState != null) {
      throw new ProcessEngineException("Already querying for incident state <" + incidentState + ">");
    }
    incidentState = IncidentState.DEFAULT;
    return this;
  }

  public HistoricIncidentQuery resolved() {
    if (incidentState != null) {
      throw new ProcessEngineException("Already querying for incident state <" + incidentState + ">");
    }
    incidentState = IncidentState.RESOLVED;
    return this;
  }

  public HistoricIncidentQuery deleted() {
    if (incidentState != null) {
      throw new ProcessEngineException("Already querying for incident state <" + incidentState + ">");
    }
    incidentState = IncidentState.DELETED;
    return this;
  }

  // ordering ////////////////////////////////////////////////////

  public HistoricIncidentQuery orderByIncidentId() {
    orderBy(HistoricIncidentQueryProperty.INCIDENT_ID);
    return this;
  }

  public HistoricIncidentQuery orderByCreateTime() {
    orderBy(HistoricIncidentQueryProperty.INCIDENT_CREATE_TIME);
    return this;
  }

  public HistoricIncidentQuery orderByEndTime() {
    orderBy(HistoricIncidentQueryProperty.INCIDENT_END_TIME);
    return this;
  }

  public HistoricIncidentQuery orderByIncidentType() {
    orderBy(HistoricIncidentQueryProperty.INCIDENT_TYPE);
    return this;
  }

  public HistoricIncidentQuery orderByExecutionId() {
    orderBy(HistoricIncidentQueryProperty.EXECUTION_ID);
    return this;
  }

  public HistoricIncidentQuery orderByActivityId() {
    orderBy(HistoricIncidentQueryProperty.ACTIVITY_ID);
    return this;
  }

  public HistoricIncidentQuery orderByProcessInstanceId() {
    orderBy(HistoricIncidentQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricIncidentQuery orderByProcessDefinitionId() {
    orderBy(HistoricIncidentQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  public HistoricIncidentQuery orderByCauseIncidentId() {
    orderBy(HistoricIncidentQueryProperty.CAUSE_INCIDENT_ID);
    return this;
  }

  public HistoricIncidentQuery orderByRootCauseIncidentId() {
    orderBy(HistoricIncidentQueryProperty.ROOT_CAUSE_INCIDENT_ID);
    return this;
  }

  public HistoricIncidentQuery orderByConfiguration() {
    orderBy(HistoricIncidentQueryProperty.CONFIGURATION);
    return this;
  }

  public HistoricIncidentQuery orderByIncidentState() {
    orderBy(HistoricIncidentQueryProperty.INCIDENT_STATE);
    return this;
  }

  public HistoricIncidentQuery orderByTenantId() {
    return orderBy(HistoricIncidentQueryProperty.TENANT_ID);
  }

  // results ////////////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricIncidentManager()
      .findHistoricIncidentCountByQueryCriteria(this);
  }

  public List<HistoricIncident> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistoricIncidentManager()
      .findHistoricIncidentByQueryCriteria(this, page);
  }


  // getters /////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public String getIncidentType() {
    return incidentType;
  }

  public String getIncidentMessage() {
    return incidentMessage;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getCauseIncidentId() {
    return causeIncidentId;
  }

  public String getRootCauseIncidentId() {
    return rootCauseIncidentId;
  }

  public String getConfiguration() {
    return configuration;
  }

  public IncidentState getIncidentState() {
    return incidentState;
  }

}
