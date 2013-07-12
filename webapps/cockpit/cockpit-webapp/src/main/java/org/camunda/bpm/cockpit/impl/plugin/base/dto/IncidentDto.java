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
package org.camunda.bpm.cockpit.impl.plugin.base.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.runtime.Incident;

/**
 * @author roman.smirnov
 */
public class IncidentDto {
  
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
  
  public String getId() {
    return id;
  }
  
  public Date getIncidentTimestamp() {
    return incidentTimestamp;
  }

  public String getIncidentType() {
    return incidentType;
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
  
  public static IncidentDto fromIncident(Incident incident) {
    IncidentDto result = new IncidentDto();
    
    result.id = incident.getId();
    result.incidentTimestamp = incident.getIncidentTimestamp();
    result.incidentType = incident.getIncidentType();
    result.executionId = incident.getExecutionId();
    result.activityId = incident.getActivityId();
    result.processInstanceId = incident.getProcessInstanceId();
    result.processDefinitionId = incident.getProcessDefinitionId();
    result.causeIncidentId = incident.getCauseIncidentId();
    result.rootCauseIncidentId = incident.getRootCauseIncidentId();
    result.configuration = incident.getConfiguration();
    
    return result;
  }
  
  public static List<IncidentDto> fromListOfIncidents(List<Incident> incidents) {
    List<IncidentDto> dtos = new ArrayList<IncidentDto>();
    for (Incident incident : incidents) {
      IncidentDto dto = fromIncident(incident);
      dtos.add(dto);
    }
    return dtos;
  }

}
