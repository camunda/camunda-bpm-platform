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
package org.camunda.bpm.engine.impl.batch.history;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

public class DeleteHistoricBatchCmd implements Command<Object> {

  protected String batchId;

  public DeleteHistoricBatchCmd(String batchId) {
    this.batchId = batchId;
  }

  public Object execute(CommandContext commandContext) {
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "Historic batch id must not be null", "historic batch id", batchId);

    HistoricBatchEntity historicBatch = commandContext.getHistoricBatchManager().findHistoricBatchById(batchId);
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "Historic batch for id '" + batchId + "' cannot be found", "historic batch", historicBatch);

    checkAccess(commandContext, historicBatch);

    writeUserOperationLog(commandContext, historicBatch.getTenantId());

    historicBatch.delete();

    return null;
  }

  protected void checkAccess(CommandContext commandContext, HistoricBatchEntity batch) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteHistoricBatch(batch);
    }
  }

  protected void writeUserOperationLog(CommandContext commandContext, String tenantId) {
    commandContext.getOperationLogManager()
      .logBatchOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY, batchId, tenantId, PropertyChange.EMPTY_CHANGE);
  }

}
