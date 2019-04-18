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
package org.camunda.bpm.engine.impl.repository;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.cmd.DeleteProcessDefinitionsByIdsCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteProcessDefinitionsByKeyCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.repository.DeleteProcessDefinitionsBuilder;
import org.camunda.bpm.engine.repository.DeleteProcessDefinitionsSelectBuilder;
import org.camunda.bpm.engine.repository.DeleteProcessDefinitionsTenantBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;

/**
 * Fluent builder implementation to delete process definitions.
 *
 * @author Tassilo Weidner
 */
public class DeleteProcessDefinitionsBuilderImpl implements DeleteProcessDefinitionsBuilder,
  DeleteProcessDefinitionsSelectBuilder, DeleteProcessDefinitionsTenantBuilder {

  private final CommandExecutor commandExecutor;

  private String processDefinitionKey;
  private List<String> processDefinitionIds;

  private boolean cascade;
  private String tenantId;
  private boolean isTenantIdSet;
  private boolean skipCustomListeners;
  protected boolean skipIoMappings;

  public DeleteProcessDefinitionsBuilderImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  @Override
  public DeleteProcessDefinitionsBuilderImpl byIds(String... processDefinitionId) {
    if (processDefinitionId != null) {
      this.processDefinitionIds = new ArrayList<String>();
      this.processDefinitionIds.addAll(Arrays.asList(processDefinitionId));
    }
    return this;
  }

  @Override
  public DeleteProcessDefinitionsBuilderImpl byKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public DeleteProcessDefinitionsBuilderImpl withoutTenantId() {
    isTenantIdSet = true;
    return this;
  }

  @Override
  public DeleteProcessDefinitionsBuilderImpl withTenantId(String tenantId) {
    ensureNotNull("tenantId", tenantId);
    isTenantIdSet = true;
    this.tenantId = tenantId;
    return this;
  }

  @Override
  public DeleteProcessDefinitionsBuilderImpl cascade() {
    this.cascade = true;
    return this;
  }

  @Override
  public DeleteProcessDefinitionsBuilderImpl skipCustomListeners() {
    this.skipCustomListeners = true;
    return this;
  }

  @Override
  public DeleteProcessDefinitionsBuilderImpl skipIoMappings() {
    this.skipIoMappings = true;
    return this;
  }

  @Override
  public void delete() {
    ensureOnlyOneNotNull(NullValueException.class, "'processDefinitionKey' or 'processDefinitionIds' cannot be null", processDefinitionKey, processDefinitionIds);

    Command<Void> command;
    if (processDefinitionKey != null) {
      command = new DeleteProcessDefinitionsByKeyCmd(processDefinitionKey, cascade, skipCustomListeners, skipIoMappings, tenantId, isTenantIdSet);
    } else if (processDefinitionIds != null && !processDefinitionIds.isEmpty()) {
      command = new DeleteProcessDefinitionsByIdsCmd(processDefinitionIds, cascade, skipCustomListeners, skipIoMappings);
    } else {
      return;
    }

    commandExecutor.execute(command);
  }

}
