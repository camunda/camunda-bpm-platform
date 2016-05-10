/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricDecisionInstanceQueryDto extends AbstractQueryDto<HistoricDecisionInstanceQuery> {

  public static final String SORT_BY_EVALUATION_TIME_VALUE = "evaluationTime";
  public static final String SORT_BY_TENANT_ID = "tenantId";

  public static final List<String> VALID_SORT_BY_VALUES;

  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_EVALUATION_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  protected String decisionInstanceId;
  protected String[] decisionInstanceIdIn;
  protected String decisionDefinitionId;
  protected String decisionDefinitionKey;
  protected String decisionDefinitionName;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processInstanceId;
  protected String caseDefinitionId;
  protected String caseDefinitionKey;
  protected String caseInstanceId;
  protected String[] activityIdIn;
  protected String[] activityInstanceIdIn;
  protected Date evaluatedBefore;
  protected Date evaluatedAfter;
  protected String userId;
  protected Boolean includeInputs;
  protected Boolean includeOutputs;
  protected Boolean disableBinaryFetching;
  protected Boolean disableCustomObjectDeserialization;
  protected List<String> tenantIds;

  public HistoricDecisionInstanceQueryDto() {
  }

  public HistoricDecisionInstanceQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("decisionInstanceId")
  public void setDecisionInstanceId(String decisionInstanceId) {
    this.decisionInstanceId = decisionInstanceId;
  }

  @CamundaQueryParam(value = "decisionInstanceIdIn", converter = StringArrayConverter.class)
  public void setDecisionInstanceIdIn(String[] decisionInstanceIdIn) {
    this.decisionInstanceIdIn = decisionInstanceIdIn;
  }

  @CamundaQueryParam("decisionDefinitionId")
  public void setDecisionDefinitionId(String decisionDefinitionId) {
    this.decisionDefinitionId = decisionDefinitionId;
  }

  @CamundaQueryParam("decisionDefinitionKey")
  public void setDecisionDefinitionKey(String decisionDefinitionKey) {
    this.decisionDefinitionKey = decisionDefinitionKey;
  }

  @CamundaQueryParam("decisionDefinitionName")
  public void setDecisionDefinitionName(String decisionDefinitionName) {
    this.decisionDefinitionName = decisionDefinitionName;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam("caseDefinitionId")
  public void setCaseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
  }

  @CamundaQueryParam("caseDefinitionKey")
  public void setCaseDefinitionKey(String caseDefinitionKey) {
    this.caseDefinitionKey = caseDefinitionKey;
  }

  @CamundaQueryParam("caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  @CamundaQueryParam(value="activityIdIn", converter = StringArrayConverter.class)
  public void setActivityIdIn(String[] activityIdIn) {
    this.activityIdIn = activityIdIn;
  }

  @CamundaQueryParam(value="activityInstanceIdIn", converter = StringArrayConverter.class)
  public void setActivityInstanceIdIn(String[] activityInstanceIdIn) {
    this.activityInstanceIdIn = activityInstanceIdIn;
  }

  @CamundaQueryParam(value = "evaluatedBefore", converter = DateConverter.class)
  public void setEvaluatedBefore(Date evaluatedBefore) {
    this.evaluatedBefore = evaluatedBefore;
  }

  @CamundaQueryParam(value = "evaluatedAfter", converter = DateConverter.class)
  public void setEvaluatedAfter(Date evaluatedAfter) {
    this.evaluatedAfter = evaluatedAfter;
  }
  
  @CamundaQueryParam(value = "userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  @CamundaQueryParam(value = "includeInputs", converter = BooleanConverter.class)
  public void setIncludeInputs(Boolean includeInputs) {
    this.includeInputs = includeInputs;
  }

  @CamundaQueryParam(value = "includeOutputs", converter = BooleanConverter.class)
  public void setIncludeOutputs(Boolean includeOutputs) {
    this.includeOutputs = includeOutputs;
  }

  @CamundaQueryParam(value = "disableBinaryFetching", converter = BooleanConverter.class)
  public void setDisableBinaryFetching(Boolean disableBinaryFetching) {
    this.disableBinaryFetching = disableBinaryFetching;
  }

  @CamundaQueryParam(value = "disableCustomObjectDeserialization", converter = BooleanConverter.class)
  public void setDisableCustomObjectDeserialization(Boolean disableCustomObjectDeserialization) {
    this.disableCustomObjectDeserialization = disableCustomObjectDeserialization;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  protected HistoricDecisionInstanceQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricDecisionInstanceQuery();
  }

  protected void applyFilters(HistoricDecisionInstanceQuery query) {
    if (decisionInstanceId != null) {
      query.decisionInstanceId(decisionInstanceId);
    }
    if (decisionInstanceIdIn != null) {
      query.decisionInstanceIdIn(decisionInstanceIdIn);
    }
    if (decisionDefinitionId != null) {
      query.decisionDefinitionId(decisionDefinitionId);
    }
    if (decisionDefinitionKey != null) {
      query.decisionDefinitionKey(decisionDefinitionKey);
    }
    if (decisionDefinitionName != null) {
      query.decisionDefinitionName(decisionDefinitionName);
    }
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (caseDefinitionId != null) {
      query.caseDefinitionId(caseDefinitionId);
    }
    if (caseDefinitionKey != null) {
      query.caseDefinitionKey(caseDefinitionKey);
    }
    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }
    if (activityIdIn != null) {
      query.activityIdIn(activityIdIn);
    }
    if (activityInstanceIdIn != null) {
      query.activityInstanceIdIn(activityInstanceIdIn);
    }
    if (evaluatedBefore != null) {
      query.evaluatedBefore(evaluatedBefore);
    }
    if (evaluatedAfter != null) {
      query.evaluatedAfter(evaluatedAfter);
    }
    if (userId != null) {
      query.userId(userId);
    }
    if (includeInputs != null && includeInputs) {
      query.includeInputs();
    }
    if (includeOutputs != null && includeOutputs) {
      query.includeOutputs();
    }
    if (disableBinaryFetching != null && disableBinaryFetching) {
      query.disableBinaryFetching();
    }
    if (disableCustomObjectDeserialization != null && disableCustomObjectDeserialization) {
      query.disableCustomObjectDeserialization();
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
  }

  protected void applySortBy(HistoricDecisionInstanceQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_EVALUATION_TIME_VALUE)) {
      query.orderByEvaluationTime();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
