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
package org.camunda.bpm.cockpit.impl.plugin.base.dto.query;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessInstanceDto;
import org.camunda.bpm.cockpit.rest.dto.AbstractRestQueryParametersDto;
import org.camunda.bpm.engine.impl.QueryVariableValue;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;

import javax.ws.rs.core.MultivaluedMap;

import java.util.*;

public abstract class AbstractProcessInstanceQueryDto<T extends ProcessInstanceDto>
  extends AbstractRestQueryParametersDto<T> {

  private static final long serialVersionUID = 1L;

  private static final String SORT_BY_PROCESS_INSTANCE_START_TIME = "startTime";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_START_TIME);
  }

  private static final Map<String, String> ORDER_BY_VALUES;
  static {
    ORDER_BY_VALUES = new HashMap<String, String>();
    ORDER_BY_VALUES.put(SORT_BY_PROCESS_INSTANCE_START_TIME, "START_TIME_");
  }

  protected String processDefinitionId;
  protected String parentProcessDefinitionId;
  protected String[] activityIdIn;
  protected String[] activityInstanceIdIn;
  protected String businessKey;
  protected String parentProcessInstanceId;
  protected Date startedBefore;
  protected Date startedAfter;

  private List<VariableQueryParameterDto> variables;

  /**
   * Process instance compatible wrapper for query variables
   */
  private List<QueryVariableValue> queryVariableValues;

  public AbstractProcessInstanceQueryDto() {
  }

  public AbstractProcessInstanceQueryDto(MultivaluedMap<String, String> queryParameter) {
    super(queryParameter);
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  @CamundaQueryParam(value="processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getParentProcessDefinitionId() {
    return parentProcessDefinitionId;
  }

  @CamundaQueryParam(value="parentProcessDefinitionId")
  public void setParentProcessDefinitionId(String parentProcessDefinitionId) {
    this.parentProcessDefinitionId = parentProcessDefinitionId;
  }

  @CamundaQueryParam(value = "variables", converter = VariableListConverter.class)
  public void setVariables(List<VariableQueryParameterDto> variables) {
    this.variables = variables;
  }

  public List<QueryVariableValue> getQueryVariableValues() {
    return queryVariableValues;
  }

  public void initQueryVariableValues(VariableSerializers variableTypes) {
    queryVariableValues = createQueryVariableValues(variableTypes, variables);
  }

  public String getParentProcessInstanceId() {
    return parentProcessInstanceId;
  }

  @CamundaQueryParam(value="parentProcessInstanceId")
  public void setParentProcessInstanceId(String parentProcessInstanceId) {
    this.parentProcessInstanceId = parentProcessInstanceId;
  }

  public String[] getActivityIdIn() {
    return activityIdIn;
  }

  @CamundaQueryParam(value="activityIdIn", converter = StringArrayConverter.class)
  public void setActivityIdIn(String[] activityIdIn) {
    this.activityIdIn = activityIdIn;
  }

  public String[] getActivityInstanceIdIn() {
    return activityInstanceIdIn;
  }

  @CamundaQueryParam(value="activityInstanceIdIn", converter = StringArrayConverter.class)
  public void setActivityInstanceIdIn(String[] activityInstanceIdIn) {
    this.activityInstanceIdIn = activityInstanceIdIn;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  @CamundaQueryParam(value="businessKey")
  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public Date getStartedBefore() {
    return startedBefore;
  }

  @CamundaQueryParam(value="startedBefore", converter = DateConverter.class)
  public void setStartedBefore(Date startedBefore) {
    this.startedBefore = startedBefore;
  }

  public Date getStartedAfter() {
    return startedAfter;
  }

  @CamundaQueryParam(value="startedAfter", converter = DateConverter.class)
  public void setStartedAfter(Date startedAfter) {
    this.startedAfter = startedAfter;
  }

  @Override
  protected String getOrderByValue(String sortBy) {
    return ORDER_BY_VALUES.get(sortBy);
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  public String getOuterOrderBy() {
    String outerOrderBy = getOrderBy();
    if (outerOrderBy == null || outerOrderBy.isEmpty()) {
      return "ID_ asc";
    }
    else if (outerOrderBy.contains(".")) {
      return outerOrderBy.substring(outerOrderBy.lastIndexOf(".") + 1);
    }
    else {
      return outerOrderBy;
    }
  }

  private List<QueryVariableValue> createQueryVariableValues(VariableSerializers variableTypes, List<VariableQueryParameterDto> variables) {

    List<QueryVariableValue> values = new ArrayList<QueryVariableValue>();

    if (variables == null) {
      return values;
    }

    for (VariableQueryParameterDto variable : variables) {
      QueryVariableValue value = new QueryVariableValue(
          variable.getName(),
          resolveVariableValue(variable.getValue()),
          ConditionQueryParameterDto.getQueryOperator(variable.getOperator()),
          false);

      value.initialize(variableTypes);
      values.add(value);
    }

    return values;
  }
}
