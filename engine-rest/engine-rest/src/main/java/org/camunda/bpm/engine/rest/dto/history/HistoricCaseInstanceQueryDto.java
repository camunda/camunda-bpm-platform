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
package org.camunda.bpm.engine.rest.dto.history;

import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricCaseInstanceQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringSetConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricCaseInstanceQueryDto extends AbstractQueryDto<HistoricCaseInstanceQuery> {

  public static final String SORT_BY_CASE_INSTANCE_ID_VALUE = "instanceId";
  public static final String SORT_BY_CASE_DEFINITION_ID_VALUE = "definitionId";
  public static final String SORT_BY_CASE_INSTANCE_BUSINESS_KEY_VALUE = "businessKey";
  public static final String SORT_BY_CASE_INSTANCE_CREATE_TIME_VALUE = "createTime";
  public static final String SORT_BY_CASE_INSTANCE_CLOSE_TIME_VALUE = "closeTime";
  public static final String SORT_BY_CASE_INSTANCE_DURATION_VALUE = "duration";
  private static final String SORT_BY_TENANT_ID = "tenantId";

  public static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_DEFINITION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_INSTANCE_BUSINESS_KEY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_INSTANCE_CREATE_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_INSTANCE_CLOSE_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_INSTANCE_DURATION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  public String caseInstanceId;
  public Set<String> caseInstanceIds;
  public String caseDefinitionId;
  public String caseDefinitionKey;
  public String caseDefinitionName;
  public String caseDefinitionNameLike;
  public List<String> caseDefinitionKeyNotIn;
  public String caseInstanceBusinessKey;
  public String caseInstanceBusinessKeyLike;
  public String superCaseInstanceId;
  public String subCaseInstanceId;
  private String superProcessInstanceId;
  private String subProcessInstanceId;
  private List<String> tenantIds;
  private Boolean withoutTenantId;
  public String createdBy;
  public List<String> caseActivityIdIn;

  public Date createdBefore;
  public Date createdAfter;
  public Date closedBefore;
  public Date closedAfter;

  public Boolean active;
  public Boolean completed;
  public Boolean terminated;
  public Boolean closed;
  public Boolean notClosed;

  protected List<VariableQueryParameterDto> variables;

  public HistoricCaseInstanceQueryDto() {}

  public HistoricCaseInstanceQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  @CamundaQueryParam(value = "caseInstanceIds", converter = StringSetConverter.class)
  public void setCaseInstanceIds(Set<String> caseInstanceIds) {
    this.caseInstanceIds = caseInstanceIds;
  }

  @CamundaQueryParam("caseDefinitionId")
  public void setCaseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
  }

  @CamundaQueryParam("caseDefinitionName")
  public void setCaseDefinitionName(String caseDefinitionName) {
    this.caseDefinitionName = caseDefinitionName;
  }

  @CamundaQueryParam("caseDefinitionNameLike")
  public void setCaseDefinitionNameLike(String caseDefinitionNameLike) {
    this.caseDefinitionNameLike = caseDefinitionNameLike;
  }

  @CamundaQueryParam("caseDefinitionKey")
  public void setCaseDefinitionKey(String caseDefinitionKey) {
    this.caseDefinitionKey = caseDefinitionKey;
  }

  @CamundaQueryParam(value = "caseDefinitionKeyNotIn", converter = StringListConverter.class)
  public void setCaseDefinitionKeyNotIn(List<String> caseDefinitionKeys) {
    this.caseDefinitionKeyNotIn = caseDefinitionKeys;
  }

  @CamundaQueryParam("caseInstanceBusinessKey")
  public void setCaseInstanceBusinessKey(String caseInstanceBusinessKey) {
    this.caseInstanceBusinessKey = caseInstanceBusinessKey;
  }

  @CamundaQueryParam("caseInstanceBusinessKeyLike")
  public void setCaseInstanceBusinessKeyLike(String caseInstanceBusinessKeyLike) {
    this.caseInstanceBusinessKeyLike = caseInstanceBusinessKeyLike;
  }

  @CamundaQueryParam("superCaseInstanceId")
  public void setSuperCaseInstanceId(String superCaseInstanceId) {
    this.superCaseInstanceId = superCaseInstanceId;
  }

  @CamundaQueryParam("subCaseInstanceId")
  public void setSubCaseInstanceId(String subCaseInstanceId) {
    this.subCaseInstanceId = subCaseInstanceId;
  }

  @CamundaQueryParam("superProcessInstanceId")
  public void setSuperProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
  }

  @CamundaQueryParam("subProcessInstanceId")
  public void setSubProcessInstanceId(String subProcessInstanceId) {
    this.subProcessInstanceId = subProcessInstanceId;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @CamundaQueryParam("createdBy")
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @CamundaQueryParam(value = "createdBefore", converter = DateConverter.class)
  public void setCreatedBefore(Date createdBefore) {
    this.createdBefore = createdBefore;
  }

  @CamundaQueryParam(value = "createdAfter", converter = DateConverter.class)
  public void setCreatedAfter(Date createdAfter) {
    this.createdAfter = createdAfter;
  }

  @CamundaQueryParam(value = "closedBefore", converter = DateConverter.class)
  public void setClosedBefore(Date closedBefore) {
    this.closedBefore = closedBefore;
  }

  @CamundaQueryParam(value = "closedAfter", converter = DateConverter.class)
  public void setClosedAfter(Date closedAfter) {
    this.closedAfter = closedAfter;
  }

  @CamundaQueryParam(value = "active", converter = BooleanConverter.class)
  public void setActive(Boolean active) {
    this.active = active;
  }

  @CamundaQueryParam(value = "completed", converter = BooleanConverter.class)
  public void setCompleted(Boolean completed) {
    this.completed = completed;
  }

  @CamundaQueryParam(value = "terminated", converter = BooleanConverter.class)
  public void setTerminated(Boolean terminated) {
    this.terminated = terminated;
  }

  @CamundaQueryParam(value = "closed", converter = BooleanConverter.class)
  public void setClosed(Boolean closed) {
    this.closed = closed;
  }

  @CamundaQueryParam(value = "notClosed", converter = BooleanConverter.class)
  public void setNotClosed(Boolean notClosed) {
    this.notClosed = notClosed;
  }

  @CamundaQueryParam(value = "variables", converter = VariableListConverter.class)
  public void setVariables(List<VariableQueryParameterDto> variables) {
    this.variables = variables;
  }

  @CamundaQueryParam(value = "caseActivityIdIn", converter = StringListConverter.class)
  public void setCaseActivityIdIn(List<String> caseActivityIdIn) {
    this.caseActivityIdIn = caseActivityIdIn;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected HistoricCaseInstanceQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricCaseInstanceQuery();
  }

  @Override
  protected void applyFilters(HistoricCaseInstanceQuery query) {

    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }
    if (caseInstanceIds != null) {
      query.caseInstanceIds(caseInstanceIds);
    }
    if (caseDefinitionId != null) {
      query.caseDefinitionId(caseDefinitionId);
    }
    if (caseDefinitionKey != null) {
      query.caseDefinitionKey(caseDefinitionKey);
    }
    if (caseDefinitionName != null) {
      query.caseDefinitionName(caseDefinitionName);
    }
    if (caseDefinitionNameLike != null) {
      query.caseDefinitionNameLike(caseDefinitionNameLike);
    }
    if (caseDefinitionKeyNotIn != null) {
      query.caseDefinitionKeyNotIn(caseDefinitionKeyNotIn);
    }
    if (caseInstanceBusinessKey != null) {
      query.caseInstanceBusinessKey(caseInstanceBusinessKey);
    }
    if (caseInstanceBusinessKeyLike != null) {
      query.caseInstanceBusinessKeyLike(caseInstanceBusinessKeyLike);
    }
    if (superCaseInstanceId != null) {
      query.superCaseInstanceId(superCaseInstanceId);
    }
    if (subCaseInstanceId != null) {
      query.subCaseInstanceId(subCaseInstanceId);
    }
    if (superProcessInstanceId != null) {
      query.superProcessInstanceId(superProcessInstanceId);
    }
    if (subProcessInstanceId != null) {
      query.subProcessInstanceId(subProcessInstanceId);
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (createdBy != null) {
      query.createdBy(createdBy);
    }
    if (createdBefore != null) {
      query.createdBefore(createdBefore);
    }
    if (createdAfter != null) {
      query.createdAfter(createdAfter);
    }
    if (closedBefore != null) {
      query.closedBefore(closedBefore);
    }
    if (closedAfter != null) {
      query.closedAfter(closedAfter);
    }
    if (active != null && active) {
      query.active();
    }
    if (completed != null && completed) {
      query.completed();
    }
    if (terminated != null && terminated) {
      query.terminated();
    }
    if (closed != null && closed) {
      query.closed();
    }
    if (notClosed != null && notClosed) {
      query.notClosed();
    }
    if (caseActivityIdIn != null && !caseActivityIdIn.isEmpty()) {
      query.caseActivityIdIn(caseActivityIdIn.toArray(new String[caseActivityIdIn.size()]));
    }
    if (variables != null) {
      for (VariableQueryParameterDto variableQueryParam : variables) {
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
  }

  @Override
  protected void applySortBy(HistoricCaseInstanceQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_CASE_INSTANCE_ID_VALUE)) {
      query.orderByCaseInstanceId();
    } else if (sortBy.equals(SORT_BY_CASE_DEFINITION_ID_VALUE)) {
      query.orderByCaseDefinitionId();
    } else if (sortBy.equals(SORT_BY_CASE_INSTANCE_BUSINESS_KEY_VALUE)) {
      query.orderByCaseInstanceBusinessKey();
    } else if (sortBy.equals(SORT_BY_CASE_INSTANCE_CREATE_TIME_VALUE)) {
      query.orderByCaseInstanceCreateTime();
    } else if (sortBy.equals(SORT_BY_CASE_INSTANCE_CLOSE_TIME_VALUE)) {
      query.orderByCaseInstanceCloseTime();
    } else if (sortBy.equals(SORT_BY_CASE_INSTANCE_DURATION_VALUE)) {
      query.orderByCaseInstanceDuration();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
