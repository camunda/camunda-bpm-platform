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

  private static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_ID_VALUE = "activityInstanceId";
  private static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "instanceId";
  private static final String SORT_BY_EXECUTION_ID_VALUE = "executionId";
  private static final String SORT_BY_ACTIVITY_ID_VALUE = "activityId";
  private static final String SORT_BY_ACTIVITY_NAME_VALUE = "activityName";
  private static final String SORT_BY_ACTIVITY_TYPE_VALUE = "activityType";
  private static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_START_TIME_VALUE = "startTime";
  private static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_END_TIME_VALUE = "endTime";
  private static final String SORT_BY_HISTORIC_ACTIVITY_INSTANCE_DURATION_VALUE = "duration";
  private static final String SORT_BY_PROCESS_DEFINITION_ID_VALUE = "definitionId";
  private static final String SORT_PARTIALLY_BY_OCCURRENCE = "occurrence";
  private static final String SORT_BY_TENANT_ID = "tenantId";

  private static final List<String> VALID_SORT_BY_VALUES;
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

  private String activityInstanceId;
  private String processInstanceId;
  private String processDefinitionId;
  private String executionId;
  private String activityId;
  private String activityName;
  private String activityType;
  private String taskAssignee;
  private Boolean finished;
  private Boolean unfinished;
  private Boolean completeScope;
  private Boolean canceled;
  private Date startedBefore;
  private Date startedAfter;
  private Date finishedBefore;
  private Date finishedAfter;
  private List<String> tenantIds;

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
