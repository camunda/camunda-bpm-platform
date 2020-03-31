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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.ModificationBatchConfiguration;
import org.camunda.bpm.engine.impl.ModificationBuilderImpl;
import org.camunda.bpm.engine.impl.batch.builder.BatchBuilder;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.DeploymentMapping;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;

public class ProcessInstanceModificationBatchCmd extends AbstractModificationCmd<Batch> {

  public ProcessInstanceModificationBatchCmd(ModificationBuilderImpl modificationBuilderImpl) {
    super(modificationBuilderImpl);
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    List<AbstractProcessInstanceModificationCommand> instructions = builder.getInstructions();
    ensureNotEmpty(BadUserRequestException.class,
        "Modification instructions cannot be empty", instructions);

    Collection<String> collectedInstanceIds = collectProcessInstanceIds();

    ensureNotEmpty(BadUserRequestException.class, "Process instance ids cannot be empty",
        "Process instance ids", collectedInstanceIds);

    ensureNotContainsNull(BadUserRequestException.class, "Process instance ids cannot be null",
        "Process instance ids", collectedInstanceIds);

    String processDefinitionId = builder.getProcessDefinitionId();
    ProcessDefinitionEntity processDefinition =
        getProcessDefinition(commandContext, processDefinitionId);

    ensureNotNull(BadUserRequestException.class,
        "Process definition id cannot be null", processDefinition);

    String tenantId = processDefinition.getTenantId();
    String annotation = builder.getAnnotation();

    return new BatchBuilder(commandContext)
        .type(Batch.TYPE_PROCESS_INSTANCE_MODIFICATION)
        .config(getConfiguration(collectedInstanceIds, processDefinition.getDeploymentId()))
        .tenantId(tenantId)
        .permission(BatchPermissions.CREATE_BATCH_MODIFY_PROCESS_INSTANCES)
        .operationLogHandler((ctx, instanceCount) ->
            writeUserOperationLog(ctx, processDefinition, instanceCount, true, annotation))
        .build();
  }

  public BatchConfiguration getConfiguration(Collection<String> instanceIds, String deploymentId) {
    return new ModificationBatchConfiguration(new ArrayList<>(instanceIds),
        DeploymentMappings.of(new DeploymentMapping(deploymentId, instanceIds.size())),
        builder.getProcessDefinitionId(),
        builder.getInstructions(),
        builder.isSkipCustomListeners(),
        builder.isSkipIoMappings());
  }

}
