package org.camunda.bpm.engine.rest.dto.history;

import org.camunda.bpm.engine.history.HistoricVariableInstance;

public class HistoricVariableInstanceDto {
	
	  private String id;	  
	  private String variableName;
	  private String variableTypeName;
	  private Object value;
	  private String processInstanceId;

	public static HistoricVariableInstanceDto fromHistoricVariableInstance(
			HistoricVariableInstance historicVariableInstance) {
		  HistoricVariableInstanceDto dto = new HistoricVariableInstanceDto();
		  dto.id = historicVariableInstance.getId();	  
		  dto.variableName = historicVariableInstance.getVariableName();
		  dto.variableTypeName = historicVariableInstance.getVariableTypeName();
		  dto.value = historicVariableInstance.getValue();
		  dto.processInstanceId = historicVariableInstance.getProcessInstanceId();
		return dto;
	}
	  
	public String getId() {
		return id;
	}
	public String getVariableName() {
		return variableName;
	}
	public String getVariableTypeName() {
		return variableTypeName;
	}
	public Object getValue() {
		return value;
	}
	public String getProcessInstanceId() {
		return processInstanceId;
	}

}
