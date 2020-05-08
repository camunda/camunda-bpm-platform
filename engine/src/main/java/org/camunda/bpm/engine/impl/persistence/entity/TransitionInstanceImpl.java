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
package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.TransitionInstance;

import java.util.Arrays;

/**
 * @author Daniel Meyer
 *
 */
public class TransitionInstanceImpl extends ProcessElementInstanceImpl implements TransitionInstance {

  protected String executionId;
  protected String activityId;
  protected String activityName;
  protected String activityType;

  protected String[] incidentIds = NO_IDS;
  protected Incident[] incidents = new Incident[0];

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getTargetActivityId() {
    return activityId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public String getActivityType() {
    return activityType;
  }

  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  public String getActivityName() {
    return activityName;
  }

  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }

  @Override
  public String[] getIncidentIds() {
    return incidentIds;
  }

  public void setIncidentIds(String[] incidentIds) {
    this.incidentIds = incidentIds;
  }

  @Override
  public Incident[] getIncidents() {
    return incidents;
  }

  public void setIncidents(Incident[] incidents) {
    this.incidents = incidents;
  }

  public String toString() {
    return this.getClass().getSimpleName()
           + "[executionId=" + executionId
           + ", targetActivityId=" + activityId
           + ", activityName=" + activityName
           + ", activityType=" + activityType
           + ", id=" + id
           + ", parentActivityInstanceId=" + parentActivityInstanceId
           + ", processInstanceId=" + processInstanceId
           + ", processDefinitionId=" + processDefinitionId
           + ", incidentIds=" + Arrays.toString(incidentIds)
           + ", incidents=" + Arrays.toString(incidents)
           + "]";
  }

}
