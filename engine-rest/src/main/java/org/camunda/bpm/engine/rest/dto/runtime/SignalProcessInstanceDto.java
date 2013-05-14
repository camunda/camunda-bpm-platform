package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.Map;


public class SignalProcessInstanceDto {
	
	private String activityId;
	private Map<String, Object> variables;

	public Map<String, Object> getVariables() {
	  return variables;
	}
  
	public void setVariables(Map<String, Object> variables) {
	  this.variables = variables;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
}
