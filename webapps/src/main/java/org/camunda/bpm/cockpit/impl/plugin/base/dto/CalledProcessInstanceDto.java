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
package org.camunda.bpm.cockpit.impl.plugin.base.dto;

import java.util.List;

public class CalledProcessInstanceDto extends ProcessInstanceDto {

  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionName;
  protected List<IncidentStatisticsDto> incidents;

  protected String callActivityInstanceId;
  protected String callActivityId;

  public CalledProcessInstanceDto() {}

  public String getId() {
    return id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getCallActivityInstanceId() {
    return callActivityInstanceId;
  }

  public String getCallActivityId() {
    return callActivityId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public List<IncidentStatisticsDto> getIncidents() {
    return incidents;
  }

  public void setIncidents(List<IncidentStatisticsDto> incidents) {
    this.incidents = incidents;
  }

}
