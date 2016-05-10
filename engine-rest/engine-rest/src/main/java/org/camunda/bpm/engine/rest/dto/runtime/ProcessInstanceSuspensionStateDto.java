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
import org.camunda.bpm.engine.rest.dto.SuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateTenantBuilder;

/**
 * @author roman.smirnov
 */
public class ProcessInstanceSuspensionStateDto extends SuspensionStateDto {

  private String processInstanceId;
  private String processDefinitionId;
  private String processDefinitionKey;

  private String processDefinitionTenantId;
  private boolean processDefinitionWithoutTenantId;

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

  public void setProcessDefinitionTenantId(String processDefinitionTenantId) {
    this.processDefinitionTenantId = processDefinitionTenantId;
  }

  public void setProcessDefinitionWithoutTenantId(boolean processDefinitionWithoutTenantId) {
    this.processDefinitionWithoutTenantId = processDefinitionWithoutTenantId;
  }

  @Override
  public void updateSuspensionState(ProcessEngine engine) {
    int params = (processInstanceId != null ? 1 : 0)
               + (processDefinitionId != null ? 1 : 0)
               + (processDefinitionKey != null ? 1 : 0);

    if (params > 1) {
      String message = "Only one of processInstanceId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    } else if(params == 0) {
      String message = "Either processInstanceId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    UpdateProcessInstanceSuspensionStateBuilder updateSuspensionStateBuilder = createUpdateSuspensionStateBuilder(engine);

    if (getSuspended()) {
      updateSuspensionStateBuilder.suspend();
    } else {
      updateSuspensionStateBuilder.activate();
    }
  }

  protected UpdateProcessInstanceSuspensionStateBuilder createUpdateSuspensionStateBuilder(ProcessEngine engine) {
    UpdateProcessInstanceSuspensionStateSelectBuilder selectBuilder = engine.getRuntimeService().updateProcessInstanceSuspensionState();

    if (processInstanceId != null) {
      return selectBuilder.byProcessInstanceId(processInstanceId);

    } else if (processDefinitionId != null) {
      return selectBuilder.byProcessDefinitionId(processDefinitionId);

    } else {
      UpdateProcessInstanceSuspensionStateTenantBuilder tenantBuilder = selectBuilder.byProcessDefinitionKey(processDefinitionKey);

      if (processDefinitionTenantId != null) {
        tenantBuilder.processDefinitionTenantId(processDefinitionTenantId);

      } else if (processDefinitionWithoutTenantId) {
        tenantBuilder.processDefinitionWithoutTenantId();
      }

      return tenantBuilder;
    }
  }

}
