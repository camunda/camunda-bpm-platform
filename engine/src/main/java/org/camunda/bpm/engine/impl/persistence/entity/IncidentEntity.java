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

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.PersistentObject;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Incident;

/**
 * @author roman.smirnov
 */
public class IncidentEntity implements Incident, PersistentObject {
  
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

      String superExecutionId = execution.getProcessInstance().getSuperExecutionId();
   
      if (superExecutionId != null && !superExecutionId.isEmpty()) {
        
        IncidentEntity newIncident = createAndInsertIncident(incidentType, superExecutionId, null, null);
        newIncident.setCauseIncidentId(id);
        newIncident.setRootCauseIncidentId(rootCauseIncidentId);
        createdIncidents.add(newIncident);
        newIncident.createRecursiveIncidents(rootCauseIncidentId, createdIncidents);
      }
    }
  }
  
  public static IncidentEntity createAndInsertIncident(String incidentType, String configuration, String message) {
    
    String incidentId = Context
        .getProcessEngineConfiguration()
        .getDbSqlSessionFactory()
        .getIdGenerator()
        .getNextId();
    
    // decorate new incident
    IncidentEntity newIncident = new IncidentEntity();
    newIncident.setId(incidentId);
    newIncident.setIncidentTimestamp(ClockUtil.getCurrentTime());
    newIncident.setIncidentMessage(message);
    newIncident.setConfiguration(configuration);
    newIncident.setIncidentType(incidentType);
    newIncident.setCauseIncidentId(incidentId);
    newIncident.setRootCauseIncidentId(incidentId);
        
    // persist new incident
    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(newIncident);
         
    return newIncident;
  }
  
  public static IncidentEntity createAndInsertIncident(String incidentType, String executionId, String configuration, String message) {
    
    // fetch execution
    ExecutionEntity execution = Context
      .getCommandContext()
      .getExecutionManager()
      .findExecutionById(executionId);
        
    // decorate new incident
    IncidentEntity newIncident = createAndInsertIncident(incidentType, configuration, message);
    newIncident.setExecution(execution);
         
    return newIncident;
  }
  
  public static IncidentEntity createAndInsertIncident(String incidentType, String processDefinitionId, String activityId, String configuration, String message) {
        
    // decorate new incident
    IncidentEntity newIncident = createAndInsertIncident(incidentType, configuration, message);
    
    newIncident.setActivityId(activityId);
    newIncident.setProcessDefinitionId(processDefinitionId);
         
    return newIncident;
  }
  
  public void delete() {
    
    final ExecutionEntity execution = getExecution();
    
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
          // delete the incident
          parentIncident.delete();
        }
      }
      
      // remove link to execution
      execution.removeIncident(this);
    }
    
    // always delete the incident
    Context
      .getCommandContext()
      .getDbSqlSession()
      .delete(this);
  }
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  public Date getIncidentTimestamp() {
    return incidentTimestamp;
  }
  
  public void setIncidentTimestamp(Date incidentTimestamp) {
    this.incidentTimestamp = incidentTimestamp; 
  }

  public String getIncidentType() {
    return incidentType;
  }
  
  public void setIncidentType(String incidentType) {
    this.incidentType = incidentType; 
  }
  
  public String getIncidentMessage() {
    return incidentMessage;
  }
  
  public void setIncidentMessage(String incidentMessage) {
    this.incidentMessage = incidentMessage;
  }

  public String getExecutionId() {
    return executionId;
  }
  
  public void setExecutionId(String executionId) {
    this.executionId = executionId; 
  }

  public String getActivityId() {
    return activityId;
  }
  
  public void setActivityId(String activityId) {
    this.activityId = activityId; 
  }
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId; 
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId; 
  }

  public String getCauseIncidentId() {
    return causeIncidentId;
  }
  
  public void setCauseIncidentId(String causeIncidentId) {
    this.causeIncidentId = causeIncidentId; 
  }

  public String getRootCauseIncidentId() {
    return rootCauseIncidentId;
  }
  
  public void setRootCauseIncidentId(String rootCauseIncidentId) {
    this.rootCauseIncidentId = rootCauseIncidentId; 
  }

  public String getConfiguration() {
    return configuration;
  }
  
  public void setConfiguration(String configuration) {
    this.configuration = configuration; 
  }

  public void setExecution(ExecutionEntity execution) {
    executionId = execution.getId();
    activityId = execution.getActivityId();
    processInstanceId = execution.getProcessInstanceId();
    processDefinitionId = execution.getProcessDefinitionId();
    execution.addIncident(this);
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
  
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("incidentTimestamp", this.incidentTimestamp);
    persistentState.put("incidentType", this.incidentType);
    persistentState.put("incidentMessage", this.incidentMessage);
    persistentState.put("executionId", this.executionId);
    persistentState.put("activityId", this.activityId);
    persistentState.put("processInstanceId", this.processInstanceId);
    persistentState.put("processDefinitionId", this.processDefinitionId);
    persistentState.put("causeId", this.causeIncidentId);
    persistentState.put("rootCauseId", this.rootCauseIncidentId);
    persistentState.put("configuration", this.configuration);
    return persistentState;
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
           + ", incidentMessage=" + incidentMessage
           + "]";
  }

}
