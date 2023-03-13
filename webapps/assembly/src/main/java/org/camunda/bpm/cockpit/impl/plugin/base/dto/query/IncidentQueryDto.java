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
package org.camunda.bpm.cockpit.impl.plugin.base.dto.query;

import javax.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.IncidentDto;
import org.camunda.bpm.cockpit.rest.dto.AbstractRestQueryParametersDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;

/**
 * @author roman.smirnov
 */
public class IncidentQueryDto extends AbstractRestQueryParametersDto<IncidentDto> {

  private static final long serialVersionUID = 1L;

  private static final String SORT_BY_INCIDENT_TIMESTAMP = "incidentTimestamp";
  private static final String SORT_BY_INCIDENT_MESSAGE = "incidentMessage";
  private static final String SORT_BY_INCIDENT_TYPE = "incidentType";
  private static final String SORT_BY_ACTIVITY_ID = "activityId";
  private static final String SORT_BY_CAUSE_INCIDENT_PROCESS_INSTANCE_ID = "causeIncidentProcessInstanceId";
  private static final String SORT_BY_ROOT_CAUSE_INCIDENT_PROCESS_INSTANCE_ID = "rootCauseIncidentProcessInstanceId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<>();
    VALID_SORT_BY_VALUES.add(SORT_BY_INCIDENT_TIMESTAMP);
    VALID_SORT_BY_VALUES.add(SORT_BY_INCIDENT_MESSAGE);
    VALID_SORT_BY_VALUES.add(SORT_BY_INCIDENT_TYPE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ACTIVITY_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_CAUSE_INCIDENT_PROCESS_INSTANCE_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_ROOT_CAUSE_INCIDENT_PROCESS_INSTANCE_ID);
  }

  private static final Map<String, String> ORDER_BY_VALUES;
  static {
    ORDER_BY_VALUES = new HashMap<>();
    ORDER_BY_VALUES.put(SORT_BY_INCIDENT_MESSAGE, "RES.INCIDENT_MSG_");
    ORDER_BY_VALUES.put(SORT_BY_INCIDENT_TIMESTAMP, "RES.INCIDENT_TIMESTAMP_");
    ORDER_BY_VALUES.put(SORT_BY_INCIDENT_TYPE, "RES.INCIDENT_TYPE_");
    ORDER_BY_VALUES.put(SORT_BY_ACTIVITY_ID, "RES.ACTIVITY_ID_");
    ORDER_BY_VALUES.put(SORT_BY_CAUSE_INCIDENT_PROCESS_INSTANCE_ID, "RES.CAUSE_PROC_INST_ID_");
    ORDER_BY_VALUES.put(SORT_BY_ROOT_CAUSE_INCIDENT_PROCESS_INSTANCE_ID, "RES.ROOT_PROC_INST_ID_");
  }

  protected String[] processDefinitionIdIn;
  protected String[] processInstanceIdIn;
  protected String[] activityIdIn;

  public IncidentQueryDto() { }

  public IncidentQueryDto(MultivaluedMap<String, String> queryParameters) {
    super(queryParameters);
  }

  public String[] getProcessDefinitionIdIn() {
    return processDefinitionIdIn;
  }

  @CamundaQueryParam(value="processDefinitionIdIn", converter = StringArrayConverter.class)
  public void setProcessDefinitionIdIn(String[] processDefinitionIdIn) {
    this.processDefinitionIdIn = processDefinitionIdIn;
  }

  public String[] getProcessInstanceIdIn() {
    return processInstanceIdIn;
  }

  @CamundaQueryParam(value="processInstanceIdIn", converter = StringArrayConverter.class)
  public void setProcessInstanceIdIn(String[] processInstanceIdIn) {
    this.processInstanceIdIn = processInstanceIdIn;
  }

  public String[] getActivityIdIn() {
    return activityIdIn;
  }

  @CamundaQueryParam(value="activityIdIn", converter = StringArrayConverter.class)
  public void setActivityIdIn(String[] activityIdIn) {
    this.activityIdIn = activityIdIn;
  }

  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  protected String getOrderByValue(String sortBy) {
    return ORDER_BY_VALUES.get(sortBy);
  }
}
