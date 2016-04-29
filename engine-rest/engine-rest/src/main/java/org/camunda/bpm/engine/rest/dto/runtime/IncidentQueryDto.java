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
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.runtime.IncidentQuery;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Roman Smirnov
 *
 */
public class IncidentQueryDto extends AbstractQueryDto<IncidentQuery>{

  private static final String SORT_BY_INCIDENT_ID = "incidentId";
  private static final String SORT_BY_INCIDENT_TIMESTAMP = "incidentTimestamp";
  private static final String SORT_BY_INCIDENT_TYPE = "incidentType";
  private static final String SORT_BY_EXECUTION_ID = "executionId";
  private static final String SORT_BY_ACTIVITY_ID = "activityId";
  private static final String SORT_BY_PROCESS_INSTANCE_ID = "processInstanceId";
  private static final String SORT_BY_PROCESS_DEFINITION_ID = "processDefinitionId";
  private static final String SORT_BY_CAUSE_INCIDENT_ID = "causeIncidentId";
  private static final String SORT_BY_ROOT_CAUSE_INCIDENT_ID = "rootCauseIncidentId";
  private static final String SORT_BY_CONFIGURATION = "configuration";
  private static final String SORT_BY_TENANT_ID = "tenantId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_INCIDENT_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_INCIDENT_TIMESTAMP);
    VALID_SORT_BY_VALUES.add(SORT_BY_INCIDENT_TYPE);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXECUTION_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_DEFINITION_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_CAUSE_INCIDENT_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_ROOT_CAUSE_INCIDENT_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_CONFIGURATION);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected String incidentId;
  protected String incidentType;
  protected String incidentMessage;
  protected String processDefinitionId;
  protected String processInstanceId;
  protected String executionId;
  protected String activityId;
  protected String causeIncidentId;
  protected String rootCauseIncidentId;
  protected String configuration;
  protected List<String> tenantIds;
  protected List<String> jobDefinitionIds;

  public IncidentQueryDto() {}

  public IncidentQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("incidentId")
  public void setIncidentId(String incidentId) {
    this.incidentId = incidentId;
  }

  @CamundaQueryParam("incidentType")
  public void setIncidentType(String incidentType) {
    this.incidentType = incidentType;
  }

  @CamundaQueryParam("incidentMessage")
  public void setIncidentMessage(String incidentMessage) {
    this.incidentMessage = incidentMessage;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam("executionId")
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @CamundaQueryParam("activityId")
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  @CamundaQueryParam("causeIncidentId")
  public void setCauseIncidentId(String causeIncidentId) {
    this.causeIncidentId = causeIncidentId;
  }

  @CamundaQueryParam("rootCauseIncidentId")
  public void setRootCauseIncidentId(String rootCauseIncidentId) {
    this.rootCauseIncidentId = rootCauseIncidentId;
  }

  @CamundaQueryParam("configuration")
  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "jobDefinitionIdIn", converter = StringListConverter.class)
  public void setJobDefinitionIdIn(List<String> jobDefinitionIds) {
    this.jobDefinitionIds = jobDefinitionIds;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }


  @Override
  protected IncidentQuery createNewQuery(ProcessEngine engine) {
    return engine.getRuntimeService().createIncidentQuery();
  }

  @Override
  protected void applyFilters(IncidentQuery query) {
    if (incidentId != null) {
      query.incidentId(incidentId);
    }
    if (incidentType != null) {
      query.incidentType(incidentType);
    }
    if (incidentMessage != null) {
      query.incidentMessage(incidentMessage);
    }
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (executionId != null) {
      query.executionId(executionId);
    }
    if (activityId != null) {
      query.activityId(activityId);
    }
    if (causeIncidentId != null) {
      query.causeIncidentId(causeIncidentId);
    }
    if (rootCauseIncidentId != null) {
      query.rootCauseIncidentId(rootCauseIncidentId);
    }
    if (configuration != null) {
      query.configuration(configuration);
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (jobDefinitionIds != null && !jobDefinitionIds.isEmpty()) {
      query.jobDefinitionIdIn(jobDefinitionIds.toArray(new String[jobDefinitionIds.size()]));
    }
  }

  @Override
  protected void applySortBy(IncidentQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_INCIDENT_ID)) {
      query.orderByIncidentId();
    } else if (sortBy.equals(SORT_BY_INCIDENT_TIMESTAMP)) {
      query.orderByIncidentTimestamp();
    } else if (sortBy.equals(SORT_BY_INCIDENT_TYPE)) {
      query.orderByIncidentType();
    } else if (sortBy.equals(SORT_BY_EXECUTION_ID)) {
      query.orderByExecutionId();
    } else if (sortBy.equals(SORT_BY_ACTIVITY_ID)) {
      query.orderByActivityId();
    } else if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID)) {
      query.orderByProcessInstanceId();
    } else if (sortBy.equals(SORT_BY_PROCESS_DEFINITION_ID)) {
      query.orderByProcessDefinitionId();
    } else if (sortBy.equals(SORT_BY_CAUSE_INCIDENT_ID)) {
      query.orderByCauseIncidentId();
    } else if (sortBy.equals(SORT_BY_ROOT_CAUSE_INCIDENT_ID)) {
      query.orderByRootCauseIncidentId();
    } else if (sortBy.equals(SORT_BY_CONFIGURATION)) {
      query.orderByConfiguration();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
