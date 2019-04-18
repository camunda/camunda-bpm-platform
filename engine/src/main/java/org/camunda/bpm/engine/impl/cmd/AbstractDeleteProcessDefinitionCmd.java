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

import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.UserOperationLogManager;
import org.camunda.bpm.engine.repository.ProcessDefinition;

import java.io.Serializable;
import java.util.List;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Tassilo Weidner
 */
public abstract class AbstractDeleteProcessDefinitionCmd implements Command<Void>, Serializable {

  protected boolean cascade;
  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;

  protected void deleteProcessDefinitionCmd(CommandContext commandContext, String processDefinitionId, boolean cascade, boolean skipCustomListeners, boolean skipIoMappings) {
    ensureNotNull("processDefinitionId", processDefinitionId);

    ProcessDefinition processDefinition = commandContext.getProcessDefinitionManager()
      .findLatestProcessDefinitionById(processDefinitionId);
    ensureNotNull(NotFoundException.class, "No process definition found with id '" + processDefinitionId + "'",
      "processDefinition", processDefinition);

    List<CommandChecker> commandCheckers = commandContext.getProcessEngineConfiguration().getCommandCheckers();
    for (CommandChecker checker: commandCheckers) {
      checker.checkDeleteProcessDefinitionById(processDefinitionId);
    }

    UserOperationLogManager userOperationLogManager = commandContext.getOperationLogManager();
    userOperationLogManager.logProcessDefinitionOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE, processDefinitionId,
      processDefinition.getKey(), new PropertyChange("cascade", false, cascade));

    ProcessDefinitionManager definitionManager = commandContext.getProcessDefinitionManager();
    definitionManager.deleteProcessDefinition(processDefinition, processDefinitionId, cascade, cascade, skipCustomListeners, skipIoMappings);
  }

}
