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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.PersistentObject;
import org.camunda.bpm.engine.runtime.Incident;

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

  @SuppressWarnings("unchecked")
  public static List<IncidentEntity> createAndInsertIncident(boolean recursive, String incidentType, 
      String executionId, String causeIncidentId, String rootCauseIncidentId, String configuration) {
    
    if(executionId != null) {
      // fetch execution
      ExecutionEntity execution = Context
        .getCommandContext()
        .getExecutionManager()
        .findExecutionById(executionId);
      
      List<IncidentEntity> result = new ArrayList<IncidentEntity>();
      
      // decorate new incident
      IncidentEntity newIncident = new IncidentEntity();
      newIncident.setIncidentTimestamp(new Date());
      newIncident.setIncidentType(incidentType);
      newIncident.setExecutionId(executionId);
      newIncident.setActivityId(execution.getActivityId());
      newIncident.setProcessInstanceId(execution.getProcessInstanceId());
      newIncident.setProcessDefinitionId(execution.getProcessDefinitionId());
      newIncident.setCauseIncidentId(causeIncidentId);
      newIncident.setRootCauseIncidentId(rootCauseIncidentId);
      newIncident.setConfiguration(configuration);
      
      // add new incident to result set
      result.add(newIncident);
      
      // persist new incident
      Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(newIncident);
      
      // add link to execution      
      execution.addIncident(newIncident);

      if (recursive) {
        // Instantiate recursive a new incident a super execution
        // (i.e. super process instance) which is affected from this
        // incident.
        // For example: a super process instance called via CallActivity 
        // a new process instance on which an incident happened, so that
        // the super process instance has an incident too.
        String superExecutionId = null;
        if (execution.getId().equals(execution.getProcessInstanceId())) {
          superExecutionId = execution.getSuperExecutionId();
        } else {
          superExecutionId = execution.getProcessInstance().getSuperExecutionId();
        }
        if (superExecutionId != null && !superExecutionId.isEmpty()) {
          if (rootCauseIncidentId == null) {
            // If a root cause has not been set, then this incident (newIncident)
            // will be set to be the root cause.
            rootCauseIncidentId = newIncident.getId();
          }
          List<IncidentEntity> incidents = createAndInsertIncident(recursive, incidentType, superExecutionId, newIncident.getId(), rootCauseIncidentId, null); 
          result.addAll(incidents);
        }
      }
      
      return result;
    }
    
    return Collections.EMPTY_LIST;
  }
  
  public void delete(ExecutionEntity execution) {
    // Extract possible super execution of the assigned execution
    ExecutionEntity superExecution = null;
    if (execution.getId().equals(execution.getProcessInstanceId())) {
      superExecution = execution.getSuperExecution();
    } else {
      superExecution = execution.getProcessInstance().getSuperExecution();
    }
    
    if (superExecution != null) {
      // get the incident, where this incident is the cause
      IncidentEntity incident = superExecution.getIncidentByCauseIncidentId(getId());
      
      if (incident != null) {
        // delete the incident
        incident.delete(superExecution);
      }
    }
    // delete this incident
    Context
      .getCommandContext()
      .getDbSqlSession()
      .delete(this);
    
    // remove link to execution
    execution.removeIncident(this);
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
    processInstanceId = execution.getProcessInstanceId();
    execution.addIncident(this);
  }
  
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("incidentTimestamp", this.incidentTimestamp);
    persistentState.put("incidentType", this.incidentType);
    persistentState.put("executionId", this.executionId);
    persistentState.put("activityId", this.activityId);
    persistentState.put("processInstanceId", this.processInstanceId);
    persistentState.put("processDefinitionId", this.processDefinitionId);
    persistentState.put("causeId", this.causeIncidentId);
    persistentState.put("rootCauseId", this.rootCauseIncidentId);
    persistentState.put("configuration", this.configuration);
    return persistentState;
  }

}
