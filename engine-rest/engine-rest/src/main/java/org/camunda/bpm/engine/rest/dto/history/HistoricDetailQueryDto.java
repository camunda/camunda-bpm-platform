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
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricDetailQueryDto extends AbstractQueryDto<HistoricDetailQuery> {

  private static final String SORT_BY_PROCESS_INSTANCE_ID = "processInstanceId";
  private static final String SORT_BY_VARIABLE_NAME = "variableName";
  private static final String SORT_BY_VARIABLE_TYPE = "variableType";
  private static final String SORT_BY_VARIABLE_REVISION = "variableRevision";
  private static final String SORT_BY_FORM_PROPERTY_ID = "formPropertyId";
  private static final String SORT_BY_TIME = "time";
  private static final String SORT_PARTIALLY_BY_OCCURENCE = "occurrence";
  private static final String SORT_BY_TENANT_ID = "tenantId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_VARIABLE_NAME);
    VALID_SORT_BY_VALUES.add(SORT_BY_VARIABLE_TYPE);
    VALID_SORT_BY_VALUES.add(SORT_BY_VARIABLE_REVISION);
    VALID_SORT_BY_VALUES.add(SORT_BY_FORM_PROPERTY_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_TIME);
    VALID_SORT_BY_VALUES.add(SORT_PARTIALLY_BY_OCCURENCE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected String processInstanceId;
  protected String executionId;
  protected String activityInstanceId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String variableInstanceId;
  protected String[] variableTypeIn;
  protected String taskId;
  protected Boolean formFields;
  protected Boolean variableUpdates;
  protected Boolean excludeTaskDetails;
  protected List<String> tenantIds;
  protected String[] processInstanceIdIn;
  protected String userOperationId;
  private Date occurredBefore;
  private Date occurredAfter;

  public HistoricDetailQueryDto() {
  }

  public HistoricDetailQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam(value = "processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam(value = "executionId")
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @CamundaQueryParam(value = "activityInstanceId")
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  @CamundaQueryParam(value = "caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  @CamundaQueryParam(value = "caseExecutionId")
  public void setCaseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
  }

  @CamundaQueryParam(value = "variableInstanceId")
  public void setVariableInstanceId(String variableInstanceId) {
    this.variableInstanceId = variableInstanceId;
  }

   @CamundaQueryParam(value="variableTypeIn", converter = StringArrayConverter.class)
  public void setVariableTypeIn(String[] variableTypeIn) {
    this.variableTypeIn = variableTypeIn;
  }

  @CamundaQueryParam(value = "taskId")
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  @CamundaQueryParam(value = "formFields", converter = BooleanConverter.class)
  public void setFormFields(Boolean formFields) {
    this.formFields = formFields;
  }

  @CamundaQueryParam(value = "variableUpdates", converter = BooleanConverter.class)
  public void setVariableUpdates(Boolean variableUpdates) {
    this.variableUpdates = variableUpdates;
  }

  @CamundaQueryParam(value = "excludeTaskDetails", converter = BooleanConverter.class)
  public void setExcludeTaskDetails(Boolean excludeTaskDetails) {
    this.excludeTaskDetails = excludeTaskDetails;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value="processInstanceIdIn", converter = StringArrayConverter.class)
  public void setProcessInstanceIdIn(String[] processInstanceIdIn) {
    this.processInstanceIdIn = processInstanceIdIn;
  }


  @CamundaQueryParam(value = "userOperationId")
  public void setUserOperationId(String userOperationId) {
    this.userOperationId = userOperationId;
  }

  @CamundaQueryParam(value = "occurredBefore", converter = DateConverter.class)
  public void setOccurredBefore(Date occurredBefore) {
    this.occurredBefore = occurredBefore;
  }

  @CamundaQueryParam(value = "occurredAfter", converter = DateConverter.class)
  public void setOccurredAfter(Date occurredAfter) {
    this.occurredAfter = occurredAfter;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected HistoricDetailQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricDetailQuery();
  }

  @Override
  protected void applyFilters(HistoricDetailQuery query) {
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (executionId != null) {
      query.executionId(executionId);
    }
    if (activityInstanceId != null) {
      query.activityInstanceId(activityInstanceId);
    }
    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }
    if (caseExecutionId != null) {
      query.caseExecutionId(caseExecutionId);
    }
    if (variableInstanceId != null) {
      query.variableInstanceId(variableInstanceId);
    }
    if (variableTypeIn != null && variableTypeIn.length > 0) {
      query.variableTypeIn(variableTypeIn);
    }
    if (taskId != null) {
      query.taskId(taskId);
    }
    if (formFields != null) {
      query.formFields();
    }
    if (variableUpdates != null) {
      query.variableUpdates();
    }
    if (excludeTaskDetails != null) {
      query.excludeTaskDetails();
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (processInstanceIdIn != null && processInstanceIdIn.length > 0) {
      query.processInstanceIdIn(processInstanceIdIn);
    }
    if (userOperationId != null) {
      query.userOperationId(userOperationId);
    }
    if (occurredBefore != null) {
      query.occurredBefore(occurredBefore);
    }
    if (occurredAfter != null) {
      query.occurredAfter(occurredAfter);
    }
  }

  @Override
  protected void applySortBy(HistoricDetailQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID)) {
      query.orderByProcessInstanceId();
    } else if (sortBy.equals(SORT_BY_VARIABLE_NAME)) {
      query.orderByVariableName();
    } else if (sortBy.equals(SORT_BY_VARIABLE_TYPE)) {
      query.orderByVariableType();
    } else if (sortBy.equals(SORT_BY_VARIABLE_REVISION)) {
      query.orderByVariableRevision();
    } else if (sortBy.equals(SORT_BY_FORM_PROPERTY_ID)) {
      query.orderByFormPropertyId();
    } else if (sortBy.equals(SORT_BY_TIME)) {
      query.orderByTime();
    } else if (sortBy.equals(SORT_PARTIALLY_BY_OCCURENCE)) {
      query.orderPartiallyByOccurrence();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
