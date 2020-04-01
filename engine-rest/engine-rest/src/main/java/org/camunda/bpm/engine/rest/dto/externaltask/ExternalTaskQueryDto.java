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
package org.camunda.bpm.engine.rest.dto.externaltask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringSetConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.rest.dto.converter.LongConverter;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskQueryDto extends AbstractQueryDto<ExternalTaskQuery> {

  public static final String SORT_BY_ID_VALUE = "id";
  public static final String SORT_BY_LOCK_EXPIRATION_TIME = "lockExpirationTime";
  public static final String SORT_BY_PROCESS_INSTANCE_ID = "processInstanceId";
  public static final String SORT_BY_PROCESS_DEFINITION_ID = "processDefinitionId";
  public static final String SORT_BY_PROCESS_DEFINITION_KEY = "processDefinitionKey";
  public static final String SORT_BY_TENANT_ID = "tenantId";
  public static final String SORT_BY_PRIORITY = "taskPriority";

  public static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<>();
    VALID_SORT_BY_VALUES.add(SORT_BY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_LOCK_EXPIRATION_TIME);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_KEY);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PRIORITY);
  }

  protected String externalTaskId;
  protected Set<String> externalTaskIds;
  protected String activityId;
  protected List<String> activityIdIn;
  protected Date lockExpirationBefore;
  protected Date lockExpirationAfter;
  protected String topicName;
  protected Boolean locked;
  protected Boolean notLocked;
  protected String executionId;
  protected String processInstanceId;
  protected List<String> processInstanceIdIn;
  protected String processDefinitionId;
  protected Boolean active;
  protected Boolean suspended;
  protected Boolean withRetriesLeft;
  protected Boolean noRetriesLeft;
  protected String workerId;
  protected List<String> tenantIds;
  protected Long priorityHigherThanOrEquals;
  protected Long priorityLowerThanOrEquals;

  public ExternalTaskQueryDto() {
  }

  public ExternalTaskQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("externalTaskId")
  public void setExternalTaskId(String externalTaskId) {
    this.externalTaskId = externalTaskId;
  }

  @CamundaQueryParam(value = "externalTaskIdIn", converter = StringSetConverter.class)
  public void setExternalTaskIdIn(Set<String> externalTaskIds) {
    this.externalTaskIds = externalTaskIds;
  }

  @CamundaQueryParam("activityId")
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  @CamundaQueryParam(value = "activityIdIn", converter = StringListConverter.class)
  public void setActivityIdIn(List<String> activityIdIn) {
    this.activityIdIn = activityIdIn;
  }

  @CamundaQueryParam(value = "lockExpirationBefore", converter = DateConverter.class)
  public void setLockExpirationBefore(Date lockExpirationBefore) {
    this.lockExpirationBefore = lockExpirationBefore;
  }

  @CamundaQueryParam(value = "lockExpirationAfter", converter = DateConverter.class)
  public void setLockExpirationAfter(Date lockExpirationAfter) {
    this.lockExpirationAfter = lockExpirationAfter;
  }

  @CamundaQueryParam("topicName")
  public void setTopicName(String topicName) {
    this.topicName = topicName;
  }

  @CamundaQueryParam(value = "locked", converter = BooleanConverter.class)
  public void setLocked(Boolean locked) {
    this.locked = locked;
  }

  @CamundaQueryParam(value = "notLocked", converter = BooleanConverter.class)
  public void setNotLocked(Boolean notLocked) {
    this.notLocked = notLocked;
  }

  @CamundaQueryParam("executionId")
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam(value="processInstanceIdIn", converter = StringListConverter.class)
  public void setProcessInstanceIdIn(List<String> processInstanceIdIn) {
    this.processInstanceIdIn = processInstanceIdIn;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam(value = "active", converter = BooleanConverter.class)
  public void setActive(Boolean active) {
    this.active = active;
  }

  @CamundaQueryParam(value = "suspended", converter = BooleanConverter.class)
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @CamundaQueryParam(value = "withRetriesLeft", converter = BooleanConverter.class)
  public void setWithRetriesLeft(Boolean withRetriesLeft) {
    this.withRetriesLeft = withRetriesLeft;
  }

  @CamundaQueryParam(value = "noRetriesLeft", converter = BooleanConverter.class)
  public void setNoRetriesLeft(Boolean noRetriesLeft) {
    this.noRetriesLeft = noRetriesLeft;
  }

  @CamundaQueryParam("workerId")
  public void setWorkerId(String workerId) {
    this.workerId = workerId;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }
  @CamundaQueryParam(value="priorityHigherThanOrEquals", converter = LongConverter.class)
  public void setPriorityHigherThanOrEquals(Long priorityHigherThanOrEquals) {
    this.priorityHigherThanOrEquals = priorityHigherThanOrEquals;
  }

  @CamundaQueryParam(value="priorityLowerThanOrEquals", converter = LongConverter.class)
  public void setPriorityLowerThanOrEquals(Long priorityLowerThanOrEquals) {
    this.priorityLowerThanOrEquals = priorityLowerThanOrEquals;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected ExternalTaskQuery createNewQuery(ProcessEngine engine) {
    return engine.getExternalTaskService().createExternalTaskQuery();
  }

  @Override
  protected void applyFilters(ExternalTaskQuery query) {
    if (externalTaskId != null) {
      query.externalTaskId(externalTaskId);
    }
    if (externalTaskIds != null && !externalTaskIds.isEmpty()) {
      query.externalTaskIdIn(externalTaskIds);
    }
    if (activityId != null) {
      query.activityId(activityId);
    }
    if (activityIdIn != null && !activityIdIn.isEmpty()) {
      query.activityIdIn(activityIdIn.toArray(new String[0]));
    }
    if (lockExpirationBefore != null) {
      query.lockExpirationBefore(lockExpirationBefore);
    }
    if (lockExpirationAfter != null) {
      query.lockExpirationAfter(lockExpirationAfter);
    }
    if (topicName != null) {
      query.topicName(topicName);
    }
    if (locked != null && locked) {
      query.locked();
    }
    if (notLocked != null && notLocked) {
      query.notLocked();
    }
    if (executionId != null) {
      query.executionId(executionId);
    }
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (processInstanceIdIn != null && !processInstanceIdIn.isEmpty()) {
      query.processInstanceIdIn(processInstanceIdIn.toArray(new String[0]));
    }
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (active != null && active) {
      query.active();
    }
    if (suspended != null && suspended) {
      query.suspended();
    }
    if (priorityHigherThanOrEquals != null) {
      query.priorityHigherThanOrEquals(priorityHigherThanOrEquals);
    }
    if (priorityLowerThanOrEquals != null) {
      query.priorityLowerThanOrEquals(priorityLowerThanOrEquals);
    }
    if (withRetriesLeft != null && withRetriesLeft) {
      query.withRetriesLeft();
    }
    if (noRetriesLeft != null && noRetriesLeft) {
      query.noRetriesLeft();
    }
    if (workerId != null) {
      query.workerId(workerId);
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[0]));
    }
  }

  @Override
  protected void applySortBy(ExternalTaskQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (SORT_BY_ID_VALUE.equals(sortBy)) {
      query.orderById();
    }
    else if (SORT_BY_LOCK_EXPIRATION_TIME.equals(sortBy)) {
      query.orderByLockExpirationTime();
    }
    else if (SORT_BY_PROCESS_DEFINITION_ID.equals(sortBy)) {
      query.orderByProcessDefinitionId();
    }
    else if (SORT_BY_PROCESS_DEFINITION_KEY.equals(sortBy)) {
      query.orderByProcessDefinitionKey();
    }
    else if (SORT_BY_PROCESS_INSTANCE_ID.equals(sortBy)) {
      query.orderByProcessInstanceId();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    } else if (sortBy.equals(SORT_BY_PRIORITY)) {
      query.orderByPriority();
    }
  }
}
