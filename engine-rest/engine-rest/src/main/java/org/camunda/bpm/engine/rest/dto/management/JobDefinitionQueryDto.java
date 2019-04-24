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
package org.camunda.bpm.engine.rest.dto.management;

import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author roman.smirnov
 */
public class JobDefinitionQueryDto extends AbstractQueryDto<JobDefinitionQuery> {

  private static final String SORT_BY_JOB_DEFINITION_ID = "jobDefinitionId";
  private static final String SORT_BY_ACTIVITY_ID = "activityId";
  private static final String SORT_BY_PROCESS_DEFINITION_ID = "processDefinitionId";
  private static final String SORT_BY_PROCESS_DEFINITION_KEY = "processDefinitionKey";
  private static final String SORT_BY_JOB_TYPE = "jobType";
  private static final String SORT_BY_JOB_CONFIGURATION = "jobConfiguration";
  private static final String SORT_BY_TENANT_ID = "tenantId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();

    VALID_SORT_BY_VALUES.add(SORT_BY_JOB_DEFINITION_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_KEY);
    VALID_SORT_BY_VALUES.add(SORT_BY_JOB_TYPE);
    VALID_SORT_BY_VALUES.add(SORT_BY_JOB_CONFIGURATION);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected String jobDefinitionId;
  protected String[] activityIdIn;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String jobType;
  protected String jobConfiguration;
  protected Boolean active;
  protected Boolean suspended;
  protected Boolean withOverridingJobPriority;
  protected List<String> tenantIds;
  protected Boolean withoutTenantId;
  protected Boolean includeJobDefinitionsWithoutTenantId;

  public JobDefinitionQueryDto() {}

  public JobDefinitionQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("jobDefinitionId")
  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

  @CamundaQueryParam(value="activityIdIn", converter = StringArrayConverter.class)
  public void setActivityIdIn(String[] activityIdIn) {
    this.activityIdIn = activityIdIn;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @CamundaQueryParam("jobType")
  public void setJobType(String jobType) {
    this.jobType = jobType;
  }

  @CamundaQueryParam("jobConfiguration")
  public void setJobConfiguration(String jobConfiguration) {
    this.jobConfiguration = jobConfiguration;
  }

  @CamundaQueryParam(value="active", converter = BooleanConverter.class)
  public void setActive(Boolean active) {
    this.active = active;
  }

  @CamundaQueryParam(value="suspended", converter = BooleanConverter.class)
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @CamundaQueryParam(value="withOverridingJobPriority", converter = BooleanConverter.class)
  public void setWithOverridingJobPriority(Boolean withOverridingJobPriority) {
    this.withOverridingJobPriority = withOverridingJobPriority;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @CamundaQueryParam(value = "includeJobDefinitionsWithoutTenantId", converter = BooleanConverter.class)
  public void setIncludeJobDefinitionsWithoutTenantId(Boolean includeJobDefinitionsWithoutTenantId) {
    this.includeJobDefinitionsWithoutTenantId = includeJobDefinitionsWithoutTenantId;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected JobDefinitionQuery createNewQuery(ProcessEngine engine) {
    return engine.getManagementService().createJobDefinitionQuery();
  }

  @Override
  protected void applyFilters(JobDefinitionQuery query) {
    if (jobDefinitionId != null) {
      query.jobDefinitionId(jobDefinitionId);
    }

    if (activityIdIn != null && activityIdIn.length > 0) {
      query.activityIdIn(activityIdIn);
    }

    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }

    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }

    if (jobType != null) {
      query.jobType(jobType);
    }

    if (jobConfiguration != null) {
      query.jobConfiguration(jobConfiguration);
    }

    if (TRUE.equals(active)) {
      query.active();
    }

    if (TRUE.equals(suspended)) {
      query.suspended();
    }

    if (TRUE.equals(withOverridingJobPriority)) {
      query.withOverridingJobPriority();
    }

    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (TRUE.equals(includeJobDefinitionsWithoutTenantId)) {
      query.includeJobDefinitionsWithoutTenantId();
    }
  }

  @Override
  protected void applySortBy(JobDefinitionQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_JOB_DEFINITION_ID)) {
      query.orderByJobDefinitionId();
    } else if (sortBy.equals(SORT_BY_ACTIVITY_ID)) {
      query.orderByActivityId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_ID)) {
      query.orderByProcessDefinitionId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_KEY)) {
      query.orderByProcessDefinitionKey();
    } else if (sortBy.equals(SORT_BY_JOB_TYPE)) {
      query.orderByJobType();
    } else if (sortBy.equals(SORT_BY_JOB_CONFIGURATION)) {
      query.orderByJobConfiguration();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
