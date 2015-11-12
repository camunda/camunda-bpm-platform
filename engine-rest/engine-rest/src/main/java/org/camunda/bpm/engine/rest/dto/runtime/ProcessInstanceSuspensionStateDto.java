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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.dto.SuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import javax.ws.rs.core.Response.Status;

/**
 * @author roman.smirnov
 */
public class ProcessInstanceSuspensionStateDto extends SuspensionStateDto {

  private String processInstanceId;
  private String processDefinitionId;
  private String processDefinitionKey;

  public String getProcessInstanceId() {
    return processInstanceId;
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
    int params = (processInstanceId != null ? 1 : 0)
               + (processDefinitionId != null ? 1 : 0)
               + (processDefinitionKey != null ? 1 : 0);

    if (params > 1) {
      String message = "Only one of processInstanceId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    RuntimeService runtimeService = engine.getRuntimeService();

    if (processInstanceId != null) {
      // activate/suspend process instance by id
      if (getSuspended()) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
      } else {
        runtimeService.activateProcessInstanceById(processInstanceId);
      }
    } else

    if (processDefinitionId != null) {
      // activate/suspend process instances by process definition id
      if (getSuspended()) {
        runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinitionId);
      } else {
        runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinitionId);
      }
    } else

    if (processDefinitionKey != null) {
      // activate/suspend process instances by process definition key
      if (getSuspended()) {
        runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinitionKey);
      } else {
        runtimeService.activateProcessInstanceByProcessDefinitionKey(processDefinitionKey);
      }
    } else {
      String message = "Either processInstanceId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }
  }

}
