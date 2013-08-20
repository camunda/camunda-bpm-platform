package org.camunda.bpm.engine.rest.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkCommandDto {

	private List<String> ids;
	private Map<String, Object> variables = new HashMap<String,Object>();

	public List<String> getIds() {
		return ids;
	}
	
	public Map<String, Object> getVariables() {
		return variables;
	}	
	
	public Object getVariableValue(String variableName){
		if(variables.containsKey(variableName)){
			return variables.get(variableName);
		}
       return null;		
	}
	
	public static BulkCommandDto fromCommandDetails(List<String> ids,Map<String, Object> variables){
		BulkCommandDto dto = new BulkCommandDto();
		dto.ids = ids;
		dto.variables = variables;
		return dto; 
	}
}
