/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author roman.smirnov
 */
public class VariableInstanceQueryDto extends AbstractQueryDto<VariableInstanceQuery> {

  private static final String SORT_BY_VARIABLE_NAME_VALUE = "variableName";
  private static final String SORT_BY_VARIABLE_TYPE_VALUE = "variableType";
  private static final String SORT_BY_ACTIVITY_INSTANCE_ID_VALUE = "activityInstanceId";
  private static final String SORT_BY_TENANT_ID = "tenantId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_VARIABLE_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_VARIABLE_TYPE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected String variableName;
  protected String variableNameLike;
  protected List<VariableQueryParameterDto> variableValues;
  protected Boolean variableNamesIgnoreCase;
  protected Boolean variableValuesIgnoreCase;
  protected String[] executionIdIn;
  protected String[] processInstanceIdIn;
  protected String[] caseExecutionIdIn;
  protected String[] caseInstanceIdIn;
  protected String[] taskIdIn;
  protected String[] batchIdIn;
  protected String[] variableScopeIdIn;
  protected String[] activityInstanceIdIn;
  private List<String> tenantIds;

  public VariableInstanceQueryDto() {}

  public VariableInstanceQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("variableName")
  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  @CamundaQueryParam("variableNameLike")
  public void setVariableNameLike(String variableNameLike) {
    this.variableNameLike = variableNameLike;
  }

  @CamundaQueryParam(value = "variableValues", converter = VariableListConverter.class)
  public void setVariableValues(List<VariableQueryParameterDto> variableValues) {
    this.variableValues = variableValues;
  }

  @CamundaQueryParam(value = "variableNamesIgnoreCase", converter = BooleanConverter.class)
  public void setVariableNamesIgnoreCase(Boolean variableNamesIgnoreCase) {
    this.variableNamesIgnoreCase = variableNamesIgnoreCase;
  }

  @CamundaQueryParam(value = "variableValuesIgnoreCase", converter = BooleanConverter.class)
  public void setVariableValuesIgnoreCase(Boolean variableValuesIgnoreCase) {
    this.variableValuesIgnoreCase = variableValuesIgnoreCase;
  }

  @CamundaQueryParam(value="executionIdIn", converter = StringArrayConverter.class)
  public void setExecutionIdIn(String[] executionIdIn) {
    this.executionIdIn = executionIdIn;
  }

  @CamundaQueryParam(value="processInstanceIdIn", converter = StringArrayConverter.class)
  public void setProcessInstanceIdIn(String[] processInstanceIdIn) {
    this.processInstanceIdIn = processInstanceIdIn;
  }

  @CamundaQueryParam(value="caseExecutionIdIn", converter = StringArrayConverter.class)
  public void setCaseExecutionIdIn(String[] caseExecutionIdIn) {
    this.caseExecutionIdIn = caseExecutionIdIn;
  }

  @CamundaQueryParam(value="caseInstanceIdIn", converter = StringArrayConverter.class)
  public void setCaseInstanceIdIn(String[] caseInstanceIdIn) {
    this.caseInstanceIdIn = caseInstanceIdIn;
  }

  @CamundaQueryParam(value="taskIdIn", converter = StringArrayConverter.class)
  public void setTaskIdIn(String[] taskIdIn) {
    this.taskIdIn = taskIdIn;
  }

  @CamundaQueryParam(value="batchIdIn", converter = StringArrayConverter.class)
  public void setBatchIdIn(String[] batchIdIn) {
    this.batchIdIn = batchIdIn;
  }

  @CamundaQueryParam(value="variableScopeIdIn", converter = StringArrayConverter.class)
  public void setVariableScopeIdIn(String[] variableScopeIdIn) {
    this.variableScopeIdIn = variableScopeIdIn;
  }

  @CamundaQueryParam(value="activityInstanceIdIn", converter = StringArrayConverter.class)
  public void setActivityInstanceIdIn(String[] activityInstanceIdIn) {
    this.activityInstanceIdIn = activityInstanceIdIn;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected VariableInstanceQuery createNewQuery(ProcessEngine engine) {
    return engine.getRuntimeService().createVariableInstanceQuery();
  }

  @Override
  protected void applyFilters(VariableInstanceQuery query) {
    if (variableName != null) {
      query.variableName(variableName);
    }

    if (variableNameLike != null) {
      query.variableNameLike(variableNameLike);
    }

    if(Boolean.TRUE.equals(variableNamesIgnoreCase)) {
      query.matchVariableNamesIgnoreCase();
    }

    if(Boolean.TRUE.equals(variableValuesIgnoreCase)) {
      query.matchVariableValuesIgnoreCase();
    }

    if (variableValues != null) {
      for (VariableQueryParameterDto variableQueryParam : variableValues) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.resolveValue(objectMapper);

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

    if (executionIdIn != null && executionIdIn.length > 0) {
      query.executionIdIn(executionIdIn);
    }

    if (processInstanceIdIn != null && processInstanceIdIn.length > 0) {
      query.processInstanceIdIn(processInstanceIdIn);
    }

    if (caseExecutionIdIn != null && caseExecutionIdIn.length > 0) {
      query.caseExecutionIdIn(caseExecutionIdIn);
    }

    if (caseInstanceIdIn != null && caseInstanceIdIn.length > 0) {
      query.caseInstanceIdIn(caseInstanceIdIn);
    }

    if (taskIdIn != null && taskIdIn.length > 0) {
      query.taskIdIn(taskIdIn);
    }

    if (batchIdIn != null && batchIdIn.length > 0) {
      query.batchIdIn(batchIdIn);
    }

    if (variableScopeIdIn != null && variableScopeIdIn.length > 0) {
      query.variableScopeIdIn(variableScopeIdIn);
    }

    if (activityInstanceIdIn != null && activityInstanceIdIn.length > 0) {
      query.activityInstanceIdIn(activityInstanceIdIn);
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[0]));
    }
  }

  @Override
  protected void applySortBy(VariableInstanceQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_VARIABLE_NAME_VALUE)) {
      query.orderByVariableName();
    } else if (sortBy.equals(SORT_BY_VARIABLE_TYPE_VALUE)) {
      query.orderByVariableType();
    } else if (sortBy.equals(SORT_BY_ACTIVITY_INSTANCE_ID_VALUE)) {
      query.orderByActivityInstanceId();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
