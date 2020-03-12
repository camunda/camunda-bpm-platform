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

import java.util.Date;

import org.camunda.bpm.engine.runtime.Job;

public class JobDto {

  protected String id;
  protected String jobDefinitionId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String executionId;
  protected String exceptionMessage;
  protected String failedActivityId;
  protected int retries;
  protected Date dueDate;
  protected boolean suspended;
  protected long priority;
  protected String tenantId;
  protected Date createTime;

  public static JobDto fromJob(Job job) {
    JobDto dto = new JobDto();
    dto.id = job.getId();
    dto.jobDefinitionId = job.getJobDefinitionId();
    dto.processInstanceId = job.getProcessInstanceId();
    dto.processDefinitionId = job.getProcessDefinitionId();
    dto.processDefinitionKey = job.getProcessDefinitionKey();
    dto.executionId = job.getExecutionId();
    dto.exceptionMessage = job.getExceptionMessage();
    dto.failedActivityId = job.getFailedActivityId();
    dto.retries = job.getRetries();
    dto.dueDate = job.getDuedate();
    dto.suspended = job.isSuspended();
    dto.priority = job.getPriority();
    dto.tenantId = job.getTenantId();
    dto.createTime = job.getCreateTime();

    return dto;
  }

  public String getId() {
    return id;
  }

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getExceptionMessage() {
    return exceptionMessage;
  }

  public String getFailedActivityId() {
    return failedActivityId;
  }

  public int getRetries() {
    return retries;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public long getPriority() {
    return priority;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Date getCreateTime() {
    return createTime;
  }

}
