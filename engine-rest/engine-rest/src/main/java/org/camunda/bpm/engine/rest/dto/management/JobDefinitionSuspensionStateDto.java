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
package org.camunda.bpm.engine.rest.dto.management;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.dto.SuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import javax.ws.rs.core.Response.Status;
import java.util.Date;

/**
 * @author roman.smirnov
 */
public class JobDefinitionSuspensionStateDto extends SuspensionStateDto {

  private String executionDate;
  private boolean includeJobs;
  private String jobDefinitionId;
  private String processDefinitionId;
  private String processDefinitionKey;

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

  public void setExecutionDate(String executionDate) {
    this.executionDate = executionDate;
  }

  public void setIncludeJobs(boolean includeJobs) {
    this.includeJobs = includeJobs;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public void updateSuspensionState(ProcessEngine engine) {
    int params = (jobDefinitionId != null ? 1 : 0)
               + (processDefinitionId != null ? 1 : 0)
               + (processDefinitionKey != null ? 1 : 0);

    if (params > 1) {
      String message = "Only one of jobDefinitionId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    ManagementService managementService = engine.getManagementService();

    Date delayedExecutionDate = null;
    if (executionDate != null && !executionDate.equals("")) {
      delayedExecutionDate = DateTimeUtil.parseDate(executionDate);
    }

    if (jobDefinitionId != null) {
      // activate/suspend job definition by id
      if (getSuspended()) {
        managementService.suspendJobDefinitionById(jobDefinitionId, includeJobs, delayedExecutionDate);
      } else {
        managementService.activateJobDefinitionById(jobDefinitionId, includeJobs, delayedExecutionDate);
      }
    } else

    if (processDefinitionId != null) {
      // activate/suspend job definition by process definition id
      if (getSuspended()) {
        managementService.suspendJobDefinitionByProcessDefinitionId(processDefinitionId, includeJobs, delayedExecutionDate);
      } else {
        managementService.activateJobDefinitionByProcessDefinitionId(processDefinitionId, includeJobs, delayedExecutionDate);
      }
    } else

    if (processDefinitionKey != null) {
      // activate/suspend job definition by process definition key
      if (getSuspended()) {
        managementService.suspendJobDefinitionByProcessDefinitionKey(processDefinitionKey, includeJobs, delayedExecutionDate);
      } else {
        managementService.activateJobDefinitionByProcessDefinitionKey(processDefinitionKey, includeJobs, delayedExecutionDate);
      }
    } else {
      String message = "Either jobDefinitionId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }
  }

}
