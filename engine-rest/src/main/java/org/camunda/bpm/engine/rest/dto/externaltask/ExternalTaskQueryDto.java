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
package org.camunda.bpm.engine.rest.dto.externaltask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

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

  public static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_LOCK_EXPIRATION_TIME);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_KEY);
  }

  protected String externalTaskId;
  protected String activityId;
  protected Date lockExpirationBefore;
  protected Date lockExpirationAfter;
  protected String topicName;
  protected Boolean locked;
  protected Boolean notLocked;
  protected String executionId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected Boolean active;
  protected Boolean suspended;
  protected Boolean withRetriesLeft;
  protected Boolean noRetriesLeft;
  protected String workerId;

  public ExternalTaskQueryDto() {
  }

  public ExternalTaskQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("externalTaskId")
  public void setExternalTaskId(String externalTaskId) {
    this.externalTaskId = externalTaskId;
  }

  @CamundaQueryParam("activityId")
  public void setActivityId(String activityId) {
    this.activityId = activityId;
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
    if (activityId != null) {
      query.activityId(activityId);
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
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (active != null && active) {
      query.active();
    }
    if (suspended != null && suspended) {
      query.suspended();
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
    }
  }

}
