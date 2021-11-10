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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringSetConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricProcessInstanceQueryDto extends AbstractQueryDto<HistoricProcessInstanceQuery> {

  private static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "instanceId";
  private static final String SORT_BY_PROCESS_DEFINITION_ID_VALUE = "definitionId";
  private static final String SORT_BY_PROCESS_INSTANCE_BUSINESS_KEY_VALUE = "businessKey";
  private static final String SORT_BY_PROCESS_INSTANCE_START_TIME_VALUE = "startTime";
  private static final String SORT_BY_PROCESS_INSTANCE_END_TIME_VALUE = "endTime";
  private static final String SORT_BY_PROCESS_INSTANCE_DURATION_VALUE = "duration";
  private static final String SORT_BY_PROCESS_DEFINITION_KEY_VALUE = "definitionKey";
  private static final String SORT_BY_PROCESS_DEFINITION_NAME_VALUE = "definitionName";
  private static final String SORT_BY_PROCESS_DEFINITION_VERSION_VALUE = "definitionVersion";

  private static final String SORT_BY_TENANT_ID = "tenantId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<>();
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_BUSINESS_KEY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_START_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_END_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_DURATION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_KEY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_VERSION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  private String processInstanceId;
  private Set<String> processInstanceIds;
  private String processDefinitionId;
  private String processDefinitionKey;
  private List<String> processDefinitionKeys;
  private String processDefinitionName;
  private String processDefinitionNameLike;
  private List<String> processDefinitionKeyNotIn;
  private String processInstanceBusinessKey;
  private List<String> processInstanceBusinessKeyIn;
  private String processInstanceBusinessKeyLike;
  private Boolean rootProcessInstances;
  private Boolean finished;
  private Boolean unfinished;
  private Boolean withIncidents;
  private Boolean withRootIncidents;
  private String incidentType;
  private String incidentStatus;
  private String incidentMessage;
  private String incidentMessageLike;
  private Date startedBefore;
  private Date startedAfter;
  private Date finishedBefore;
  private Date finishedAfter;
  private Date executedActivityAfter;
  private Date executedActivityBefore;
  private Date executedJobAfter;
  private Date executedJobBefore;
  private String startedBy;
  private String superProcessInstanceId;
  private String subProcessInstanceId;
  private String superCaseInstanceId;
  private String subCaseInstanceId;
  private String caseInstanceId;
  private List<String> tenantIds;
  private Boolean withoutTenantId;
  private List<String> executedActivityIdIn;
  private List<String> activeActivityIdIn;
  private Boolean active;
  private Boolean suspended;
  private Boolean completed;
  private Boolean externallyTerminated;
  private Boolean internallyTerminated;

  private List<VariableQueryParameterDto> variables;

  protected Boolean variableNamesIgnoreCase;
  protected Boolean variableValuesIgnoreCase;

  private List<HistoricProcessInstanceQueryDto> orQueries;

  public HistoricProcessInstanceQueryDto() {}

  public HistoricProcessInstanceQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("orQueries")
  public void setOrQueries(List<HistoricProcessInstanceQueryDto> orQueries) {
    this.orQueries = orQueries;
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam(value = "processInstanceIds", converter = StringSetConverter.class)
  public void setProcessInstanceIds(Set<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("processDefinitionName")
  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  @CamundaQueryParam("processDefinitionNameLike")
  public void setProcessDefinitionNameLike(String processDefinitionNameLike) {
    this.processDefinitionNameLike = processDefinitionNameLike;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @CamundaQueryParam(value = "processDefinitionKeyIn", converter = StringListConverter.class)
  public void setProcessDefinitionKeyIn(List<String> processDefinitionKeys) {
    this.processDefinitionKeys = processDefinitionKeys;
  }

  @CamundaQueryParam(value = "processDefinitionKeyNotIn", converter = StringListConverter.class)
  public void setProcessDefinitionKeyNotIn(List<String> processDefinitionKeys) {
    this.processDefinitionKeyNotIn = processDefinitionKeys;
  }

  @CamundaQueryParam("processInstanceBusinessKey")
  public void setProcessInstanceBusinessKey(String processInstanceBusinessKey) {
    this.processInstanceBusinessKey = processInstanceBusinessKey;
  }

  @CamundaQueryParam(value = "processInstanceBusinessKeyIn", converter = StringListConverter.class)
  public void setProcessInstanceBusinessKeyIn(List<String> processInstanceBusinessKeyIn) {
    this.processInstanceBusinessKeyIn = processInstanceBusinessKeyIn;
  }

  @CamundaQueryParam("processInstanceBusinessKeyLike")
  public void setProcessInstanceBusinessKeyLike(String processInstanceBusinessKeyLike) {
    this.processInstanceBusinessKeyLike = processInstanceBusinessKeyLike;
  }

  @CamundaQueryParam(value = "rootProcessInstances", converter = BooleanConverter.class)
  public void setRootProcessInstances(Boolean rootProcessInstances) {
    this.rootProcessInstances = rootProcessInstances;
  }

  @CamundaQueryParam(value = "finished", converter = BooleanConverter.class)
  public void setFinished(Boolean finished) {
    this.finished = finished;
  }

  @CamundaQueryParam(value = "unfinished", converter = BooleanConverter.class)
  public void setUnfinished(Boolean unfinished) {
    this.unfinished = unfinished;
  }

  @CamundaQueryParam(value = "withIncidents", converter = BooleanConverter.class)
  public void setWithIncidents(Boolean withIncidents) {
    this.withIncidents = withIncidents;
  }

  @CamundaQueryParam(value = "withRootIncidents", converter = BooleanConverter.class)
  public void setWithRootIncidents(Boolean withRootIncidents) {
    this.withRootIncidents = withRootIncidents;
  }

  @CamundaQueryParam(value = "incidentStatus")
  public void setIncidentStatus(String status) {
    this.incidentStatus = status;
  }

  @CamundaQueryParam(value = "incidentMessage")
  public void setIncidentMessage(String incidentMessage) {
    this.incidentMessage = incidentMessage;
  }

  @CamundaQueryParam(value = "incidentMessageLike")
  public void setIncidentMessageLike(String incidentMessageLike) {
    this.incidentMessageLike = incidentMessageLike;
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

  @CamundaQueryParam("subProcessInstanceId")
  public void setSubProcessInstanceId(String subProcessInstanceId) {
    this.subProcessInstanceId = subProcessInstanceId;
  }

  @CamundaQueryParam("superCaseInstanceId")
  public void setSuperCaseInstanceId(String superCaseInstanceId) {
    this.superCaseInstanceId = superCaseInstanceId;
  }

  @CamundaQueryParam("subCaseInstanceId")
  public void setSubCaseInstanceId(String subCaseInstanceId) {
    this.subCaseInstanceId = subCaseInstanceId;
  }

  @CamundaQueryParam("caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  @CamundaQueryParam(value = "variables", converter = VariableListConverter.class)
  public void setVariables(List<VariableQueryParameterDto> variables) {
    this.variables = variables;
  }

  @CamundaQueryParam(value = "variableNamesIgnoreCase", converter = BooleanConverter.class)
  public void setVariableNamesIgnoreCase(Boolean variableNamesIgnoreCase) {
    this.variableNamesIgnoreCase = variableNamesIgnoreCase;
  }

  @CamundaQueryParam(value = "variableValuesIgnoreCase", converter = BooleanConverter.class)
  public void setVariableValuesIgnoreCase(Boolean variableValuesIgnoreCase) {
    this.variableValuesIgnoreCase = variableValuesIgnoreCase;
  }

  public String getIncidentType() {
    return incidentType;
  }

  @CamundaQueryParam(value = "incidentType")
  public void setIncidentType(String incidentType) {
    this.incidentType = incidentType;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @CamundaQueryParam(value = "executedActivityAfter", converter = DateConverter.class)
  public void setExecutedActivityAfter(Date executedActivityAfter) {
    this.executedActivityAfter = executedActivityAfter;
  }

  @CamundaQueryParam(value = "executedActivityIdIn", converter = StringListConverter.class)
  public void setExecutedActivityIdIn(List<String> executedActivityIds) {
    this.executedActivityIdIn = executedActivityIds;
  }

  @CamundaQueryParam(value = "executedActivityBefore", converter = DateConverter.class)
  public void setExecutedActivityBefore(Date executedActivityBefore) {
    this.executedActivityBefore = executedActivityBefore;
  }

  @CamundaQueryParam(value = "activeActivityIdIn", converter = StringListConverter.class)
  public void setActiveActivityIdIn(List<String> activeActivityIdIn) {
    this.activeActivityIdIn = activeActivityIdIn;
  }

  @CamundaQueryParam(value = "executedJobAfter", converter = DateConverter.class)
  public void setExecutedJobAfter(Date executedJobAfter) {
    this.executedJobAfter = executedJobAfter;
  }

  @CamundaQueryParam(value = "executedJobBefore", converter = DateConverter.class)
  public void setExecutedJobBefore(Date executedJobBefore) {
    this.executedJobBefore = executedJobBefore;
  }

  @CamundaQueryParam(value = "active", converter = BooleanConverter.class)
  public void setActive(Boolean active) {
    this.active = active;
  }

  @CamundaQueryParam(value = "suspended", converter = BooleanConverter.class)
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @CamundaQueryParam(value = "completed", converter = BooleanConverter.class)
  public void setCompleted(Boolean completed) {
    this.completed = completed;
  }

  @CamundaQueryParam(value = "externallyTerminated", converter = BooleanConverter.class)
  public void setExternallyTerminated(Boolean externallyTerminated) {
    this.externallyTerminated = externallyTerminated;
  }

  @CamundaQueryParam(value = "internallyTerminated", converter = BooleanConverter.class)
  public void setInternallyTerminated(Boolean internallyTerminated) {
    this.internallyTerminated = internallyTerminated;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected HistoricProcessInstanceQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricProcessInstanceQuery();
  }

  public List<HistoricProcessInstanceQueryDto> getOrQueries() {
    return orQueries;
  }

  @Override
  protected void applyFilters(HistoricProcessInstanceQuery query) {
    if (orQueries != null) {
      for (HistoricProcessInstanceQueryDto orQueryDto: orQueries) {
        HistoricProcessInstanceQueryImpl orQuery = new HistoricProcessInstanceQueryImpl();
        orQuery.setOrQueryActive();
        orQueryDto.applyFilters(orQuery);
        ((HistoricProcessInstanceQueryImpl) query).addOrQuery(orQuery);
      }
    }
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
    if (processDefinitionKeys != null && !processDefinitionKeys.isEmpty()) {
      query.processDefinitionKeyIn(processDefinitionKeys.toArray(new String[0]));
    }
    if (processDefinitionName != null) {
      query.processDefinitionName(processDefinitionName);
    }
    if (processDefinitionNameLike != null) {
      query.processDefinitionNameLike(processDefinitionNameLike);
    }
    if (processDefinitionKeyNotIn != null) {
      query.processDefinitionKeyNotIn(processDefinitionKeyNotIn);
    }
    if (processInstanceBusinessKey != null) {
      query.processInstanceBusinessKey(processInstanceBusinessKey);
    }
    if (processInstanceBusinessKeyIn != null && !processInstanceBusinessKeyIn.isEmpty()) {
      query.processInstanceBusinessKeyIn(processInstanceBusinessKeyIn.toArray(new String[0]));
    }
    if (processInstanceBusinessKeyLike != null) {
      query.processInstanceBusinessKeyLike(processInstanceBusinessKeyLike);
    }
    if (rootProcessInstances != null && rootProcessInstances) {
      query.rootProcessInstances();
    }
    if (finished != null && finished) {
      query.finished();
    }
    if (unfinished != null && unfinished) {
      query.unfinished();
    }
    if (withIncidents != null && withIncidents) {
      query.withIncidents();
    }
    if (withRootIncidents != null && withRootIncidents) {
      query.withRootIncidents();
    }
    if (incidentStatus != null) {
      query.incidentStatus(incidentStatus);
    }
    if (incidentType != null) {
      query.incidentType(incidentType);
    }
    if(incidentMessage != null) {
      query.incidentMessage(incidentMessage);
    }
    if(incidentMessageLike != null) {
      query.incidentMessageLike(incidentMessageLike);
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
    if (subProcessInstanceId != null) {
      query.subProcessInstanceId(subProcessInstanceId);
    }
    if (superCaseInstanceId != null) {
      query.superCaseInstanceId(superCaseInstanceId);
    }
    if (subCaseInstanceId != null) {
      query.subCaseInstanceId(subCaseInstanceId);
    }
    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[0]));
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if(TRUE.equals(variableNamesIgnoreCase)) {
      query.matchVariableNamesIgnoreCase();
    }
    if(TRUE.equals(variableValuesIgnoreCase)) {
      query.matchVariableValuesIgnoreCase();
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

    if (executedActivityAfter != null) {
      query.executedActivityAfter(executedActivityAfter);
    }

    if (executedActivityBefore != null) {
      query.executedActivityBefore(executedActivityBefore);
    }

    if (executedActivityIdIn != null && !executedActivityIdIn.isEmpty()) {
      query.executedActivityIdIn(executedActivityIdIn.toArray(new String[0]));
    }

    if (activeActivityIdIn != null && !activeActivityIdIn.isEmpty()) {
      query.activeActivityIdIn(activeActivityIdIn.toArray(new String[0]));
    }

    if (executedJobAfter != null) {
      query.executedJobAfter(executedJobAfter);
    }

    if (executedJobBefore != null) {
      query.executedJobBefore(executedJobBefore);
    }

    if (active != null && active) {
      query.active();
    }
    if (suspended != null && suspended) {
      query.suspended();
    }
    if (completed != null && completed) {
      query.completed();
    }
    if (externallyTerminated != null && externallyTerminated) {
      query.externallyTerminated();
    }
    if (internallyTerminated != null && internallyTerminated) {
      query.internallyTerminated();
    }
  }

  @Override
  protected void applySortBy(HistoricProcessInstanceQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
      query.orderByProcessInstanceId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_ID_VALUE)) {
      query.orderByProcessDefinitionId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_KEY_VALUE)) {
      query.orderByProcessDefinitionKey();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_NAME_VALUE)) {
      query.orderByProcessDefinitionName();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_VERSION_VALUE)) {
      query.orderByProcessDefinitionVersion();
    } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_BUSINESS_KEY_VALUE)) {
      query.orderByProcessInstanceBusinessKey();
    } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_START_TIME_VALUE)) {
      query.orderByProcessInstanceStartTime();
    } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_END_TIME_VALUE)) {
      query.orderByProcessInstanceEndTime();
    } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_DURATION_VALUE)) {
      query.orderByProcessInstanceDuration();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
