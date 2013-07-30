package org.camunda.bpm.engine.rest.dto.history;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;

import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringSetConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

public class HistoricProcessInstanceQueryDto extends AbstractQueryDto<HistoricProcessInstanceQuery> {

	private static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "processInstanceId";
	private static final String SORT_BY_PROCESS_DEFINITION_ID_VALUE = "processDefinitionId";
	private static final String SORT_BY_PROCESS_INSTANCE_BUSINESS_KEY_VALUE = "processInstanceBusinessKey";
	private static final String SORT_BY_PROCESS_INSTANCE_START_TIME_VALUE = "processInstanceStartTime";
	private static final String SORT_BY_PROCESS_INSTANCE_END_TIME_VALUE = "processInstanceEndTime";
	private static final String SORT_BY_PROCESS_INSTANCE_DURATION_VALUE = "processInstanceDuration";
	
	private static final List<String> VALID_SORT_BY_VALUES;
	static {
		VALID_SORT_BY_VALUES = new ArrayList<String>();
		VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_ID_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_BUSINESS_KEY_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_START_TIME_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_END_TIME_VALUE);
		VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_DURATION_VALUE);
	}
	
	   private String processInstanceId;
	   private Set<String> processInstanceIds;
	   private String processDefinitionId;
	   private String processDefinitionKey;
	   private List<String> processDefinitionKeys;
	   private String processInstanceBusinessKey;
	   private Boolean finished;
	   private Date startedBefore;
	   private Date startedAfter;
	   private Date finishedBefore;
	   private Date finishedAfter;
	   private String startedBy;
	   private String superProcessInstanceId;
	   private Boolean deleted;
	  
	private List<VariableQueryParameterDto> variables;
	
	public HistoricProcessInstanceQueryDto(MultivaluedMap<String, String> queryParameters) {
	    super(queryParameters);
    }
	
	@CamundaQueryParam("processInstanceId")
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	@CamundaQueryParam(value = "processInstanceIds", converter = StringSetConverter.class)
	public void setProcessInstanceIds(Set<String> processInstanceIds) {
		this.processInstanceIds = processInstanceIds;
	}

	@CamundaQueryParam("processDefinitionId")
	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	@CamundaQueryParam("processDefinitionKey")
	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}

	@CamundaQueryParam(value = "processDefinitionKeys", converter = StringListConverter.class)
	public void setProcessDefinitionKeys(List<String> processDefinitionKeys) {
		this.processDefinitionKeys = processDefinitionKeys;
	}

	@CamundaQueryParam("processInstanceBusinessKey")
	public void setProcessInstanceBusinessKey(String processInstanceBusinessKey) {
		this.processInstanceBusinessKey = processInstanceBusinessKey;
	}

	@CamundaQueryParam(value = "finished", converter = BooleanConverter.class)
	public void setFinished(Boolean finished) {
		this.finished = finished;
	}

	@CamundaQueryParam(value = "startedBefore", converter = DateConverter.class)
	public void setStartedBefore(Date startedBefore) {
		this.startedBefore = startedBefore;
	}

	@CamundaQueryParam(value = "startedAfter", converter = DateConverter.class)
	public void setStartedAfter(Date startedAfter) {
		this.startedAfter = startedAfter;
	}

	@CamundaQueryParam(value = "finishedBefore", converter = DateConverter.class)
	public void setFinishedBefore(Date finishedBefore) {
		this.finishedBefore = finishedBefore;
	}

	@CamundaQueryParam(value = "finishedAfter", converter = DateConverter.class)
	public void setFinishedAfter(Date finishedAfter) {
		this.finishedAfter = finishedAfter;
	}

	@CamundaQueryParam("startedBy")
	public void setStartedBy(String startedBy) {
		this.startedBy = startedBy;
	}

	@CamundaQueryParam("superProcessInstanceId")
	public void setSuperProcessInstanceId(String superProcessInstanceId) {
		this.superProcessInstanceId = superProcessInstanceId;
	}

	@CamundaQueryParam(value = "deleted", converter = BooleanConverter.class)
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	
	@CamundaQueryParam(value = "variables", converter = VariableListConverter.class)
	public void setVariables(List<VariableQueryParameterDto> variables) {
		this.variables = variables;
	}
	
	@Override
	protected boolean isValidSortByValue(String value) {
		return VALID_SORT_BY_VALUES.contains(value);
	}

	@Override
	protected HistoricProcessInstanceQuery createNewQuery(ProcessEngine engine) {
		return engine.getHistoryService().createHistoricProcessInstanceQuery();	
	}

	@Override
	protected void applyFilters(HistoricProcessInstanceQuery query) {
		
		if (processInstanceId != null) {
	      query.processInstanceId(processInstanceId);
	    }
	    if (processInstanceIds != null) {
	      query.processInstanceIds(processInstanceIds);
	    }
	    if (processDefinitionId != null) {
	      query.processDefinitionId(processDefinitionId);
	    }
	    if (processDefinitionKey != null) {
	      query.processDefinitionKey(processDefinitionKey);
	    }
	    if (processDefinitionKeys != null) {
		      query.processDefinitionKeyNotIn(processDefinitionKeys);
		}
	    if (processInstanceBusinessKey != null) {
		      query.processInstanceBusinessKey(processInstanceBusinessKey);
		}	  
	    if (finished != null) {
	    	if(this.finished) {
	    		  query.finished();
	    	} else {
	    		query.unfinished();
	    	}		    
		}	
		if (startedBefore != null) {
	    	query.startedBefore(startedBefore);
		}	    
		if (startedAfter != null) {
	    	query.startedAfter(startedAfter);
		}	
		if (finishedBefore != null) {
	    	query.finishedBefore(finishedBefore);
		}	
		if (finishedAfter != null) {
	    	query.finishedAfter(finishedAfter);
		}
	    if (startedBy != null) {
		      query.startedBy(startedBy);
		}
	    if (superProcessInstanceId != null) {
		      query.superProcessInstanceId(superProcessInstanceId);
		}
	    if (deleted != null) {
	    	if(this.deleted) {
	    		query.deleted();
	    	} else {
	    		query.notDeleted();
	    	}
		}
	    
	    if (variables != null) {
	      for (VariableQueryParameterDto variableQueryParam : variables) {
	        String variableName = variableQueryParam.getName();
	        String op = variableQueryParam.getOperator();
	        Object variableValue = variableQueryParam.getValue();
	        
	        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
	          query.variableValueEquals(variableName, variableValue);
	        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
	          query.variableValueGreaterThan(variableName, variableValue);
	        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME)) {
	          query.variableValueGreaterThanOrEqual(variableName, variableValue);
	        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
	          query.variableValueLessThan(variableName, variableValue);
	        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME)) {
	          query.variableValueLessThanOrEqual(variableName, variableValue);
	        } else if (op.equals(VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME)) {
	          query.variableValueNotEquals(variableName, variableValue);
	        } else if (op.equals(VariableQueryParameterDto.LIKE_OPERATOR_NAME)) {
	          query.variableValueLike(variableName, String.valueOf(variableValue));
	        } else {
	          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid variable comparator specified: " + op);
	        }
	      }
	    }
	}
	
	@Override
	protected void applySortingOptions(HistoricProcessInstanceQuery query) {
	    if (sortBy != null) {
	        if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
	          query.orderByProcessInstanceId();
	        } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_ID_VALUE)) {
	          query.orderByProcessDefinitionId();
	        } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_BUSINESS_KEY_VALUE)) {
	          query.orderByProcessInstanceBusinessKey();
	        } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_START_TIME_VALUE)) {
		          query.orderByProcessInstanceStartTime();
		    } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_END_TIME_VALUE)) {
		          query.orderByProcessInstanceEndTime();
		    } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_DURATION_VALUE)) {
		          query.orderByProcessInstanceDuration();
		    }
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
