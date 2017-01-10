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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricExternalTaskLogQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.LongConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoricExternalTaskLogQueryDto extends AbstractQueryDto<HistoricExternalTaskLogQuery> {

  protected static final String SORT_BY_TIMESTAMP = "timestamp";
  protected static final String SORT_BY_EXTERNAL_TASK_ID = "externalTaskId";
  protected static final String SORT_BY_RETRIES = "retries";
  protected static final String SORT_BY_PRIORITY = "priority";
  protected static final String SORT_BY_TOPIC_NAME = "topicName";
  protected static final String SORT_BY_WORKER_ID = "workerId";
  protected static final String SORT_BY_ACTIVITY_ID = "activityId";
  protected static final String SORT_BY_ACTIVITY_INSTANCE_ID = "activityInstanceId";
  protected static final String SORT_BY_EXECUTION_ID = "executionId";
  protected static final String SORT_BY_PROCESS_INSTANCE_ID = "processInstanceId";
  protected static final String SORT_BY_PROCESS_DEFINITION_ID = "processDefinitionId";
  protected static final String SORT_BY_PROCESS_DEFINITION_KEY = "processDefinitionKey";
  protected static final String SORT_BY_TENANT_ID = "tenantId";

  protected static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();

    VALID_SORT_BY_VALUES.add(SORT_BY_TIMESTAMP);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXTERNAL_TASK_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_RETRIES);
    VALID_SORT_BY_VALUES.add(SORT_BY_PRIORITY);
    VALID_SORT_BY_VALUES.add(SORT_BY_TOPIC_NAME);
    VALID_SORT_BY_VALUES.add(SORT_BY_WORKER_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_INSTANCE_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXECUTION_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_KEY);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected String id;
  protected String externalTaskId;
  protected String topicName;
  protected String workerId;
  protected String errorMessage;
  protected String[] activityIds;
  protected String[] activityInstanceIds;
  protected String[] executionIds;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected Long priorityHigherThanOrEquals;
  protected Long priorityLowerThanOrEquals;
  protected String[] tenantIds;
  protected Boolean creationLog;
  protected Boolean failureLog;
  protected Boolean successLog;
  protected Boolean deletionLog;

  public HistoricExternalTaskLogQueryDto() {}

  public HistoricExternalTaskLogQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("logId")
  public void setLogId(String id) {
    this.id = id;
  }

  @CamundaQueryParam("externalTaskId")
  public void setExternalTaskId(String externalTaskId) {
    this.externalTaskId = externalTaskId;
  }

  @CamundaQueryParam("topicName")
  public void setTopicName(String topicName) {
    this.topicName = topicName;
  }

  @CamundaQueryParam("workerId")
  public void setWorkerId(String workerId) {
    this.workerId = workerId;
  }

  @CamundaQueryParam("errorMessage")
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  @CamundaQueryParam(value="activityIdIn", converter = StringArrayConverter.class)
  public void setActivityIdIn(String[] activityIds) {
    this.activityIds = activityIds;
  }

  @CamundaQueryParam(value="activityInstanceIdIn", converter = StringArrayConverter.class)
  public void setActivityInstanceIdIn(String[] activityInstanceIds) {
    this.activityInstanceIds = activityInstanceIds;
  }

  @CamundaQueryParam(value="executionIdIn", converter = StringArrayConverter.class)
  public void setExecutionIdIn(String[] executionIds) {
    this.executionIds = executionIds;
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

  @CamundaQueryParam(value="priorityHigherThanOrEquals", converter = LongConverter.class)
  public void setPriorityHigherThanOrEquals(Long priorityHigherThanOrEquals) {
    this.priorityHigherThanOrEquals = priorityHigherThanOrEquals;
  }

  @CamundaQueryParam(value="priorityLowerThanOrEquals", converter = LongConverter.class)
  public void setPriorityLowerThanOrEquals(Long priorityLowerThanOrEquals) {
    this.priorityLowerThanOrEquals = priorityLowerThanOrEquals;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringArrayConverter.class)
  public void setTenantIdIn(String[] tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value="creationLog", converter = BooleanConverter.class)
  public void setCreationLog(Boolean creationLog) {
    this.creationLog = creationLog;
  }

  @CamundaQueryParam(value="failureLog", converter = BooleanConverter.class)
  public void setFailureLog(Boolean failureLog) {
    this.failureLog = failureLog;
  }

  @CamundaQueryParam(value="successLog", converter = BooleanConverter.class)
  public void setSuccessLog(Boolean successLog) {
    this.successLog = successLog;
  }

  @CamundaQueryParam(value="deletionLog", converter = BooleanConverter.class)
  public void setDeletionLog(Boolean deletionLog) {
    this.deletionLog = deletionLog;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected HistoricExternalTaskLogQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricExternalTaskLogQuery();

  }

  @Override
  protected void applyFilters(HistoricExternalTaskLogQuery query) {
    if (id != null) {
      query.logId(id);
    }

    if (externalTaskId != null) {
      query.externalTaskId(externalTaskId);
    }

    if (topicName != null) {
      query.topicName(topicName);
    }

    if (workerId != null) {
      query.workerId(workerId);
    }

    if (errorMessage != null) {
      query.errorMessage(errorMessage);
    }

    if (activityIds != null && activityIds.length > 0) {
      query.activityIdIn(activityIds);
    }

    if (activityInstanceIds != null && activityInstanceIds.length > 0) {
      query.activityInstanceIdIn(activityInstanceIds);
    }

    if (executionIds != null && executionIds.length > 0) {
      query.executionIdIn(executionIds);
    }

    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }

    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }

    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }

    if (creationLog != null && creationLog) {
      query.creationLog();
    }

    if (failureLog != null && failureLog) {
      query.failureLog();
    }

    if (successLog != null && successLog) {
      query.successLog();
    }

    if (deletionLog != null && deletionLog) {
      query.deletionLog();
    }

    if (priorityHigherThanOrEquals != null) {
      query.priorityHigherThanOrEquals(priorityHigherThanOrEquals);
    }

    if (priorityLowerThanOrEquals != null) {
      query.priorityLowerThanOrEquals(priorityLowerThanOrEquals);
    }
    if (tenantIds != null && tenantIds.length > 0) {
      query.tenantIdIn(tenantIds);
    }
  }

  @Override
  protected void applySortBy(HistoricExternalTaskLogQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_TIMESTAMP)) {
      query.orderByTimestamp();
    } else if (sortBy.equals(SORT_BY_EXTERNAL_TASK_ID)) {
      query.orderByExternalTaskId();
    } else if (sortBy.equals(SORT_BY_RETRIES)) {
      query.orderByRetries();
    } else if (sortBy.equals(SORT_BY_PRIORITY)) {
      query.orderByPriority();
    } else if (sortBy.equals(SORT_BY_TOPIC_NAME)) {
      query.orderByTopicName();
    } else if (sortBy.equals(SORT_BY_WORKER_ID)) {
      query.orderByWorkerId();
    } else if (sortBy.equals(SORT_BY_ACTIVITY_ID)) {
      query.orderByActivityId();
    }else if (sortBy.equals(SORT_BY_ACTIVITY_INSTANCE_ID)) {
      query.orderByActivityInstanceId();
    } else if (sortBy.equals(SORT_BY_EXECUTION_ID)) {
      query.orderByExecutionId();
    } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID)) {
      query.orderByProcessInstanceId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_ID)) {
      query.orderByProcessDefinitionId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_KEY)) {
      query.orderByProcessDefinitionKey();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }
}
