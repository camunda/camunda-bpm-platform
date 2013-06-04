package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.management.IncidentStatistics;

public class IncidentStatisticsEntity implements IncidentStatistics {
  
  
  protected String incidentType;
  protected int incidentCount;

  public String getIncidentType() {
    return incidentType;
  }

  public void setIncidenType(String incidentType) {
    this.incidentType = incidentType;
  }
  
  public int getIncidentCount() {
    return incidentCount;
  }

}
