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

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.SuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import javax.ws.rs.core.Response.Status;

/**
 * @author roman.smirnov
 */
public class JobSuspensionStateDto extends SuspensionStateDto {

  private String jobId;
  private String jobDefinitionId;
  private String processInstanceId;
  private String processDefinitionId;
  private String processDefinitionKey;

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public void updateSuspensionState(ProcessEngine engine) {
    int params = (jobId != null ? 1 : 0)
               + (jobDefinitionId != null ? 1 : 0)
               + (processInstanceId != null ? 1 : 0)
               + (processDefinitionId != null ? 1 : 0)
               + (processDefinitionKey != null ? 1 : 0);

    if (params > 1) {
      String message = "Only one of jobId, jobDefinitionId, processInstanceId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    ManagementService managementService = engine.getManagementService();

    if (jobId != null) {
      // activate/suspend job by id
      if (getSuspended()) {
        managementService.suspendJobById(jobId);
      } else {
        managementService.activateJobById(jobId);
      }
    } else

    if (jobDefinitionId != null) {
      // activate/suspend jobs by job definition id
      if (getSuspended()) {
        managementService.suspendJobByJobDefinitionId(jobDefinitionId);
      } else {
        managementService.activateJobByJobDefinitionId(jobDefinitionId);
      }
    } else

    if (processInstanceId != null) {
      // activate/suspend jobs by process instance id
      if (getSuspended()) {
        managementService.suspendJobByProcessInstanceId(processInstanceId);
      } else {
        managementService.activateJobByProcessInstanceId(processInstanceId);
      }
    } else

    if (processDefinitionId != null) {
      // activate/suspend jobs by process definition id
      if (getSuspended()) {
        managementService.suspendJobByProcessDefinitionId(processDefinitionId);
      } else {
        managementService.activateJobByProcessDefinitionId(processDefinitionId);
      }
    } else

    if (processDefinitionKey != null) {
      // activate/suspend jobs by process definition key
      if (getSuspended()) {
        managementService.suspendJobByProcessDefinitionKey(processDefinitionKey);
      } else {
        managementService.activateJobByProcessDefinitionKey(processDefinitionKey);
      }
    } else {
      String message = "Either jobId, jobDefinitionId, processInstanceId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }
  }

}
