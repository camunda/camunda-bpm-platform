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
package org.camunda.bpm.engine.rest.dto.runtime;

import org.camunda.bpm.engine.runtime.TransitionInstance;

/**
 * @author Daniel Meyer
 *
 */
public class TransitionInstanceDto {

  protected String id;
  protected String parentActivityInstanceId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String activityId;
  protected String activityName;
  protected String activityType;
  protected String executionId;
  protected String[] incidentIds;
  protected ActivityInstanceIncidentDto[] incidents;

  public String getId() {
    return id;
  }

  public String getParentActivityInstanceId() {
    return parentActivityInstanceId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  @Deprecated
  public String getTargetActivityId() {
    return activityId;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getActivityName() {
    return activityName;
  }

  public String getActivityType() {
    return activityType;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String[] getIncidentIds() {
    return incidentIds;
  }

  public ActivityInstanceIncidentDto[] getIncidents() {
    return incidents;
  }

  public static TransitionInstanceDto fromTransitionInstance(TransitionInstance instance) {
    TransitionInstanceDto result = new TransitionInstanceDto();
    result.id = instance.getId();
    result.parentActivityInstanceId = instance.getParentActivityInstanceId();
    result.activityId = instance.getActivityId();
    result.activityName = instance.getActivityName();
    result.activityType = instance.getActivityType();
    result.processInstanceId = instance.getProcessInstanceId();
    result.processDefinitionId = instance.getProcessDefinitionId();
    result.executionId = instance.getExecutionId();
    result.incidentIds = instance.getIncidentIds();
    result.incidents = ActivityInstanceIncidentDto.fromIncidents(instance.getIncidents());
    return result;
  }


  public static TransitionInstanceDto[] fromListOfTransitionInstance(TransitionInstance[] instances) {
    TransitionInstanceDto[] result = new TransitionInstanceDto[instances.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = fromTransitionInstance(instances[i]);
    }
    return result;
  }

}
