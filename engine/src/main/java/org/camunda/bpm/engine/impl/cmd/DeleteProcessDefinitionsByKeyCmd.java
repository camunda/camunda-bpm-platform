/*
 * Copyright 2017 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.repository.ProcessDefinition;

import java.util.List;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * Command to delete process definitions by a given key.
 *
 * @author Tassilo Weidner
 */
public class DeleteProcessDefinitionsByKeyCmd extends AbstractDeleteProcessDefinitionCmd {

  private static final long serialVersionUID = 1L;

  private final String processDefinitionKey;
  private final String tenantId;
  private final boolean isTenantIdSet;

  public DeleteProcessDefinitionsByKeyCmd(String processDefinitionKey, boolean cascade, boolean skipCustomListeners, String tenantId, boolean isTenantIdSet) {
    this.processDefinitionKey = processDefinitionKey;
    this.cascade = cascade;
    this.skipCustomListeners = skipCustomListeners;
    this.tenantId = tenantId;
    this.isTenantIdSet = isTenantIdSet;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotNull("processDefinitionKey", processDefinitionKey);

    List<ProcessDefinition> processDefinitions = commandContext.getProcessDefinitionManager()
      .findDefinitionsByKeyAndTenantId(processDefinitionKey, tenantId, isTenantIdSet);
    ensureNotEmpty(NotFoundException.class, "No process definition found with key '" + processDefinitionKey + "'",
      "processDefinitions", processDefinitions);

    for (ProcessDefinition processDefinition: processDefinitions) {
      String processDefinitionId = processDefinition.getId();
      deleteProcessDefinitionCmd(commandContext, processDefinitionId, cascade, skipCustomListeners);
    }

    return null;
  }

}
