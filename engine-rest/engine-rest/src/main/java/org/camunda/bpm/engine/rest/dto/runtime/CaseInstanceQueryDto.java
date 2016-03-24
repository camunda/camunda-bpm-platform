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

import static java.lang.Boolean.TRUE;

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
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Roman Smirnov
 *
 */
public class CaseInstanceQueryDto extends AbstractQueryDto<CaseInstanceQuery> {

  protected static final String SORT_BY_INSTANCE_ID_VALUE = "caseInstanceId";
  protected static final String SORT_BY_DEFINITION_KEY_VALUE = "caseDefinitionKey";
  protected static final String SORT_BY_DEFINITION_ID_VALUE = "caseDefinitionId";
  protected static final String SORT_BY_TENANT_ID = "tenantId";

  protected static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DEFINITION_KEY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DEFINITION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected String caseInstanceId;
  protected String businessKey;
  protected String caseDefinitionKey;
  protected String caseDefinitionId;
  protected String deploymentId;
  protected String superProcessInstance;
  protected String subProcessInstance;
  protected String superCaseInstance;
  protected String subCaseInstance;
  protected List<String> tenantIds;
  protected Boolean withoutTenantId;
  protected Boolean active;
  protected Boolean completed;
  protected Boolean terminated;

  protected List<VariableQueryParameterDto> variables;

  public CaseInstanceQueryDto() {
  }

  public CaseInstanceQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  @CamundaQueryParam("businessKey")
  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  @CamundaQueryParam("caseDefinitionKey")
  public void setCaseDefinitionKey(String caseDefinitionKey) {
    this.caseDefinitionKey = caseDefinitionKey;
  }

  @CamundaQueryParam("caseDefinitionId")
  public void setCaseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
  }

  @CamundaQueryParam("deploymentId")
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  @CamundaQueryParam("superProcessInstance")
  public void setSuperProcessInstance(String superProcessInstance) {
    this.superProcessInstance = superProcessInstance;
  }

  @CamundaQueryParam("subProcessInstance")
  public void setSubProcessInstance(String subProcessInstance) {
    this.subProcessInstance = subProcessInstance;
  }

  @CamundaQueryParam("superCaseInstance")
  public void setSuperCaseInstance(String superCaseInstance) {
    this.superCaseInstance = superCaseInstance;
  }

  @CamundaQueryParam("subCaseInstance")
  public void setSubCaseInstance(String subCaseInstance) {
    this.subCaseInstance = subCaseInstance;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
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

  @CamundaQueryParam(value = "variables", converter = VariableListConverter.class)
  public void setVariables(List<VariableQueryParameterDto> variables) {
    this.variables = variables;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected CaseInstanceQuery createNewQuery(ProcessEngine engine) {
    return engine.getCaseService().createCaseInstanceQuery();
  }

  @Override
  protected void applyFilters(CaseInstanceQuery query) {
    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }
    if (businessKey != null) {
      query.caseInstanceBusinessKey(businessKey);
    }
    if (caseDefinitionKey != null) {
      query.caseDefinitionKey(caseDefinitionKey);
    }
    if (caseDefinitionId != null) {
      query.caseDefinitionId(caseDefinitionId);
    }
    if (deploymentId != null) {
      query.deploymentId(deploymentId );
    }
    if (superProcessInstance != null) {
      query.superProcessInstanceId(superProcessInstance);
    }
    if (subProcessInstance != null) {
      query.subProcessInstanceId(subProcessInstance);
    }
    if (superCaseInstance != null) {
      query.superCaseInstanceId(superCaseInstance);
    }
    if (subCaseInstance != null) {
      query.subCaseInstanceId(subCaseInstance);
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (active != null && active == true) {
      query.active();
    }
    if (completed != null && completed == true) {
      query.completed();
    }
    if (terminated != null && terminated == true) {
      query.terminated();
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
  protected void applySortBy(CaseInstanceQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_INSTANCE_ID_VALUE)) {
      query.orderByCaseInstanceId();
    } else if (sortBy.equals(SORT_BY_DEFINITION_KEY_VALUE)) {
      query.orderByCaseDefinitionKey();
    } else if (sortBy.equals(SORT_BY_DEFINITION_ID_VALUE)) {
      query.orderByCaseDefinitionId();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
