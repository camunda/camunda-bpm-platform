/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author roman.smirnov
 */
public class VariableInstanceQueryDto extends AbstractQueryDto<VariableInstanceQuery> {

  private static final String SORT_BY_VARIABLE_NAME_VALUE = "variableName";
  private static final String SORT_BY_VARIABLE_TYPE_VALUE = "variableType";
  private static final String SORT_BY_ACTIVITY_INSTANCE_ID_VALUE = "activityInstanceId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_VARIABLE_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_VARIABLE_TYPE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_INSTANCE_ID_VALUE);
  }

  protected String variableName;
  protected String variableNameLike;
  protected List<VariableQueryParameterDto> variableValues;
  protected String[] executionIdIn;
  protected String[] processInstanceIdIn;
  protected String[] caseExecutionIdIn;
  protected String[] caseInstanceIdIn;
  protected String[] taskIdIn;
  protected String[] variableScopeIdIn;
  protected String[] activityInstanceIdIn;

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

  @CamundaQueryParam(value="variableScopeIdIn", converter = StringArrayConverter.class)
  public void setVariableScopeIdIn(String[] variableScopeIdIn) {
    this.variableScopeIdIn = variableScopeIdIn;
  }

  @CamundaQueryParam(value="activityInstanceIdIn", converter = StringArrayConverter.class)
  public void setActivityInstanceIdIn(String[] activityInstanceIdIn) {
    this.activityInstanceIdIn = activityInstanceIdIn;
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

    if (variableScopeIdIn != null && variableScopeIdIn.length > 0) {
      query.variableScopeIdIn(variableScopeIdIn);
    }

    if (activityInstanceIdIn != null && activityInstanceIdIn.length > 0) {
      query.activityInstanceIdIn(activityInstanceIdIn);
    }
  }

  @Override
  protected void applySortingOptions(VariableInstanceQuery query) {
    if (sortBy != null) {
      if (sortBy.equals(SORT_BY_VARIABLE_NAME_VALUE)) {
        query.orderByVariableName();
      } else if (sortBy.equals(SORT_BY_VARIABLE_TYPE_VALUE)) {
        query.orderByVariableType();
      } else if (sortBy.equals(SORT_BY_ACTIVITY_INSTANCE_ID_VALUE)) {
        query.orderByActivityInstanceId();
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
