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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchElementConfiguration;
import org.camunda.bpm.engine.impl.batch.SetJobRetriesBatchConfiguration;
import org.camunda.bpm.engine.impl.batch.builder.BatchBuilder;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * @author Askar Akhmerov
 */
public abstract class AbstractSetJobsRetriesBatchCmd implements Command<Batch> {

  protected int retries;
  protected Date dueDate;
  protected boolean isDueDateSet;

  @Override
  public Batch execute(CommandContext commandContext) {
    BatchElementConfiguration elementConfiguration = collectJobIds(commandContext);

    EnsureUtil.ensureNotEmpty(BadUserRequestException.class, "jobIds", elementConfiguration.getIds());
    EnsureUtil.ensureGreaterThanOrEqual("Retries count", retries, 0);
    if(dueDate == null && commandContext.getProcessEngineConfiguration().isEnsureJobDueDateNotNull()) {
      dueDate = ClockUtil.getCurrentTime();
    }

    return new BatchBuilder(commandContext)
        .config(getConfiguration(elementConfiguration))
        .type(Batch.TYPE_SET_JOB_RETRIES)
        .permission(BatchPermissions.CREATE_BATCH_SET_JOB_RETRIES)
        .operationLogHandler(this::writeUserOperationLog)
        .build();
  }

  protected void writeUserOperationLog(CommandContext commandContext, int numInstances) {

    List<PropertyChange> propertyChanges = new ArrayList<>();
    propertyChanges.add(new PropertyChange("nrOfInstances",
        null,
        numInstances));
    propertyChanges.add(new PropertyChange("async", null, true));
    propertyChanges.add(new PropertyChange("retries", null, retries));

    commandContext.getOperationLogManager()
        .logJobOperation(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES,
            null,
            null,
            null,
            null,
            null,
            propertyChanges);
  }

  protected abstract BatchElementConfiguration collectJobIds(CommandContext commandContext);

  public BatchConfiguration getConfiguration(BatchElementConfiguration elementConfiguration) {
    return new SetJobRetriesBatchConfiguration(elementConfiguration.getIds(), elementConfiguration.getMappings(), retries, dueDate, isDueDateSet);
  }

}
