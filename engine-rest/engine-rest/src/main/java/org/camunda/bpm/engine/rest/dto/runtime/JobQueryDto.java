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
package org.camunda.bpm.engine.rest.dto.runtime;

import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.ConditionListConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.LongConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringSetConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.runtime.JobQuery;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JobQueryDto extends AbstractQueryDto<JobQuery> {

  private static final String SORT_BY_JOB_ID_VALUE = "jobId";
  private static final String SORT_BY_EXECUTION_ID_VALUE = "executionId";
  private static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "processInstanceId";
  private static final String SORT_BY_PROCESS_DEFINITION_ID_VALUE = "processDefinitionId";
  private static final String SORT_BY_PROCESS_DEFINITION_KEY_VALUE = "processDefinitionKey";
  private static final String SORT_BY_JOB_RETRIES_VALUE = "jobRetries";
  private static final String SORT_BY_JOB_DUEDATE_VALUE = "jobDueDate";
  private static final String SORT_BY_JOB_PRIORITY_VALUE = "jobPriority";
  private static final String SORT_BY_TENANT_ID = "tenantId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<>();
    VALID_SORT_BY_VALUES.add(SORT_BY_JOB_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXECUTION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_KEY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_JOB_RETRIES_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_JOB_DUEDATE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_JOB_PRIORITY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected String activityId;
  protected String jobId;
  protected Set<String> jobIds;
  protected String executionId;
  protected String processInstanceId;
  protected Set<String> processInstanceIds;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected Boolean withRetriesLeft;
  protected Boolean executable;
  protected Boolean timers;
  protected Boolean messages;
  protected Boolean withException;
  protected String exceptionMessage;
  protected String failedActivityId;
  protected Boolean noRetriesLeft;
  protected Boolean active;
  protected Boolean suspended;
  protected Long priorityHigherThanOrEquals;
  protected Long priorityLowerThanOrEquals;
  protected String jobDefinitionId;
  protected List<String> tenantIds;
  protected Boolean withoutTenantId;
  protected Boolean includeJobsWithoutTenantId;

  protected List<ConditionQueryParameterDto> dueDates;
  protected List<ConditionQueryParameterDto> createTimes;

  public JobQueryDto() {}

  public JobQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("activityId")
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  @CamundaQueryParam("failedActivityId")
  public void setFailedActivityId(String activityId) {
    this.failedActivityId = activityId;
  }

  @CamundaQueryParam("jobId")
  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  @CamundaQueryParam(value = "jobIds", converter = StringSetConverter.class)
  public void setJobIds(Set<String> jobIds) {
    this.jobIds = jobIds;
  }

  @CamundaQueryParam("executionId")
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam(value = "processInstanceIds", converter = StringSetConverter.class)
  public void setProcessInstanceIds(Set<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @CamundaQueryParam(value="withRetriesLeft", converter = BooleanConverter.class)
  public void setWithRetriesLeft(Boolean withRetriesLeft) {
    this.withRetriesLeft = withRetriesLeft;
  }

  @CamundaQueryParam(value="executable", converter = BooleanConverter.class)
  public void setExecutable(Boolean executable) {
    this.executable = executable;
  }

  @CamundaQueryParam(value="timers", converter = BooleanConverter.class)
  public void setTimers(Boolean timers) {
    this.timers = timers;
  }

  @CamundaQueryParam(value="withException", converter = BooleanConverter.class)
  public void setWithException(Boolean withException) {
    this.withException = withException;
  }

  @CamundaQueryParam(value="messages", converter = BooleanConverter.class)
  public void setMessages(Boolean messages) {
    this.messages = messages;
  }

  @CamundaQueryParam("exceptionMessage")
  public void setExceptionMessage(String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }

  @CamundaQueryParam(value = "dueDates", converter = ConditionListConverter.class)
  public void setDueDates(List<ConditionQueryParameterDto> dueDates) {
    this.dueDates = dueDates;
  }

  @CamundaQueryParam(value = "createTimes", converter = ConditionListConverter.class)
  public void setCreateTimes(List<ConditionQueryParameterDto> createTimes) {
    this.createTimes = createTimes;
  }

  @CamundaQueryParam(value="noRetriesLeft", converter = BooleanConverter.class)
  public void setNoRetriesLeft(Boolean noRetriesLeft) {
    this.noRetriesLeft = noRetriesLeft;
  }

  @CamundaQueryParam(value="active", converter = BooleanConverter.class)
  public void setActive(Boolean active) {
    this.active = active;
  }

  @CamundaQueryParam(value="suspended", converter = BooleanConverter.class)
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @CamundaQueryParam(value="priorityHigherThanOrEquals", converter = LongConverter.class)
  public void setPriorityHigherThanOrEquals(Long priorityHigherThanOrEquals) {
    this.priorityHigherThanOrEquals = priorityHigherThanOrEquals;
  }

  @CamundaQueryParam(value="priorityLowerThanOrEquals", converter = LongConverter.class)
  public void setPriorityLowerThanOrEquals(Long priorityLowerThanOrEquals) {
    this.priorityLowerThanOrEquals = priorityLowerThanOrEquals;
  }

  @CamundaQueryParam("jobDefinitionId")
  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @CamundaQueryParam(value = "includeJobsWithoutTenantId", converter = BooleanConverter.class)
  public void setIncludeJobsWithoutTenantId(Boolean includeJobsWithoutTenantId) {
    this.includeJobsWithoutTenantId = includeJobsWithoutTenantId;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected JobQuery createNewQuery(ProcessEngine engine) {
    return engine.getManagementService().createJobQuery();
  }

  private abstract class ApplyDates {
    void run(List<ConditionQueryParameterDto> dates) {
      DateConverter dateConverter = new DateConverter();
      dateConverter.setObjectMapper(objectMapper);

      for (ConditionQueryParameterDto conditionQueryParam : dates) {
        String op = conditionQueryParam.getOperator();
        Date date;

        try {
          date = dateConverter.convertQueryParameterToType((String) conditionQueryParam.getValue());
        } catch (RestException e) {
          throw new InvalidRequestException(e.getStatus(), e, "Invalid " + fieldName() + " format: " + e.getMessage());
        }

        if (op.equals(ConditionQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
          setGreaterThan(date);
        } else if (op.equals(ConditionQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
          setLowerThan(date);
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid " + fieldName() + " comparator specified: " + op);
        }
      }
    }

    /**
     * @return a descriptive name of the target field, used in error-messages
     */
    abstract String fieldName();

    abstract void setGreaterThan(Date date);

    abstract void setLowerThan(Date date);
  }

  @Override
  protected void applyFilters(final JobQuery query) {
    if (activityId != null){
      query.activityId(activityId);
    }

    if (jobId != null) {
      query.jobId(jobId);
    }

    if (!CollectionUtil.isEmpty(jobIds)) {
      query.jobIds(jobIds);
    }

    if (executionId != null) {
      query.executionId(executionId);
    }

    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }

    if (!CollectionUtil.isEmpty(processInstanceIds)) {
      query.processInstanceIds(processInstanceIds);
    }

    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }

    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }

    if (TRUE.equals(withRetriesLeft)) {
      query.withRetriesLeft();
    }

    if (TRUE.equals(executable)) {
      query.executable();
    }

    if (TRUE.equals(timers)) {
      if (messages != null && messages) {
        throw new InvalidRequestException(Status.BAD_REQUEST, "Parameter timers cannot be used together with parameter messages.");
      }
      query.timers();
    }

    if (TRUE.equals(messages)) {
      if (timers != null && timers) {
        throw new InvalidRequestException(Status.BAD_REQUEST, "Parameter messages cannot be used together with parameter timers.");
      }
      query.messages();
    }

    if (TRUE.equals(withException)) {
      query.withException();
    }

    if (exceptionMessage != null) {
      query.exceptionMessage(exceptionMessage);
    }

    if (failedActivityId != null) {
      query.failedActivityId(failedActivityId);
    }

    if (TRUE.equals(noRetriesLeft)) {
      query.noRetriesLeft();
    }

    if (TRUE.equals(active)) {
      query.active();
    }

    if (TRUE.equals(suspended)) {
      query.suspended();
    }

    if (priorityHigherThanOrEquals != null) {
      query.priorityHigherThanOrEquals(priorityHigherThanOrEquals);
    }

    if (priorityLowerThanOrEquals != null) {
      query.priorityLowerThanOrEquals(priorityLowerThanOrEquals);
    }

    if (jobDefinitionId != null) {
      query.jobDefinitionId(jobDefinitionId);
    }

    if (dueDates != null) {
      new ApplyDates() {
        @Override
        void setGreaterThan(Date date) {
          query.duedateHigherThan(date);
        }

        @Override
        void setLowerThan(Date date) {
          query.duedateLowerThan(date);
        }

        @Override
        String fieldName() {
          return "due date";
        }
      }.run(dueDates);
    }

    if (createTimes != null) {
      new ApplyDates() {
        @Override
        void setGreaterThan(Date date) {
          query.createdAfter(date);
        }

        @Override
        void setLowerThan(Date date) {
          query.createdBefore(date);
        }

        @Override
        String fieldName() {
          return "create time";
        }
      }.run(createTimes);
    }

    if (!CollectionUtil.isEmpty(tenantIds)) {
      query.tenantIdIn(tenantIds.toArray(new String[0]));
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (TRUE.equals(includeJobsWithoutTenantId)) {
      query.includeJobsWithoutTenantId();
    }
  }

  @Override
  protected void applySortBy(JobQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_JOB_ID_VALUE)) {
      query.orderByJobId();
    } else if (sortBy.equals(SORT_BY_EXECUTION_ID_VALUE)) {
      query.orderByExecutionId();
    } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
      query.orderByProcessInstanceId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_ID_VALUE)) {
      query.orderByProcessDefinitionId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_KEY_VALUE)) {
      query.orderByProcessDefinitionKey();
    } else if (sortBy.equals(SORT_BY_JOB_RETRIES_VALUE)) {
      query.orderByJobRetries();
    } else if (sortBy.equals(SORT_BY_JOB_DUEDATE_VALUE)) {
      query.orderByJobDuedate();
    } else if (sortBy.equals(SORT_BY_JOB_PRIORITY_VALUE)) {
      query.orderByJobPriority();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
