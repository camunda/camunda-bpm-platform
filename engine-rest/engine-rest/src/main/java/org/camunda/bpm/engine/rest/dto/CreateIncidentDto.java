package org.camunda.bpm.engine.rest.dto;

public class CreateIncidentDto {

  private String incidentType;
  private String configuration;
  private String message;

  public String getIncidentType() {
    return incidentType;
  }

  public void setIncidentType(String incidentType) {
    this.incidentType = incidentType;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}