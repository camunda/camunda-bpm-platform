package org.camunda.bpm.engine.rest.dto;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.QueryOperator;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

public class ProcessInstanceQueryDto extends SortableParameterizedQueryDto {

  private static final String SORT_BY_INSTANCE_ID_VALUE = "instanceId";
  private static final String SORT_BY_DEFINITION_KEY_VALUE = "definitionKey";
  private static final String SORT_BY_DEFINITION_ID_VALUE = "definitionId";
  
  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DEFINITION_KEY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DEFINITION_ID_VALUE);
  }
  
  private String processDefinitionKey;
  private String businessKey;
  private String processDefinitionId;
  private String superProcessInstanceId;
  private String subProcessInstanceId;
  private Boolean active;
  private Boolean suspended;
  
  private List<VariableQueryParameterDto> variables;

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }
  
  @CamundaQueryParam("businessKey")
  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("super")
  public void setSuperProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
  }

  @CamundaQueryParam("sub")
  public void setSubProcessInstanceId(String subProcessInstanceId) {
    this.subProcessInstanceId = subProcessInstanceId;
  }

  @CamundaQueryParam(value = "active", converter = BooleanConverter.class)
  public void setActive(Boolean active) {
    this.active = active;
  }

  @CamundaQueryParam(value = "suspended", converter = BooleanConverter.class)
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @CamundaQueryParam(value = "variables", converter = VariableListConverter.class)
  public void setVariables(List<VariableQueryParameterDto> variables) {
    this.variables = variables;
  }
  
  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  public ProcessInstanceQuery toQuery(RuntimeService runtimeService) {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    
    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }
    if (businessKey != null) {
      query.processInstanceBusinessKey(businessKey);
    }
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (superProcessInstanceId != null) {
      query.superProcessInstanceId(superProcessInstanceId);
    }
    if (subProcessInstanceId != null) {
      query.subProcessInstanceId(subProcessInstanceId);
    }
    if (active != null && active == true) {
      query.active();
    }
    if (suspended != null && suspended == true) {
      query.suspended();
    }
    if (variables != null) {
      for (VariableQueryParameterDto variableQueryParam : variables) {
        String variableName = variableQueryParam.getVariableKey();
        QueryOperator op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.getVariableValue();
        
        if (op == QueryOperator.EQUALS) {
          query.variableValueEquals(variableName, variableValue);
        } else if (op == QueryOperator.GREATER_THAN) {
          query.variableValueGreaterThan(variableName, variableValue);
        } else if (op == QueryOperator.GREATER_THAN_OR_EQUAL) {
          query.variableValueGreaterThanOrEqual(variableName, variableValue);
        } else if (op == QueryOperator.LESS_THAN) {
          query.variableValueLessThan(variableName, variableValue);
        } else if (op == QueryOperator.LESS_THAN_OR_EQUAL) {
          query.variableValueLessThanOrEqual(variableName, variableValue);
        } else if (op == QueryOperator.NOT_EQUALS) {
          query.variableValueNotEquals(variableName, variableValue);
        } else if (op == QueryOperator.LIKE) {
          query.variableValueLike(variableName, String.valueOf(variableValue));
        }
      }
    }
    
    if (!sortOptionsValid()) {
      throw new InvalidRequestException("You may not specify a single sorting parameter.");
    }
    
    if (sortBy != null) {
      if (sortBy.equals(SORT_BY_INSTANCE_ID_VALUE)) {
        query.orderByProcessInstanceId();
      } else if (sortBy.equals(SORT_BY_DEFINITION_KEY_VALUE)) {
        query.orderByProcessDefinitionKey();
      } else if (sortBy.equals(SORT_BY_DEFINITION_ID_VALUE)) {
        query.orderByProcessDefinitionId();
      }
    }
    
    if (sortOrder != null) {
      if (sortOrder.equals(SORT_ORDER_ASC_VALUE)) {
        query.asc();
      } else if (sortOrder.equals(SORT_ORDER_DESC_VALUE)) {
        query.desc();
      }
    }
    
    return query;
  }
  
}
