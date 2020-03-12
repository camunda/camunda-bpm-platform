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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.LongConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricJobLogQueryDto extends AbstractQueryDto<HistoricJobLogQuery> {

  protected static final String SORT_BY_TIMESTAMP = "timestamp";
  protected static final String SORT_BY_JOB_ID = "jobId";
  protected static final String SORT_BY_JOB_DUE_DATE = "jobDueDate";
  protected static final String SORT_BY_JOB_RETRIES = "jobRetries";
  protected static final String SORT_BY_JOB_PRIORITY = "jobPriority";
  protected static final String SORT_BY_JOB_DEFINITION_ID = "jobDefinitionId";
  protected static final String SORT_BY_ACTIVITY_ID = "activityId";
  protected static final String SORT_BY_EXECUTION_ID = "executionId";
  protected static final String SORT_BY_PROCESS_INSTANCE_ID = "processInstanceId";
  protected static final String SORT_BY_PROCESS_DEFINITION_ID = "processDefinitionId";
  protected static final String SORT_BY_PROCESS_DEFINITION_KEY = "processDefinitionKey";
  protected static final String SORT_BY_DEPLOYMENT_ID = "deploymentId";
  protected static final String SORT_PARTIALLY_BY_OCCURRENCE = "occurrence";
  protected static final String SORT_BY_TENANT_ID = "tenantId";
  protected static final String SORT_BY_HOSTNAME = "hostname";

  protected static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<>();

    VALID_SORT_BY_VALUES.add(SORT_BY_TIMESTAMP);
    VALID_SORT_BY_VALUES.add(SORT_BY_JOB_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_JOB_DUE_DATE);
    VALID_SORT_BY_VALUES.add(SORT_BY_JOB_RETRIES);
    VALID_SORT_BY_VALUES.add(SORT_BY_JOB_PRIORITY);
    VALID_SORT_BY_VALUES.add(SORT_BY_JOB_DEFINITION_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXECUTION_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_KEY);
    VALID_SORT_BY_VALUES.add(SORT_BY_DEPLOYMENT_ID);
    VALID_SORT_BY_VALUES.add(SORT_PARTIALLY_BY_OCCURRENCE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_HOSTNAME);
  }

  protected String id;
  protected String jobId;
  protected String jobExceptionMessage;
  protected String jobDefinitionId;
  protected String jobDefinitionType;
  protected String jobDefinitionConfiguration;
  protected String[] activityIds;
  protected String[] failedActivityIds;
  protected String[] executionIds;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String deploymentId;
  protected Boolean creationLog;
  protected Boolean failureLog;
  protected Boolean successLog;
  protected Boolean deletionLog;
  protected Long jobPriorityHigherThanOrEquals;
  protected Long jobPriorityLowerThanOrEquals;
  protected List<String> tenantIds;
  protected Boolean withoutTenantId;
  protected String hostname;

  public HistoricJobLogQueryDto() {}

  public HistoricJobLogQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("logId")
  public void setLogId(String id) {
    this.id = id;
  }

  @CamundaQueryParam("jobId")
  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  @CamundaQueryParam("jobExceptionMessage")
  public void setJobExceptionMessage(String jobExceptionMessage) {
    this.jobExceptionMessage = jobExceptionMessage;
  }

  @CamundaQueryParam("jobDefinitionId")
  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

  @CamundaQueryParam("jobDefinitionType")
  public void setJobDefinitionType(String jobDefinitionType) {
    this.jobDefinitionType = jobDefinitionType;
  }

  @CamundaQueryParam("jobDefinitionConfiguration")
  public void setJobDefinitionConfiguration(String jobDefinitionConfiguration) {
    this.jobDefinitionConfiguration = jobDefinitionConfiguration;
  }

  @CamundaQueryParam(value="activityIdIn", converter = StringArrayConverter.class)
  public void setActivityIdIn(String[] activityIds) {
    this.activityIds = activityIds;
  }

  @CamundaQueryParam(value="failedActivityIdIn", converter = StringArrayConverter.class)
  public void setFailedActivityIdIn(String[] activityIds) {
    this.failedActivityIds = activityIds;
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

  @CamundaQueryParam("deploymentId")
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
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

  @CamundaQueryParam(value="jobPriorityHigherThanOrEquals", converter = LongConverter.class)
  public void setJobPriorityHigherThanOrEquals(Long jobPriorityHigherThanOrEquals) {
    this.jobPriorityHigherThanOrEquals = jobPriorityHigherThanOrEquals;
  }

  @CamundaQueryParam(value="jobPriorityLowerThanOrEquals", converter = LongConverter.class)
  public void setJobPriorityLowerThanOrEquals(Long jobPriorityLowerThanOrEquals) {
    this.jobPriorityLowerThanOrEquals = jobPriorityLowerThanOrEquals;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @CamundaQueryParam(value = "hostname")
  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected HistoricJobLogQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricJobLogQuery();
  }

  @Override
  protected void applyFilters(HistoricJobLogQuery query) {
    if (id != null) {
      query.logId(id);
    }

    if (jobId != null) {
      query.jobId(jobId);
    }

    if (jobExceptionMessage != null) {
      query.jobExceptionMessage(jobExceptionMessage);
    }

    if (jobDefinitionId != null) {
      query.jobDefinitionId(jobDefinitionId);
    }

    if (jobDefinitionType != null) {
      query.jobDefinitionType(jobDefinitionType);
    }

    if (jobDefinitionConfiguration != null) {
      query.jobDefinitionConfiguration(jobDefinitionConfiguration);
    }

    if (activityIds != null && activityIds.length > 0) {
      query.activityIdIn(activityIds);
    }

    if (failedActivityIds != null && failedActivityIds.length > 0) {
      query.failedActivityIdIn(failedActivityIds);
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

    if (deploymentId != null) {
      query.deploymentId(deploymentId);
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

    if (jobPriorityLowerThanOrEquals != null) {
      query.jobPriorityLowerThanOrEquals(jobPriorityLowerThanOrEquals);
    }

    if (jobPriorityHigherThanOrEquals != null) {
      query.jobPriorityHigherThanOrEquals(jobPriorityHigherThanOrEquals);
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (hostname != null && !hostname.isEmpty()) {
      query.hostname(hostname);
    }
  }

  @Override
  protected void applySortBy(HistoricJobLogQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_TIMESTAMP)) {
      query.orderByTimestamp();
    } else if (sortBy.equals(SORT_BY_JOB_ID)) {
      query.orderByJobId();
    } else if (sortBy.equals(SORT_BY_JOB_DUE_DATE)) {
      query.orderByJobDueDate();
    } else if (sortBy.equals(SORT_BY_JOB_RETRIES)) {
      query.orderByJobRetries();
    } else if (sortBy.equals(SORT_BY_JOB_PRIORITY)) {
      query.orderByJobPriority();
    } else if (sortBy.equals(SORT_BY_JOB_DEFINITION_ID)) {
      query.orderByJobDefinitionId();
    } else if (sortBy.equals(SORT_BY_ACTIVITY_ID)) {
      query.orderByActivityId();
    } else if (sortBy.equals(SORT_BY_EXECUTION_ID)) {
      query.orderByExecutionId();
    } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID)) {
      query.orderByProcessInstanceId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_ID)) {
      query.orderByProcessDefinitionId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_KEY)) {
      query.orderByProcessDefinitionKey();
    } else if (sortBy.equals(SORT_BY_DEPLOYMENT_ID)) {
      query.orderByDeploymentId();
    } else if (sortBy.equals(SORT_PARTIALLY_BY_OCCURRENCE)) {
      query.orderPartiallyByOccurrence();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    } else if (sortBy.equals(SORT_BY_HOSTNAME)) {
      query.orderByHostname();
    }
  }

}
