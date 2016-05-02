/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.batch;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Thorben Lindhauer
 *
 */
public class DeleteBatchCmd implements Command<Void> {

  protected boolean cascadeToHistory;
  protected String batchId;

  public DeleteBatchCmd(String batchId, boolean cascadeToHistory) {
    this.batchId = batchId;
    this.cascadeToHistory = cascadeToHistory;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotNull(BadUserRequestException.class, "Batch id must not be null", "batch id", batchId);

    BatchEntity batchEntity = commandContext.getBatchManager().findBatchById(batchId);
    ensureNotNull(BadUserRequestException.class, "Batch for id '" + batchId + "' cannot be found", "batch", batchEntity);

    checkAccess(commandContext, batchEntity);

    batchEntity.delete(cascadeToHistory);

    return null;
  }

  protected void checkAccess(CommandContext commandContext, BatchEntity batch) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteBatch(batch);
    }
  }

}
