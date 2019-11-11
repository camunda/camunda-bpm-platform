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

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricCaseActivityInstanceQueryDto extends AbstractQueryDto<HistoricCaseActivityInstanceQuery> {

  protected static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_ID_VALUE = "caseActivityInstanceId";
  protected static final String SORT_BY_CASE_INSTANCE_ID_VALUE = "caseInstanceId";
  protected static final String SORT_BY_CASE_EXECUTION_ID_VALUE = "caseExecutionId";
  protected static final String SORT_BY_CASE_ACTIVITY_ID_VALUE = "caseActivityId";
  protected static final String SORT_BY_CASE_ACTIVITY_NAME_VALUE = "caseActivityName";
  protected static final String SORT_BY_CASE_ACTIVITY_TYPE_VALUE = "caseActivityType";
  protected static final String SORT_BY_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATE_TIME_VALUE = "createTime";
  protected static final String SORT_BY_HISTORIC_CASE_ACTIVITY_INSTANCE_END_TIME_VALUE = "endTime";
  protected static final String SORT_BY_HISTORIC_CASE_ACTIVITY_INSTANCE_DURATION_VALUE = "duration";
  protected static final String SORT_BY_CASE_DEFINITION_ID_VALUE = "caseDefinitionId";
  protected static final String SORT_BY_TENANT_ID = "tenantId";

  protected static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_EXECUTION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_ACTIVITY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_ACTIVITY_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_ACTIVITY_TYPE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATE_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_HISTORIC_CASE_ACTIVITY_INSTANCE_END_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_HISTORIC_CASE_ACTIVITY_INSTANCE_DURATION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_DEFINITION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected String caseActivityInstanceId;
  protected List<String> caseActivityInstanceIds;
  protected String caseInstanceId;
  protected String caseDefinitionId;
  protected String caseExecutionId;
  protected String caseActivityId;
  protected List<String> caseActivityIds;
  protected String caseActivityName;
  protected String caseActivityType;
  protected Date createdBefore;
  protected Date createdAfter;
  protected Date endedBefore;
  protected Date endedAfter;
  protected Boolean required;
  protected Boolean finished;
  protected Boolean unfinished;
  protected Boolean available;
  protected Boolean enabled;
  protected Boolean disabled;
  protected Boolean active;
  protected Boolean completed;
  protected Boolean terminated;
  protected List<String> tenantIds;
  protected Boolean withoutTenantId;

  public HistoricCaseActivityInstanceQueryDto() {
  }

  public HistoricCaseActivityInstanceQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("caseActivityInstanceId")
  public void setCaseActivityInstanceId(String caseActivityInstanceId) {
    this.caseActivityInstanceId = caseActivityInstanceId;
  }

  @CamundaQueryParam(value = "caseActivityInstanceIdIn", converter = StringListConverter.class)
  public void setCaseActivityInstanceIdIn(List<String> caseActivityInstanceIds) {
    this.caseActivityInstanceIds = caseActivityInstanceIds;
  }

  @CamundaQueryParam("caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  @CamundaQueryParam("caseDefinitionId")
  public void setCaseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
  }

  @CamundaQueryParam("caseExecutionId")
  public void setCaseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
  }

  @CamundaQueryParam("caseActivityId")
  public void setCaseActivityId(String caseActivityId) {
    this.caseActivityId = caseActivityId;
  }

  @CamundaQueryParam(value = "caseActivityIdIn", converter = StringListConverter.class)
  public void setCaseActivityIdIn(List<String> caseActivityIds) {
    this.caseActivityIds = caseActivityIds;
  }

  @CamundaQueryParam("caseActivityName")
  public void setCaseActivityName(String caseActivityName) {
    this.caseActivityName = caseActivityName;
  }

  @CamundaQueryParam("caseActivityType")
  public void setCaseActivityType(String caseActivityType) {
    this.caseActivityType = caseActivityType;
  }

  @CamundaQueryParam(value = "createdBefore", converter = DateConverter.class)
  public void setCreatedBefore(Date createdBefore) {
    this.createdBefore = createdBefore;
  }

  @CamundaQueryParam(value = "createdAfter", converter = DateConverter.class)
  public void setCreatedAfter(Date createdAfter) {
    this.createdAfter = createdAfter;
  }

  @CamundaQueryParam(value = "endedBefore", converter = DateConverter.class)
  public void setEndedBefore(Date endedBefore) {
    this.endedBefore = endedBefore;
  }

  @CamundaQueryParam(value = "endedAfter", converter = DateConverter.class)
  public void setEndedAfter(Date endedAfter) {
    this.endedAfter = endedAfter;
  }

  @CamundaQueryParam(value = "required", converter = BooleanConverter.class)
  public void setRequired(Boolean required) {
    this.required = required;
  }

  @CamundaQueryParam(value = "finished", converter = BooleanConverter.class)
  public void setFinished(Boolean finished) {
    this.finished = finished;
  }

  @CamundaQueryParam(value = "unfinished", converter = BooleanConverter.class)
  public void setUnfinished(Boolean unfinished) {
    this.unfinished = unfinished;
  }

  @CamundaQueryParam(value = "available", converter = BooleanConverter.class)
  public void setAvailable(Boolean available) {
    this.available = available;
  }

  @CamundaQueryParam(value = "enabled", converter = BooleanConverter.class)
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  @CamundaQueryParam(value = "disabled", converter = BooleanConverter.class)
  public void setDisabled(Boolean disabled) {
    this.disabled = disabled;
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

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected HistoricCaseActivityInstanceQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricCaseActivityInstanceQuery();
  }

  @Override
  protected void applyFilters(HistoricCaseActivityInstanceQuery query) {
    if (caseActivityInstanceId != null) {
      query.caseActivityInstanceId(caseActivityInstanceId);
    }
    if (caseActivityInstanceIds != null && !caseActivityInstanceIds.isEmpty()) {
      query.caseActivityInstanceIdIn(caseActivityInstanceIds.toArray(new String[caseActivityInstanceIds.size()]));
    }
    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }
    if (caseDefinitionId != null) {
      query.caseDefinitionId(caseDefinitionId);
    }
    if (caseExecutionId != null) {
      query.caseExecutionId(caseExecutionId);
    }
    if (caseActivityId != null) {
      query.caseActivityId(caseActivityId);
    }
    if (caseActivityIds != null && !caseActivityIds.isEmpty()) {
      query.caseActivityIdIn(caseActivityIds.toArray(new String[caseActivityIds.size()]));
    }
    if (caseActivityName != null) {
      query.caseActivityName(caseActivityName);
    }
    if (caseActivityType != null) {
      query.caseActivityType(caseActivityType);
    }
    if (createdBefore != null) {
      query.createdBefore(createdBefore);
    }
    if (createdAfter != null) {
      query.createdAfter(createdAfter);
    }
    if (endedBefore != null) {
      query.endedBefore(endedBefore);
    }
    if (endedAfter != null) {
      query.endedAfter(endedAfter);
    }
    if (required != null && required) {
      query.required();
    }
    if (finished != null && finished) {
      query.ended();
    }
    if (unfinished != null && unfinished) {
      query.notEnded();
    }
    if (available != null && available) {
      query.available();
    }
    if (enabled != null && enabled) {
      query.enabled();
    }
    if (disabled != null && disabled) {
      query.disabled();
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
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
  }

  protected void applySortBy(HistoricCaseActivityInstanceQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_ID_VALUE)) {
      query.orderByHistoricCaseActivityInstanceId();
    } else if (sortBy.equals(SORT_BY_CASE_INSTANCE_ID_VALUE)) {
      query.orderByCaseInstanceId();
    } else if (sortBy.equals(SORT_BY_CASE_EXECUTION_ID_VALUE)) {
      query.orderByCaseExecutionId();
    } else if (sortBy.equals(SORT_BY_CASE_ACTIVITY_ID_VALUE)) {
      query.orderByCaseActivityId();
    } else if (sortBy.equals(SORT_BY_CASE_ACTIVITY_NAME_VALUE)) {
      query.orderByCaseActivityName();
    } else if (sortBy.equals(SORT_BY_CASE_ACTIVITY_TYPE_VALUE)) {
      query.orderByCaseActivityType();
    } else if (sortBy.equals(SORT_BY_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATE_TIME_VALUE)) {
      query.orderByHistoricCaseActivityInstanceCreateTime();
    } else if (sortBy.equals(SORT_BY_HISTORIC_CASE_ACTIVITY_INSTANCE_END_TIME_VALUE)) {
      query.orderByHistoricCaseActivityInstanceEndTime();
    } else if (sortBy.equals(SORT_BY_HISTORIC_CASE_ACTIVITY_INSTANCE_DURATION_VALUE)) {
      query.orderByHistoricCaseActivityInstanceDuration();
    } else if (sortBy.equals(SORT_BY_CASE_DEFINITION_ID_VALUE)) {
      query.orderByCaseDefinitionId();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
