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
package org.camunda.bpm.engine.rest.dto.repository;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.dto.SuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import javax.ws.rs.core.Response.Status;
import java.util.Date;

/**
 * @author roman.smirnov
 */
public class ProcessDefinitionSuspensionStateDto extends SuspensionStateDto {

  private String executionDate;
  private boolean includeProcessInstances;
  private String processDefinitionId;
  private String processDefinitionKey;

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public void setExecutionDate(String executionDate) {
    this.executionDate = executionDate;
  }

  public void setIncludeProcessInstances(boolean includeProcessInstances) {
    this.includeProcessInstances = includeProcessInstances;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public void updateSuspensionState(ProcessEngine engine) {
    if (processDefinitionId != null && processDefinitionKey != null) {
      String message = "Only one of processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    RepositoryService repositoryService = engine.getRepositoryService();

    Date delayedExecutionDate = null;
    if (executionDate != null && !executionDate.equals("")) {
      delayedExecutionDate = DateTimeUtil.parseDate(executionDate);
    }

    if (processDefinitionId != null) {
      // activate/suspend process definition by id
      if (getSuspended()) {
        repositoryService.suspendProcessDefinitionById(processDefinitionId, includeProcessInstances, delayedExecutionDate);
      } else {
        repositoryService.activateProcessDefinitionById(processDefinitionId, includeProcessInstances, delayedExecutionDate);
      }
    } else

    if (processDefinitionKey != null) {
      // activate/suspend process definition by key
      if (getSuspended()) {
        repositoryService.suspendProcessDefinitionByKey(processDefinitionKey, includeProcessInstances, delayedExecutionDate);
      } else {
        repositoryService.activateProcessDefinitionByKey(processDefinitionKey, includeProcessInstances, delayedExecutionDate);
      }
    } else {
      String message = "Either processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }
  }

}
