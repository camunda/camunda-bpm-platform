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
package org.camunda.bpm.engine.rest.dto;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateBuilder;

public class SuspensionStateDto {

  private boolean suspended;

  public boolean getSuspended() {
    return suspended;
  }

  public void setSuspended(boolean suspended) {
    this.suspended = suspended;
  }

  public static SuspensionStateDto fromState(boolean suspended) {
    SuspensionStateDto dto = new SuspensionStateDto();
    dto.suspended = suspended;
    return dto;
  }

  public void updateSuspensionState(ProcessEngine engine) {}

  public void updateSuspensionState(ProcessEngine engine, String processInstanceId) {
    UpdateProcessInstanceSuspensionStateBuilder updateSuspensionStateBuilder = null;
    if (processInstanceId != null) {
      updateSuspensionStateBuilder = engine.getRuntimeService().updateProcessInstanceSuspensionState()
                                                               .byProcessInstanceId(processInstanceId);
    } else {
      String message = "Specify processInstance with processInstanceId";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    if (getSuspended()) {
      updateSuspensionStateBuilder.suspend();
    } else {
      updateSuspensionStateBuilder.activate();
    }
  }
}
