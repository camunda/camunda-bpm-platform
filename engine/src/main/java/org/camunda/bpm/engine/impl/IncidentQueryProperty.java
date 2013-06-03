package org.camunda.bpm.engine.impl;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.query.QueryProperty;

public class IncidentQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;
  private static final Map<String, IncidentQueryProperty> properties = new HashMap<String, IncidentQueryProperty>();
  
  public static final IncidentQueryProperty INCIDENT_ID = new IncidentQueryProperty("RES.ID_");
  public static final IncidentQueryProperty INCIDENT_TIMESTAMP = new IncidentQueryProperty("RES.INCIDENT_TIMESTAMP_");
  public static final IncidentQueryProperty INCIDENT_TYPE = new IncidentQueryProperty("RES.INCIDENT_TYPE_");
  public static final IncidentQueryProperty EXECUTION_ID = new IncidentQueryProperty("RES.EXECUTION_ID_");
  public static final IncidentQueryProperty ACTIVITY_ID = new IncidentQueryProperty("RES.ACTIVITY_ID_");
  public static final IncidentQueryProperty PROCESS_INSTANCE_ID = new IncidentQueryProperty("RES.PROC_INST_ID_");
  public static final IncidentQueryProperty PROCESS_DEFINITION_ID = new IncidentQueryProperty("RES.PROC_DEF_ID_");
  public static final IncidentQueryProperty CAUSE_INCIDENT_ID = new IncidentQueryProperty("RES.CAUSE_INCIDENT_ID_");
  public static final IncidentQueryProperty ROOT_CAUSE_INCIDENT_ID = new IncidentQueryProperty("RES.ROOT_CAUSE_INCIDENT_ID_");
  public static final IncidentQueryProperty CONFIGURATION = new IncidentQueryProperty("RES.CONFIGURATION_");
  
  private String name;

  public IncidentQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }
  
  public static IncidentQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
