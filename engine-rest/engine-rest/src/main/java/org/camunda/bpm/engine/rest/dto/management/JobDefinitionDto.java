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
package org.camunda.bpm.engine.rest.dto.management;

import org.camunda.bpm.engine.management.JobDefinition;

/**
 * @author roman.smirnov
 */
public class JobDefinitionDto {

  protected String id;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String jobType;
  protected String jobConfiguration;
  protected String activityId;
  protected boolean suspended;
  protected Long overridingJobPriority;
  protected String tenantId;
  protected String deploymentId;

  public JobDefinitionDto() { }

  public String getId() {
    return id;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  public String getJobType() {
    return jobType;
  }
  public String getJobConfiguration() {
    return jobConfiguration;
  }
  public String getActivityId() {
    return activityId;
  }
  public boolean isSuspended() {
    return suspended;
  }
  public Long getOverridingJobPriority() {
    return overridingJobPriority;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public static JobDefinitionDto fromJobDefinition(JobDefinition definition) {
    JobDefinitionDto dto = new JobDefinitionDto();

    dto.id = definition.getId();
    dto.processDefinitionId = definition.getProcessDefinitionId();
    dto.processDefinitionKey = definition.getProcessDefinitionKey();
    dto.jobType = definition.getJobType();
    dto.jobConfiguration = definition.getJobConfiguration();
    dto.activityId = definition.getActivityId();
    dto.suspended = definition.isSuspended();
    dto.overridingJobPriority = definition.getOverridingJobPriority();
    dto.tenantId = definition.getTenantId();
    dto.deploymentId = definition.getDeploymentId();

    return dto;
  }

}
