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
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricActivityInstanceQueryDto extends AbstractQueryDto<HistoricActivityInstanceQuery> {

  protected static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_ID_VALUE = "activityInstanceId";
  protected static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "instanceId";
  protected static final String SORT_BY_EXECUTION_ID_VALUE = "executionId";
  protected static final String SORT_BY_ACTIVITY_ID_VALUE = "activityId";
  protected static final String SORT_BY_ACTIVITY_NAME_VALUE = "activityName";
  protected static final String SORT_BY_ACTIVITY_TYPE_VALUE = "activityType";
  protected static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_START_TIME_VALUE = "startTime";
  protected static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_END_TIME_VALUE = "endTime";
  protected static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_DURATION_VALUE = "duration";
  protected static final String SORT_BY_PROCESS_DEFINITION_ID_VALUE = "definitionId";
  protected static final String SORT_PARTIALLY_BY_OCCURRENCE = "occurrence";
  protected static final String SORT_BY_TENANT_ID = "tenantId";

  protected static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXECUTION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_TYPE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_START_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_END_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_DURATION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_PARTIALLY_BY_OCCURRENCE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected String activityInstanceId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String executionId;
  protected String activityId;
  protected String activityName;
  protected String activityNameLike;
  protected String activityType;
  protected String taskAssignee;
  protected Boolean finished;
  protected Boolean unfinished;
  protected Boolean completeScope;
  protected Boolean canceled;
  protected Date startedBefore;
  protected Date startedAfter;
  protected Date finishedBefore;
  protected Date finishedAfter;
  protected List<String> tenantIds;
  protected Boolean withoutTenantId;

  public HistoricActivityInstanceQueryDto() {
  }

  public HistoricActivityInstanceQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("activityInstanceId")
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("executionId")
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @CamundaQueryParam("activityId")
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  @CamundaQueryParam("activityName")
  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }

  @CamundaQueryParam("activityNameLike")
  public void setActivityNameLike(String activityNameLike) {
    this.activityNameLike = activityNameLike;
  }

  @CamundaQueryParam("activityType")
  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  @CamundaQueryParam("taskAssignee")
  public void setTaskAssignee(String taskAssignee) {
    this.taskAssignee = taskAssignee;
  }

  @CamundaQueryParam(value = "finished", converter = BooleanConverter.class)
  public void setFinished(Boolean finished) {
    this.finished = finished;
  }

  @CamundaQueryParam(value = "unfinished", converter = BooleanConverter.class)
  public void setUnfinished(Boolean unfinished) {
    this.unfinished = unfinished;
  }

  @CamundaQueryParam(value = "completeScope", converter = BooleanConverter.class)
  public void setCompleteScope(Boolean completeScope) {
    this.completeScope = completeScope;
  }

  @CamundaQueryParam(value = "canceled", converter = BooleanConverter.class)
  public void setCanceled(Boolean canceled) {
    this.canceled = canceled;
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
  protected HistoricActivityInstanceQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricActivityInstanceQuery();
  }

  @Override
  protected void applyFilters(HistoricActivityInstanceQuery query) {
    if (activityInstanceId != null) {
      query.activityInstanceId(activityInstanceId);
    }
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (executionId != null) {
      query.executionId(executionId);
    }
    if (activityId != null) {
      query.activityId(activityId);
    }
    if (activityName != null) {
      query.activityName(activityName);
    }
    if (activityNameLike != null) {
      query.activityNameLike(activityNameLike);
    }
    if (activityType != null) {
      query.activityType(activityType);
    }
    if (taskAssignee != null) {
      query.taskAssignee(taskAssignee);
    }
    if (finished != null && finished) {
      query.finished();
    }
    if (unfinished != null && unfinished) {
      query.unfinished();
    }
    if (completeScope != null && completeScope) {
      query.completeScope();
    }
    if (canceled != null && canceled) {
      query.canceled();
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
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
  }

  @Override
  protected void applySortBy(HistoricActivityInstanceQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_ID_VALUE)) {
      query.orderByHistoricActivityInstanceId();
    } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
      query.orderByProcessInstanceId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_ID_VALUE)) {
      query.orderByProcessDefinitionId();
    } else if (sortBy.equals(SORT_BY_EXECUTION_ID_VALUE)) {
      query.orderByExecutionId();
    } else if (sortBy.equals(SORT_BY_ACTIVITY_ID_VALUE)) {
      query.orderByActivityId();
    } else if (sortBy.equals(SORT_BY_ACTIVITY_NAME_VALUE)) {
      query.orderByActivityName();
    } else if (sortBy.equals(SORT_BY_ACTIVITY_TYPE_VALUE)) {
      query.orderByActivityType();
    } else if (sortBy.equals(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_START_TIME_VALUE)) {
      query.orderByHistoricActivityInstanceStartTime();
    } else if (sortBy.equals(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_END_TIME_VALUE)) {
      query.orderByHistoricActivityInstanceEndTime();
    } else if (sortBy.equals(SORT_BY_HISTORIC_ACTIVITY_INSTANCE_DURATION_VALUE)) {
      query.orderByHistoricActivityInstanceDuration();
    } else if (sortBy.equals(SORT_PARTIALLY_BY_OCCURRENCE)) {
      query.orderPartiallyByOccurrence();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
