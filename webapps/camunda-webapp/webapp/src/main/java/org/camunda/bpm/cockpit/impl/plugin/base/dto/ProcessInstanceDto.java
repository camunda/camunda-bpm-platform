package org.camunda.bpm.cockpit.impl.plugin.base.dto;

import java.util.Date;
import java.util.List;

public class ProcessInstanceDto {

  protected String id;
  protected String businessKey;
  protected Date startTime;
  protected List<IncidentStatisticsDto> incidents;

  public ProcessInstanceDto() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }
  
  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public List<IncidentStatisticsDto> getIncidents() {
    return incidents;
  }
  
  public void setIncidents(List<IncidentStatisticsDto> incidents) {
    this.incidents = incidents;
  }

}
