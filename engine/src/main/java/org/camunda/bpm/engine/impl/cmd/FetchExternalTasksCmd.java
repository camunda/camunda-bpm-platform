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
package org.camunda.bpm.engine.impl.cmd;

import java.util.*;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingListener;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.externaltask.LockedExternalTaskImpl;
import org.camunda.bpm.engine.impl.externaltask.TopicFetchInstruction;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * @author Thorben Lindhauer
 * @author Christopher Zell
 *
 */
public class FetchExternalTasksCmd implements Command<List<LockedExternalTask>> {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected String workerId;
  protected int maxResults;
  protected boolean usePriority;
  protected Map<String, TopicFetchInstruction> fetchInstructions = new HashMap<String, TopicFetchInstruction>();
  protected boolean filterByBusinessKey;

  public FetchExternalTasksCmd(String workerId, int maxResults, Map<String, TopicFetchInstruction> instructions) {
    this(workerId, maxResults, instructions, false);
  }

  public FetchExternalTasksCmd(String workerId, int maxResults, Map<String, TopicFetchInstruction> instructions, boolean usePriority) {
    this.workerId = workerId;
    this.maxResults = maxResults;
    this.fetchInstructions = instructions;
    this.filterByBusinessKey = false;
    this.usePriority = usePriority;
  }

  public FetchExternalTasksCmd(String workerId, int maxResults, Map<String, TopicFetchInstruction> instructions, boolean filterByBusinessKey, boolean usePriority) {
    this.workerId = workerId;
    this.maxResults = maxResults;
    this.fetchInstructions = instructions;
    this.filterByBusinessKey = filterByBusinessKey;
    this.usePriority = usePriority;
  }

  @Override
  public List<LockedExternalTask> execute(CommandContext commandContext) {
    validateInput();

    for (TopicFetchInstruction instruction : fetchInstructions.values()) {
      instruction.ensureVariablesInitialized();
    }

    List<ExternalTaskEntity> externalTasks = commandContext
      .getExternalTaskManager()
      .selectExternalTasksForTopics(fetchInstructions.values(), filterByBusinessKey, maxResults, usePriority);

    final List<LockedExternalTask> result = new ArrayList<LockedExternalTask>();

    for (ExternalTaskEntity entity : externalTasks) {

      TopicFetchInstruction fetchInstruction = fetchInstructions.get(entity.getTopicName());
      entity.lock(workerId, fetchInstruction.getLockDuration());

      LockedExternalTaskImpl resultTask = LockedExternalTaskImpl.fromEntity(entity,
          fetchInstruction.getVariablesToFetch(), fetchInstruction.isLocalVariables() , fetchInstruction.isDeserializeVariables());

      result.add(resultTask);
    }

    filterOnOptimisticLockingFailure(commandContext, result);

    return result;
  }

  protected void filterOnOptimisticLockingFailure(CommandContext commandContext, final List<LockedExternalTask> tasks) {
    commandContext.getDbEntityManager().registerOptimisticLockingListener(new OptimisticLockingListener() {

      public Class<? extends DbEntity> getEntityType() {
        return ExternalTaskEntity.class;
      }

      public void failedOperation(DbOperation operation) {
        if (operation instanceof DbEntityOperation) {
          DbEntityOperation dbEntityOperation = (DbEntityOperation) operation;
          DbEntity dbEntity = dbEntityOperation.getEntity();

          boolean failedOperationEntityInList = false;

          Iterator<LockedExternalTask> it = tasks.iterator();
          while (it.hasNext()) {
            LockedExternalTask resultTask = it.next();
            if (resultTask.getId().equals(dbEntity.getId())) {
              it.remove();
              failedOperationEntityInList = true;
              break;
            }
          }

          if (!failedOperationEntityInList) {
            throw LOG.concurrentUpdateDbEntityException(operation);
          }
        }
      }
    });
  }

  protected void validateInput() {
    EnsureUtil.ensureNotNull("workerId", workerId);
    EnsureUtil.ensureGreaterThanOrEqual("maxResults", maxResults, 0);

    for (TopicFetchInstruction instruction : fetchInstructions.values()) {
      EnsureUtil.ensureNotNull("topicName", instruction.getTopicName());
      EnsureUtil.ensurePositive("lockTime", instruction.getLockDuration());
    }
  }
}
