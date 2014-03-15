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

  public HistoricIncidentQueryImpl() {
  }

  public HistoricIncidentQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public HistoricIncidentQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricIncidentQuery incidentId(String incidentId) {
    assertParamNotNull("incidentId", incidentId);
    this.id = incidentId;
    return this;
  }

  public HistoricIncidentQuery incidentType(String incidentType) {
    assertParamNotNull("incidentType", incidentType);
    this.incidentType = incidentType;
    return this;
  }

  public HistoricIncidentQuery incidentMessage(String incidentMessage) {
    assertParamNotNull("incidentMessage", incidentMessage);
    this.incidentMessage = incidentMessage;
    return this;
  }

  public HistoricIncidentQuery executionId(String executionId) {
    assertParamNotNull("executionId", executionId);
    this.executionId = executionId;
    return this;
  }

  public HistoricIncidentQuery activityId(String activityId) {
    assertParamNotNull("activityId", activityId);
    this.activityId = activityId;
    return this;
  }

  public HistoricIncidentQuery processInstanceId(String processInstanceId) {
    assertParamNotNull("processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricIncidentQuery processDefinitionId(String processDefinitionId) {
    assertParamNotNull("processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public HistoricIncidentQuery causeIncidentId(String causeIncidentId) {
    assertParamNotNull("causeIncidentId", causeIncidentId);
    this.causeIncidentId = causeIncidentId;
    return this;
  }

  public HistoricIncidentQuery rootCauseIncidentId(String rootCauseIncidentId) {
    assertParamNotNull("rootCauseIncidentId", rootCauseIncidentId);
    this.rootCauseIncidentId = rootCauseIncidentId;
    return this;
  }

  public HistoricIncidentQuery configuration(String configuration) {
    assertParamNotNull("configuration", configuration);
    this.configuration = configuration;
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
    this.orderProperty = HistoricIncidentQueryProperty.INCIDENT_ID;
    return this;
  }

  public HistoricIncidentQuery orderByCreateTime() {
    this.orderProperty = HistoricIncidentQueryProperty.INCIDENT_CREATE_TIME;
    return this;
  }

  public HistoricIncidentQuery orderByEndTime() {
    this.orderProperty = HistoricIncidentQueryProperty.INCIDENT_END_TIME;
    return this;
  }

  public HistoricIncidentQuery orderByIncidentType() {
    this.orderProperty = HistoricIncidentQueryProperty.INCIDENT_TYPE;
    return this;
  }

  public HistoricIncidentQuery orderByExecutionId() {
    this.orderProperty = HistoricIncidentQueryProperty.EXECUTION_ID;
    return this;
  }

  public HistoricIncidentQuery orderByActivityId() {
    this.orderProperty = HistoricIncidentQueryProperty.ACTIVITY_ID;
    return this;
  }

  public HistoricIncidentQuery orderByProcessInstanceId() {
    this.orderProperty = HistoricIncidentQueryProperty.PROCESS_INSTANCE_ID;
    return this;
  }

  public HistoricIncidentQuery orderByProcessDefinitionId() {
    this.orderProperty = HistoricIncidentQueryProperty.PROCESS_DEFINITION_ID;
    return this;
  }

  public HistoricIncidentQuery orderByCauseIncidentId() {
    this.orderProperty = HistoricIncidentQueryProperty.CAUSE_INCIDENT_ID;
    return this;
  }

  public HistoricIncidentQuery orderByRootCauseIncidentId() {
    this.orderProperty = HistoricIncidentQueryProperty.ROOT_CAUSE_INCIDENT_ID;
    return this;
  }

  public HistoricIncidentQuery orderByConfiguration() {
    this.orderProperty = HistoricIncidentQueryProperty.CONFIGURATION;
    return this;
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
