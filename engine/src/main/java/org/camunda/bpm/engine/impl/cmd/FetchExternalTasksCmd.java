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

import static org.camunda.bpm.engine.impl.Direction.DESCENDING;
import static org.camunda.bpm.engine.impl.ExternalTaskQueryProperty.PRIORITY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
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
  protected List<QueryOrderingProperty> orderingProperties;

  protected Map<String, TopicFetchInstruction> fetchInstructions;

  public FetchExternalTasksCmd(String workerId, int maxResults, Map<String, TopicFetchInstruction> instructions) {
    this(workerId, maxResults, instructions, false, Collections.emptyList());
  }

  public FetchExternalTasksCmd(String workerId,
                               int maxResults,
                               Map<String, TopicFetchInstruction> instructions,
                               boolean usePriority,
                               List<QueryOrderingProperty> orderingProperties) {
    this.workerId = workerId;
    this.maxResults = maxResults;
    this.fetchInstructions = instructions;
    this.orderingProperties = orderingPropertiesWithPriority(usePriority, orderingProperties);
  }

  @Override
  public List<LockedExternalTask> execute(CommandContext commandContext) {
    validateInput();

    for (TopicFetchInstruction instruction : fetchInstructions.values()) {
      instruction.ensureVariablesInitialized();
    }

    List<ExternalTaskEntity> externalTasks = commandContext
      .getExternalTaskManager()
      .selectExternalTasksForTopics(new ArrayList<>(fetchInstructions.values()), maxResults, orderingProperties);

    final List<LockedExternalTask> result = new ArrayList<>();

    for (ExternalTaskEntity entity : externalTasks) {

      TopicFetchInstruction fetchInstruction = fetchInstructions.get(entity.getTopicName());

      // retrieve the execution first to detect concurrent modifications @https://jira.camunda.com/browse/CAM-10750
      ExecutionEntity execution = entity.getExecution(false);

      if (execution != null) {
        entity.lock(workerId, fetchInstruction.getLockDuration());

        LockedExternalTaskImpl resultTask = LockedExternalTaskImpl.fromEntity(
            entity,
            fetchInstruction.getVariablesToFetch(),
            fetchInstruction.isLocalVariables(),
            fetchInstruction.isDeserializeVariables(),
            fetchInstruction.isIncludeExtensionProperties()
        );

        result.add(resultTask);
      } else {
        LOG.logTaskWithoutExecution(workerId);
      }
    }

    filterOnOptimisticLockingFailure(commandContext, result);

    return result;
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

  protected List<QueryOrderingProperty> orderingPropertiesWithPriority(boolean usePriority,
                                                                       List<QueryOrderingProperty> queryOrderingProperties) {
    List<QueryOrderingProperty> results = new ArrayList<>();

    // Priority needs to be the first item in the list because it takes precedence over other sorting options
    // Multi level ordering works by going through the list of ordering properties from first to last item
    if (usePriority) {
      results.add(new QueryOrderingProperty(PRIORITY, DESCENDING));
    }

    results.addAll(queryOrderingProperties);

    return results;
  }
}