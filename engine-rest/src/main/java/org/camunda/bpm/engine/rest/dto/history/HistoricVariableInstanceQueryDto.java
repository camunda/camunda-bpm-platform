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
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.codehaus.jackson.map.ObjectMapper;

public class HistoricVariableInstanceQueryDto extends AbstractQueryDto<HistoricVariableInstanceQuery> {

  private static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "instanceId";
  private static final String SORT_BY_VARIABLE_NAME_VALUE = "variableName";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_VARIABLE_NAME_VALUE);
  }

  protected String processInstanceId;
  protected String caseInstanceId;
  protected String variableName;
  protected String variableNameLike;
  protected Object variableValue;
  protected String[] executionIdIn;
  protected String[] taskIdIn;
  protected String[] activityInstanceIdIn;
  protected String[] caseExecutionIdIn;

  public HistoricVariableInstanceQueryDto() {
  }

  public HistoricVariableInstanceQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam("caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  @CamundaQueryParam("variableName")
  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  @CamundaQueryParam("variableNameLike")
  public void setVariableNameLike(String variableNameLike) {
    this.variableNameLike = variableNameLike;
  }

  @CamundaQueryParam("variableValue")
  public void setVariableValue(Object variableValue) {
    this.variableValue = variableValue;
  }

  @CamundaQueryParam(value="executionIdIn", converter = StringArrayConverter.class)
  public void setExecutionIdIn(String[] executionIdIn) {
    this.executionIdIn = executionIdIn;
  }

  @CamundaQueryParam(value="taskIdIn", converter = StringArrayConverter.class)
  public void setTaskIdIn(String[] taskIdIn) {
    this.taskIdIn = taskIdIn;
  }

  @CamundaQueryParam(value="activityInstanceIdIn", converter = StringArrayConverter.class)
  public void setActivityInstanceIdIn(String[] activityInstanceIdIn) {
    this.activityInstanceIdIn = activityInstanceIdIn;
  }

  @CamundaQueryParam(value="caseExecutionIdIn", converter = StringArrayConverter.class)
  public void setCaseExecutionIdIn(String[] caseExecutionIdIn) {
    this.caseExecutionIdIn = caseExecutionIdIn;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected HistoricVariableInstanceQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createHistoricVariableInstanceQuery();
  }

  @Override
  protected void applyFilters(HistoricVariableInstanceQuery query) {
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }
    if (variableName != null) {
      query.variableName(variableName);
    }
    if (variableNameLike != null) {
      query.variableNameLike(variableNameLike);
    }
    if (variableValue != null) {
      if (variableName != null) {
        query.variableValueEquals(variableName, variableValue);
      } else {
        throw new InvalidRequestException(Status.BAD_REQUEST,
            "Only a single variable value parameter specified: variable name and value are required to be able to query after a specific variable value.");
      }
    }
    if (executionIdIn != null && executionIdIn.length > 0) {
      query.executionIdIn(executionIdIn);
    }
    if (taskIdIn != null && taskIdIn.length > 0) {
      query.taskIdIn(taskIdIn);
    }
    if (activityInstanceIdIn != null && activityInstanceIdIn.length > 0) {
      query.activityInstanceIdIn(activityInstanceIdIn);
    }
    if (caseExecutionIdIn != null && caseExecutionIdIn.length > 0) {
      query.caseExecutionIdIn(caseExecutionIdIn);
    }
  }

  @Override
  protected void applySortingOptions(HistoricVariableInstanceQuery query) {
    if (sortBy != null) {
      if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
        query.orderByProcessInstanceId();
      } else if (sortBy.equals(SORT_BY_VARIABLE_NAME_VALUE)) {
        query.orderByVariableName();
      }

      if (sortOrder != null) {
        if (sortOrder.equals(SORT_ORDER_ASC_VALUE)) {
          query.asc();
        } else if (sortOrder.equals(SORT_ORDER_DESC_VALUE)) {
          query.desc();
        }
      }
    }

  }

}
