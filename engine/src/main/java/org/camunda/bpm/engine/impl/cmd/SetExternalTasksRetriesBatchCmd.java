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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchElementConfiguration;
import org.camunda.bpm.engine.impl.batch.SetRetriesBatchConfiguration;
import org.camunda.bpm.engine.impl.batch.builder.BatchBuilder;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class SetExternalTasksRetriesBatchCmd extends AbstractSetExternalTaskRetriesCmd<Batch> {

  public SetExternalTasksRetriesBatchCmd(UpdateExternalTaskRetriesBuilderImpl builder) {
    super(builder);
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    BatchElementConfiguration elementConfiguration = collectExternalTaskIds(commandContext);

    ensureNotEmpty(BadUserRequestException.class, "externalTaskIds", elementConfiguration.getIds());

    return new BatchBuilder(commandContext)
        .type(Batch.TYPE_SET_EXTERNAL_TASK_RETRIES)
        .config(getConfiguration(elementConfiguration))
        .permission(BatchPermissions.CREATE_BATCH_SET_EXTERNAL_TASK_RETRIES)
        .operationLogHandler(this::writeUserOperationLogAsync)
        .build();
  }

  public BatchConfiguration getConfiguration(BatchElementConfiguration elementConfiguration) {
    return new SetRetriesBatchConfiguration(elementConfiguration.getIds(),
        elementConfiguration.getMappings(), builder.getRetries());
  }

}
