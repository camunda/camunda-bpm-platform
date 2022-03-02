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
package org.camunda.bpm.engine.rest.dto.history;

import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricVariableInstanceQueryDto extends AbstractQueryDto<HistoricVariableInstanceQuery> {

  private static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "instanceId";
  private static final String SORT_BY_VARIABLE_NAME_VALUE = "variableName";
  private static final String SORT_BY_TENANT_ID = "tenantId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<>();
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_VARIABLE_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String caseInstanceId;
  protected String variableName;
  protected String variableNameLike;
  protected Object variableValue;
  protected Boolean variableValuesIgnoreCase;
  protected Boolean variableNamesIgnoreCase;
  protected String[] variableTypeIn;
  protected String[] executionIdIn;
  protected String[] taskIdIn;
  protected String[] activityInstanceIdIn;
  protected String[] caseExecutionIdIn;
  protected String[] caseActivityIdIn;
  protected String[] processInstanceIdIn;
  protected List<String> tenantIds;
  protected Boolean withoutTenantId;
  protected boolean includeDeleted;
  protected String[] variableNameIn;

  public HistoricVariableInstanceQueryDto() {
  }

  public HistoricVariableInstanceQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @CamundaQueryParam("caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
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

  @CamundaQueryParam(value="variableTypeIn", converter = StringArrayConverter.class)
  public void setVariableTypeIn(String[] variableTypeIn) {
    this.variableTypeIn = variableTypeIn;
  }

  @CamundaQueryParam(value="variableValuesIgnoreCase", converter = BooleanConverter.class)
  public void setVariableValuesIgnoreCase(Boolean variableValuesIgnoreCase) {
    this.variableValuesIgnoreCase = variableValuesIgnoreCase;
  }

  @CamundaQueryParam(value="variableNamesIgnoreCase", converter = BooleanConverter.class)
  public void setVariableNamesIgnoreCase(Boolean variableNamesIgnoreCase) {
    this.variableNamesIgnoreCase = variableNamesIgnoreCase;
  }

  @CamundaQueryParam(value="executionIdIn", converter = StringArrayConverter.class)
  public void setExecutionIdIn(String[] executionIdIn) {
    this.executionIdIn = executionIdIn;
  }

  @CamundaQueryParam(value="taskIdIn", converter = StringArrayConverter.class)
  public void setTaskIdIn(String[] taskIdIn) {
    this.taskIdIn = taskIdIn;
  }

  @CamundaQueryParam(value="processInstanceIdIn", converter = StringArrayConverter.class)
  public void setProcessInstanceIdIn(String[] processInstanceIdIn) {
    this.processInstanceIdIn = processInstanceIdIn;
  }

  @CamundaQueryParam(value="activityInstanceIdIn", converter = StringArrayConverter.class)
  public void setActivityInstanceIdIn(String[] activityInstanceIdIn) {
    this.activityInstanceIdIn = activityInstanceIdIn;
  }

  @CamundaQueryParam(value="caseExecutionIdIn", converter = StringArrayConverter.class)
  public void setCaseExecutionIdIn(String[] caseExecutionIdIn) {
    this.caseExecutionIdIn = caseExecutionIdIn;
  }

  @CamundaQueryParam(value="caseActivityIdIn", converter = StringArrayConverter.class)
  public void setCaseActivityIdIn(String[] caseActivityIdIn) {
    this.caseActivityIdIn = caseActivityIdIn;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  public boolean isIncludeDeleted() {
    return includeDeleted;
  }

  @CamundaQueryParam(value = "includeDeleted", converter = BooleanConverter.class)
  public void setIncludeDeleted(boolean includeDeleted) {
    this.includeDeleted = includeDeleted;
  }

  @CamundaQueryParam(value = "variableNameIn", converter = StringArrayConverter.class)
  public void setVariableNameIn(String[] variableNameIn) {
    this.variableNameIn = variableNameIn;
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
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }
    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }
    if (variableName != null) {
      query.variableName(variableName);
    }
    if (variableNameLike != null) {
      query.variableNameLike(variableNameLike);
    }
    if (variableValue != null) {
      if (variableName != null) {
        query.variableValueEquals(variableName, variableValue);
      } else {
        throw new InvalidRequestException(Status.BAD_REQUEST,
            "Only a single variable value parameter specified: variable name and value are required to be able to query after a specific variable value.");
      }
    }
    if (variableTypeIn != null && variableTypeIn.length > 0) {
      query.variableTypeIn(variableTypeIn);
    }
    if (TRUE.equals(variableNamesIgnoreCase)) {
      query.matchVariableNamesIgnoreCase();
    }
    if (TRUE.equals(variableValuesIgnoreCase)) {
      query.matchVariableValuesIgnoreCase();
    }
    if (executionIdIn != null && executionIdIn.length > 0) {
      query.executionIdIn(executionIdIn);
    }
    if (taskIdIn != null && taskIdIn.length > 0) {
      query.taskIdIn(taskIdIn);
    }
    if (processInstanceIdIn != null && processInstanceIdIn.length > 0) {
      query.processInstanceIdIn(processInstanceIdIn);
    }
    if (activityInstanceIdIn != null && activityInstanceIdIn.length > 0) {
      query.activityInstanceIdIn(activityInstanceIdIn);
    }
    if (caseExecutionIdIn != null && caseExecutionIdIn.length > 0) {
      query.caseExecutionIdIn(caseExecutionIdIn);
    }
    if (caseActivityIdIn != null && caseActivityIdIn.length > 0) {
      query.caseActivityIdIn(caseActivityIdIn);
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (includeDeleted) {
      query.includeDeleted();
    }
    if (variableNameIn != null && variableNameIn.length > 0) {
      query.variableNameIn(variableNameIn);
    }
  }

  @Override
  protected void applySortBy(HistoricVariableInstanceQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
      query.orderByProcessInstanceId();
    } else if (sortBy.equals(SORT_BY_VARIABLE_NAME_VALUE)) {
      query.orderByVariableName();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }
}
