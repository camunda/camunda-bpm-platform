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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;

import java.io.Serializable;
import java.util.Arrays;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Tobias Metzke
 *
 */
public class DeleteHistoricVariableInstancesByProcessInstanceIdCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  private String processInstanceId;

  public DeleteHistoricVariableInstancesByProcessInstanceIdCmd(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  
  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotEmpty(BadUserRequestException.class,"processInstanceId", processInstanceId);

    HistoricProcessInstanceEntity instance = commandContext.getHistoricProcessInstanceManager().findHistoricProcessInstance(processInstanceId);
    ensureNotNull(NotFoundException.class, "No historic process instance found with id: " + processInstanceId, "instance", instance);
    
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteHistoricVariableInstancesByProcessInstance(instance);
    }

    commandContext.getHistoricDetailManager().deleteHistoricDetailsByProcessInstanceIds(Arrays.asList(processInstanceId));
    commandContext.getHistoricVariableInstanceManager().deleteHistoricVariableInstanceByProcessInstanceIds(Arrays.asList(processInstanceId));
    
    // create user operation log
    ResourceDefinitionEntity<?> definition = null;
    try {
      definition = commandContext.getProcessEngineConfiguration().getDeploymentCache().findDeployedProcessDefinitionById(instance.getProcessDefinitionId());
    } catch (NullValueException nve) {
      // definition has been deleted already
    }
    commandContext.getOperationLogManager().logHistoricVariableOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY, instance, definition, PropertyChange.EMPTY_CHANGE);
    
    return null;
  }
}
