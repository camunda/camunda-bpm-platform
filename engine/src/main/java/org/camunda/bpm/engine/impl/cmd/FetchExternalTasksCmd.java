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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingListener;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingResult;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.externaltask.LockedExternalTaskImpl;
import org.camunda.bpm.engine.impl.externaltask.TopicFetchInstruction;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
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
  protected Map<String, TopicFetchInstruction> fetchInstructions = new HashMap<>();

  public FetchExternalTasksCmd(String workerId, int maxResults, Map<String, TopicFetchInstruction> instructions) {
    this(workerId, maxResults, instructions, false);
  }

  public FetchExternalTasksCmd(String workerId, int maxResults, Map<String, TopicFetchInstruction> instructions, boolean usePriority) {
    this.workerId = workerId;
    this.maxResults = maxResults;
    this.fetchInstructions = instructions;
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
      .selectExternalTasksForTopics(new ArrayList<>(fetchInstructions.values()), maxResults, usePriority);

    final List<LockedExternalTask> result = new ArrayList<>();

    for (ExternalTaskEntity entity : externalTasks) {

      TopicFetchInstruction fetchInstruction = fetchInstructions.get(entity.getTopicName());

      // retrieve the execution first to detect concurrent modifications @https://jira.camunda.com/browse/CAM-10750
      ExecutionEntity execution = entity.getExecution(false);

      if (execution != null) {
        entity.lock(workerId, fetchInstruction.getLockDuration());
        LockedExternalTaskImpl resultTask = LockedExternalTaskImpl.fromEntity(entity, fetchInstruction.getVariablesToFetch(), fetchInstruction.isLocalVariables(),
              fetchInstruction.isDeserializeVariables(), fetchInstruction.isIncludeExtensionProperties());
        result.add(resultTask);
      } else {
        LOG.logTaskWithoutExecution(workerId);
      }
    }

    filterOnOptimisticLockingFailure(commandContext, result);

    return result;
  }

  /**
   * When CockroachDB is used, this command may be retried multiple times until
   * it is successful, or the retries are exhausted. CockroachDB uses a stricter,
   * SERIALIZABLE transaction isolation which ensures a serialized manner
   * of transaction execution. A concurrent transaction that attempts to modify
   * the same data as another transaction is required to abort, rollback and retry.
   * This also makes our use-case of pessimistic locks redundant since we only use
   * them as synchronization barriers, and not to lock actual data which would
   * protect it from concurrent modifications.
   *
   * The FetchExternalTasks command only executes internal code, so we are certain
   * that a retry of a failed external task locking will not impact user data, and
   * may be performed multiple times.
   */
  @Override
  public boolean isRetryable() {
    return true;
  }

  protected void filterOnOptimisticLockingFailure(CommandContext commandContext, final List<LockedExternalTask> tasks) {
    commandContext.getDbEntityManager().registerOptimisticLockingListener(new OptimisticLockingListener() {

      @Override
      public Class<? extends DbEntity> getEntityType() {
        return ExternalTaskEntity.class;
      }

      @Override
      public OptimisticLockingResult failedOperation(DbOperation operation) {

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

          // If the entity that failed with an OLE is not in the list,
          // we rethrow the OLE to the caller.
          if (!failedOperationEntityInList) {
            return OptimisticLockingResult.THROW;
          }

          // If the entity that failed with an OLE has been removed
          // from the list, we suppress the OLE.
          return OptimisticLockingResult.IGNORE;
        }

        // If none of the conditions are satisfied, this might indicate a bug,
        // so we throw the OLE.
        return OptimisticLockingResult.THROW;
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
