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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Incident;

/**
 * @author roman.smirnov
 */
public class IncidentEntity implements Incident, DbEntity, HasDbRevision, HasDbReferences {

  protected int revision;

  protected String id;
  protected Date incidentTimestamp;
  protected String incidentType;
  protected String executionId;
  protected String activityId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String causeIncidentId;
  protected String rootCauseIncidentId;
  protected String configuration;
  protected String incidentMessage;
  protected String tenantId;

  public List<IncidentEntity> createRecursiveIncidents() {
    List<IncidentEntity> createdIncidents = new ArrayList<IncidentEntity>();
    createRecursiveIncidents(id, createdIncidents);
    return createdIncidents;
  }

  /** Instantiate recursive a new incident a super execution
   * (i.e. super process instance) which is affected from this
   * incident.
   * For example: a super process instance called via CallActivity
   * a new process instance on which an incident happened, so that
   * the super process instance has an incident too. */
  protected void createRecursiveIncidents(String rootCauseIncidentId, List<IncidentEntity> createdIncidents) {

    final ExecutionEntity execution = getExecution();

    if(execution != null) {

      ExecutionEntity superExecution = execution.getProcessInstance().getSuperExecution();

      if (superExecution != null) {

        // create a new incident
        IncidentEntity newIncident = create(incidentType);
        newIncident.setExecution(superExecution);

        // set cause and root cause
        newIncident.setCauseIncidentId(id);
        newIncident.setRootCauseIncidentId(rootCauseIncidentId);

        // insert new incident (and create a new historic incident)
        insert(newIncident);

        // add new incident to result set
        createdIncidents.add(newIncident);

        newIncident.createRecursiveIncidents(rootCauseIncidentId, createdIncidents);
      }
    }
  }

  /**
   * use {@link #createAndInsertIncident(String, IncidentContext, String)}
   */
  @Deprecated
  public static IncidentEntity createAndInsertIncident(String incidentType, String executionId, String configuration, String message) {
    IncidentContext ctx = new IncidentContext();
    ctx.setExecutionId(executionId);
    ctx.setConfiguration(configuration);

    return createAndInsertIncident(incidentType, ctx, message);
  }

  public static IncidentEntity createAndInsertIncident(String incidentType, IncidentContext context, String message) {
    // create new incident
    IncidentEntity newIncident = create(incidentType);

    newIncident.setConfiguration(context.getConfiguration());
    newIncident.setIncidentMessage(message);

    if(context.getExecutionId() != null) {
      // fetch execution
      ExecutionEntity execution = Context
        .getCommandContext()
        .getExecutionManager()
        .findExecutionById(context.getExecutionId());

      // inherit further properties from execution
      newIncident.setExecution(execution);

    } else {
      // set further properties from context
      newIncident.setActivityId(context.getActivityId());
      newIncident.setProcessDefinitionId(context.getProcessDefinitionId());
      newIncident.setTenantId(context.getTenantId());
    }

    // insert new incident (and create a new historic incident)
    insert(newIncident);

    return newIncident;
  }

  protected static IncidentEntity create(String incidentType) {

    String incidentId = Context.getProcessEngineConfiguration()
        .getDbSqlSessionFactory()
        .getIdGenerator()
        .getNextId();

    // decorate new incident
    IncidentEntity newIncident = new IncidentEntity();
    newIncident.setId(incidentId);
    newIncident.setIncidentTimestamp(ClockUtil.getCurrentTime());
    newIncident.setIncidentType(incidentType);
    newIncident.setCauseIncidentId(incidentId);
    newIncident.setRootCauseIncidentId(incidentId);

    return newIncident;
  }

  protected static void insert(IncidentEntity incident) {
    // persist new incident
    Context
      .getCommandContext()
      .getDbEntityManager()
      .insert(incident);

    incident.fireHistoricIncidentEvent(HistoryEventTypes.INCIDENT_CREATE);
  }

  public void delete() {
    remove(false);
  }

  public void resolve() {
    remove(true);
  }

  protected void remove(boolean resolved) {

    ExecutionEntity execution = getExecution();

    if(execution != null) {
      // Extract possible super execution of the assigned execution
      ExecutionEntity superExecution = null;
      if (execution.getId().equals(execution.getProcessInstanceId())) {
        superExecution = execution.getSuperExecution();
      } else {
        superExecution = execution.getProcessInstance().getSuperExecution();
      }

      if (superExecution != null) {
        // get the incident, where this incident is the cause
        IncidentEntity parentIncident = superExecution.getIncidentByCauseIncidentId(getId());

        if (parentIncident != null) {
          // remove the incident
          parentIncident.remove(resolved);
        }
      }

      // remove link to execution
      execution.removeIncident(this);
    }

    // always delete the incident
    Context
      .getCommandContext()
      .getDbEntityManager()
      .delete(this);

    // update historic incident
    HistoryEventType eventType = resolved ? HistoryEventTypes.INCIDENT_RESOLVE : HistoryEventTypes.INCIDENT_DELETE;
    fireHistoricIncidentEvent(eventType);
  }

