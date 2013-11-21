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
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.Date;

import org.camunda.bpm.engine.runtime.Job;

public class JobDto {

  private String id;
  private String processInstanceId;
  private String processDefinitionId;
  private String processDefinitionKey;
  private String executionId;
  private String exceptionMessage;
  private int retries;
  private Date dueDate;
  private boolean suspended;

  public static JobDto fromJob(Job job) {
    JobDto dto = new JobDto();
    dto.id = job.getId();
    dto.processInstanceId = job.getProcessInstanceId();
    dto.processDefinitionId = job.getProcessDefinitionId();
    dto.processDefinitionKey = job.getProcessDefinitionKey();
    dto.executionId = job.getExecutionId();
    dto.exceptionMessage = job.getExceptionMessage();
    dto.retries = job.getRetries();
    dto.dueDate = job.getDuedate();
    dto.suspended = job.isSuspended();
    return dto;
  }

  public String getId() {
    return id;
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

}
