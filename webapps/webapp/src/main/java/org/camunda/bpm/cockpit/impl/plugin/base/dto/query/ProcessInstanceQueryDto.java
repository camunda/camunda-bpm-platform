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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessInstanceDto;
import org.camunda.bpm.cockpit.rest.dto.AbstractRestQueryParametersDto;
import org.camunda.bpm.engine.impl.QueryVariableValue;
import org.camunda.bpm.engine.impl.variable.VariableTypes;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;

public class ProcessInstanceQueryDto extends AbstractRestQueryParametersDto<ProcessInstanceDto> {

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

  private List<VariableQueryParameterDto> variables;

  /**
   * Process instance compatible wrapper for query variables
   */
  private List<QueryVariableValue> queryVariableValues;

  public ProcessInstanceQueryDto() {
  }

  public ProcessInstanceQueryDto(MultivaluedMap<String, String> queryParameter) {
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

  public void initQueryVariableValues(VariableTypes variableTypes) {
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

  @Override
  protected String getOrderByValue(String sortBy) {
    return ORDER_BY_VALUES.get(sortBy);
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  private List<QueryVariableValue> createQueryVariableValues(VariableTypes variableTypes, List<VariableQueryParameterDto> variables) {

    List<QueryVariableValue> values = new ArrayList<QueryVariableValue>();

    if (variables == null) {
      return values;
    }

    for (VariableQueryParameterDto variable : variables) {
      QueryVariableValue value = new QueryVariableValue(
          variable.getName(),
          variable.getValue(),
          ConditionQueryParameterDto.getQueryOperator(variable.getOperator()),
          false);

      value.initialize(variableTypes);
      values.add(value);
    }

    return values;
  }
}