  protected void fireHistoricIncidentEvent(HistoryEventType eventType) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    HistoryLevel historyLevel = processEngineConfiguration.getHistoryLevel();
    if(historyLevel.isHistoryEventProduced(eventType, this)) {

      final HistoryEventProducer eventProducer = processEngineConfiguration.getHistoryEventProducer();
      final HistoryEventHandler eventHandler = processEngineConfiguration.getHistoryEventHandler();

      HistoryEvent event = null;
      if (HistoryEvent.INCIDENT_CREATE.equals(eventType.getEventName())) {
        event = eventProducer.createHistoricIncidentCreateEvt(this);

      } else if (HistoryEvent.INCIDENT_RESOLVE.equals(eventType.getEventName())) {
        event = eventProducer.createHistoricIncidentResolveEvt(this);

      } else if (HistoryEvent.INCIDENT_DELETE.equals(eventType.getEventName())) {
        event = eventProducer.createHistoricIncidentDeleteEvt(this);

      } else {
        return;
      }

      eventHandler.handleEvent(event);
    }
  }

  @Override
  public boolean hasReferenceTo(DbEntity entity) {
    if (entity instanceof IncidentEntity) {
      IncidentEntity incident = (IncidentEntity) entity;
      String otherId = incident.getId();

      if(causeIncidentId != null && causeIncidentId.equals(otherId)) {
        return true;
      }

    }
    return false;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Date getIncidentTimestamp() {
    return incidentTimestamp;
  }

  public void setIncidentTimestamp(Date incidentTimestamp) {
    this.incidentTimestamp = incidentTimestamp;
  }

  @Override
  public String getIncidentType() {
    return incidentType;
  }

  public void setIncidentType(String incidentType) {
    this.incidentType = incidentType;
  }

  @Override
  public String getIncidentMessage() {
    return incidentMessage;
  }

  public void setIncidentMessage(String incidentMessage) {
    this.incidentMessage = incidentMessage;
  }

  @Override
  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @Override
  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  @Override
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public ProcessDefinitionEntity getProcessDefinition() {
    if (processDefinitionId != null) {
      return Context
          .getProcessEngineConfiguration()
          .getDeploymentCache()
          .findDeployedProcessDefinitionById(processDefinitionId);
    }
    return null;
  }

  @Override
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @Override
  public String getCauseIncidentId() {
    return causeIncidentId;
  }

  public void setCauseIncidentId(String causeIncidentId) {
    this.causeIncidentId = causeIncidentId;
  }

  @Override
  public String getRootCauseIncidentId() {
    return rootCauseIncidentId;
  }

  public void setRootCauseIncidentId(String rootCauseIncidentId) {
    this.rootCauseIncidentId = rootCauseIncidentId;
  }

  @Override
  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  @Override
  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public void setExecution(ExecutionEntity execution) {
    if (execution != null) {
      executionId = execution.getId();
      activityId = execution.getActivityId();
      processInstanceId = execution.getProcessInstanceId();
      processDefinitionId = execution.getProcessDefinitionId();
      tenantId = execution.getTenantId();

      execution.addIncident(this);
    }
    else {
      getExecution().removeIncident(this);
      executionId = null;
      activityId = null;
      processInstanceId = null;
      processDefinitionId = null;
      tenantId = null;
    }
  }

  public ExecutionEntity getExecution() {
    if(executionId != null) {
      return Context.getCommandContext()
        .getExecutionManager()
        .findExecutionById(executionId);
    } else {
      return null;
    }
  }

  @Override
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("executionId", executionId);
    persistentState.put("processDefinitionId", processDefinitionId);
    persistentState.put("activityId", activityId);
    return persistentState;
  }

  @Override
  public void setRevision(int revision) {
    this.revision = revision;
  }

  @Override
  public int getRevision() {
    return revision;
  }

  @Override
  public int getRevisionNext() {
    return revision + 1;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", incidentTimestamp=" + incidentTimestamp
           + ", incidentType=" + incidentType
           + ", executionId=" + executionId
           + ", activityId=" + activityId
           + ", processInstanceId=" + processInstanceId
           + ", processDefinitionId=" + processDefinitionId
           + ", causeIncidentId=" + causeIncidentId
           + ", rootCauseIncidentId=" + rootCauseIncidentId
           + ", configuration=" + configuration
           + ", tenantId=" + tenantId
           + ", incidentMessage=" + incidentMessage
           + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IncidentEntity other = (IncidentEntity) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }


}
