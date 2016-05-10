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

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.management.UpdateJobSuspensionStateBuilder;
import org.camunda.bpm.engine.management.UpdateJobSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.management.UpdateJobSuspensionStateTenantBuilder;
import org.camunda.bpm.engine.rest.dto.SuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

/**
 * @author roman.smirnov
 */
public class JobSuspensionStateDto extends SuspensionStateDto {

  private String jobId;
  private String jobDefinitionId;
  private String processInstanceId;
  private String processDefinitionId;
  private String processDefinitionKey;

  private String processDefinitionTenantId;
  private boolean processDefinitionWithoutTenantId;

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

  public void setProcessDefinitionTenantId(String processDefinitionTenantId) {
    this.processDefinitionTenantId = processDefinitionTenantId;
  }

  public void setProcessDefinitionWithoutTenantId(boolean processDefinitionWithoutTenantId) {
    this.processDefinitionWithoutTenantId = processDefinitionWithoutTenantId;
  }

  @Override
  public void updateSuspensionState(ProcessEngine engine) {
    int params = (jobId != null ? 1 : 0)
               + (jobDefinitionId != null ? 1 : 0)
               + (processInstanceId != null ? 1 : 0)
               + (processDefinitionId != null ? 1 : 0)
               + (processDefinitionKey != null ? 1 : 0);

    if (params > 1) {
      String message = "Only one of jobId, jobDefinitionId, processInstanceId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);

    } else if(params == 0) {
      String message = "Either jobId, jobDefinitionId, processInstanceId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    UpdateJobSuspensionStateBuilder updateSuspensionStateBuilder = createUpdateSuspensionStateBuilder(engine);

    if(getSuspended()) {
      updateSuspensionStateBuilder.suspend();
    } else {
      updateSuspensionStateBuilder.activate();
    }
  }

  protected UpdateJobSuspensionStateBuilder createUpdateSuspensionStateBuilder(ProcessEngine engine) {
    UpdateJobSuspensionStateSelectBuilder selectBuilder = engine.getManagementService().updateJobSuspensionState();

    if (jobId != null) {
      return selectBuilder.byJobId(jobId);

    } else  if (jobDefinitionId != null) {
      return selectBuilder.byJobDefinitionId(jobDefinitionId);

    } else if (processInstanceId != null) {
      return selectBuilder.byProcessInstanceId(processInstanceId);

    } else if (processDefinitionId != null) {
      return selectBuilder.byProcessDefinitionId(processDefinitionId);

    } else {
      UpdateJobSuspensionStateTenantBuilder tenantBuilder = selectBuilder.byProcessDefinitionKey(processDefinitionKey);

      if (processDefinitionTenantId != null) {
        tenantBuilder.processDefinitionTenantId(processDefinitionTenantId);

      } else if (processDefinitionWithoutTenantId) {
        tenantBuilder.processDefinitionWithoutTenantId();
      }

      return tenantBuilder;
    }
  }

}
