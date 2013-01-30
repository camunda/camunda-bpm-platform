package org.camunda.bpm.engine.rest.dto;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.QueryOperator;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;

import com.google.common.collect.Lists;

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

  @CamundaQueryParam("active")
  public void setActive(Boolean active) {
    this.active = active;
  }

  @CamundaQueryParam("suspended")
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @CamundaQueryParam("variables")
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
        if (variableQueryParam.getOperator() == QueryOperator.EQUALS) {
          query.variableValueEquals(variableQueryParam.getVariableKey(), variableQueryParam.getVariableValue());
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
  
  @Override
  public void setPropertyFromParameterPair(String key, String value) {
    try {
      if (key.equals("variables")) {
        VariableQueryParameterDto queryVariable = new VariableQueryParameterDto();
        
        String[] valueTriple = value.split("_");
        if (valueTriple.length != 3) {
          throw new InvalidRequestException("variable query parameter has to have format KEY_OPERATOR_VALUE.");
        }
        queryVariable.setVariableKey(valueTriple[0]);
        queryVariable.setOperator(QueryOperator.valueOf(valueTriple[1].toUpperCase()));
        queryVariable.setVariableValue(valueTriple[2]);
        
        List<VariableQueryParameterDto> queryVariables = Lists.newArrayList(queryVariable);
        setValueBasedOnAnnotation(key, queryVariables);
      }
      else if (key.equals("active") || key.equals("suspended")) {
        Boolean booleanValue = new Boolean(value);
        setValueBasedOnAnnotation(key, booleanValue);
      } else {
      setValueBasedOnAnnotation(key, value);
      }
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException("Cannot set parameter.");
    } catch (IllegalAccessException e) {
      throw new RestException("Cannot set parameter.");
    } catch (InvocationTargetException e) {
      throw new InvalidRequestException(e.getTargetException().getMessage());
    }
  }
}
