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


import java.util.Collections;

import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ModificationBatchConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.ProcessInstanceModificationBuilderImpl;
import org.camunda.bpm.engine.impl.batch.builder.BatchBuilder;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.DeploymentMapping;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

/**
 * @author Yana Vasileva
 *
 */
public class ModifyProcessInstanceAsyncCmd implements Command<Batch> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected ProcessInstanceModificationBuilderImpl builder;

  public ModifyProcessInstanceAsyncCmd(ProcessInstanceModificationBuilderImpl builder) {
    this.builder = builder;
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    String processInstanceId = builder.getProcessInstanceId();

    ExecutionManager executionManager = commandContext.getExecutionManager();
    ExecutionEntity processInstance = executionManager.findExecutionById(processInstanceId);

    ensureProcessInstanceExists(processInstanceId, processInstance);

    String processDefinitionId = processInstance.getProcessDefinitionId();
    String tenantId = processInstance.getTenantId();

    String deploymentId = commandContext.getProcessEngineConfiguration().getDeploymentCache()
      .findDeployedProcessDefinitionById(processDefinitionId)
      .getDeploymentId();

    return new BatchBuilder(commandContext)
        .type(Batch.TYPE_PROCESS_INSTANCE_MODIFICATION)
        .config(getConfiguration(processDefinitionId, deploymentId))
        .tenantId(tenantId)
        .totalJobs(1)
        .permission(BatchPermissions.CREATE_BATCH_MODIFY_PROCESS_INSTANCES)
        .operationLogHandler(this::writeOperationLog)
        .build();
  }

  protected void ensureProcessInstanceExists(String processInstanceId,
                                             ExecutionEntity processInstance) {
    if (processInstance == null) {
      throw LOG.processInstanceDoesNotExist(processInstanceId);
    }
  }

  protected String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE;
  }

  protected void writeOperationLog(CommandContext commandContext) {
    commandContext.getOperationLogManager().logProcessInstanceOperation(getLogEntryOperation(),
        builder.getProcessInstanceId(),
        null,
        null,
        Collections.singletonList(PropertyChange.EMPTY_CHANGE),
        builder.getAnnotation());
  }

  public BatchConfiguration getConfiguration(String processDefinitionId, String deploymentId) {
    return new ModificationBatchConfiguration(
        Collections.singletonList(builder.getProcessInstanceId()),
        DeploymentMappings.of(new DeploymentMapping(deploymentId, 1)),
        processDefinitionId,
        builder.getModificationOperations(),
        builder.isSkipCustomListeners(),
        builder.isSkipIoMappings());
  }

}
