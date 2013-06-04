package org.camunda.bpm.engine.management;

public interface IncidentStatistics {
  
  /**
   * Returns the type of the incidents.
   */
  String getIncidentType();
  
  /**
   * Returns the number of incidents to the corresponding
   * incidentType.
   */
  int getIncidentCount();

}
