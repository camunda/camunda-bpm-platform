package org.camunda.bpm.engine.rest.dto.history;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;

public class HistoricVariableInstanceQueryDto extends AbstractQueryDto<HistoricVariableInstanceQuery> {

	private static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "instanceId";
	private static final String SORT_BY_VARIABLE_NAME_VALUE = "variableName";

	private static final List<String> VALID_SORT_BY_VALUES;
	static {
		VALID_SORT_BY_VALUES = new ArrayList<String>();
		VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_VARIABLE_NAME_VALUE);
	}
	
	private String processInstanceId;
	private String variableName;
	private String variableNameLike;
	private Object variableValue;
	
	public HistoricVariableInstanceQueryDto() {
		
	}
	
	public HistoricVariableInstanceQueryDto(
			MultivaluedMap<String, String> queryParameters) {
		super(queryParameters);
	}
	
	@CamundaQueryParam("processInstanceId")
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
    
	@CamundaQueryParam("variableName")
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	@CamundaQueryParam("variableNameLike")
	public void setVariableNameLike(String variableNameLike) {
		this.variableNameLike = variableNameLike;
	}

	@CamundaQueryParam("variableValue")
	public void setVariableValue(Object variableValue) {
		this.variableValue = variableValue;
	}

	@Override
	protected boolean isValidSortByValue(String value) {
		return VALID_SORT_BY_VALUES.contains(value);
	}	

	@Override
	protected HistoricVariableInstanceQuery createNewQuery(ProcessEngine engine) {
		return engine.getHistoryService().createHistoricVariableInstanceQuery();		
	}

	@Override
	protected void applyFilters(HistoricVariableInstanceQuery query) {
		if (processInstanceId != null) {
			query.processInstanceId(processInstanceId);
		}
		if (variableName != null) {
			query.variableName(variableName);
		}
		if (variableNameLike != null) {
			query.variableNameLike(variableNameLike);
		}
		if (variableName != null && variableValue != null) {
			query.variableValueEquals(variableName, variableValue);
		}		
	}

	@Override
	protected void applySortingOptions(HistoricVariableInstanceQuery query) {
		if (sortBy != null) {
			if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
				query.orderByProcessInstanceId();
			} else if (sortBy.equals(SORT_BY_VARIABLE_NAME_VALUE)) {
				query.orderByVariableName();		
		}

		if (sortOrder != null) {
			if (sortOrder.equals(SORT_ORDER_ASC_VALUE)) {
				query.asc();
			} else if (sortOrder.equals(SORT_ORDER_DESC_VALUE)) {
				query.desc();
			}
		}		
	}

}
	
}
