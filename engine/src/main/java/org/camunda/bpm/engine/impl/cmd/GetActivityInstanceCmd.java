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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ActivityInstanceImpl;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TransitionInstanceImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.LegacyBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * <p>Creates an activity instance tree according to the following strategy:
 *
 * <ul>
 *   <li> Event scope executions are not considered at all
 *   <li> For every leaf execution, generate an activity/transition instance;
 *   the activity instance id is set in the leaf execution and the parent instance id is set in the parent execution
 *   <li> For every non-leaf scope execution, generate an activity instance;
 *   the activity instance id is always set in the parent execution and the parent activity
 *   instance id is always set in the parent's parent (because of tree compactation, we ensure
 *   that an activity instance id for a scope activity is always stored in the corresponding scope execution's parent,
 *   unless the execution is a leaf)
 *   <li> Compensation is an exception to the above procedure: A compensation throw event is not a scope, however the compensating executions
 *   are added as child executions of the (probably non-scope) execution executing the throw event. Logically, the compensating executions
 *   are children of the scope execution the throwing event is executed in. Due to this oddity, the activity instance id are stored on different
 *   executions
 * </ul>
 *
 * @author Thorben Lindhauer
 *
 */
public class GetActivityInstanceCmd implements Command<ActivityInstance> {

  protected String processInstanceId;

  /**
   * @param processInstanceId
   */
  public GetActivityInstanceCmd(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public ActivityInstance execute(CommandContext commandContext) {

    ensureNotNull("processInstanceId", processInstanceId);
    List<ExecutionEntity> executionList = loadProcessInstance(processInstanceId, commandContext);

    if (executionList.isEmpty()) {
      return null;
    }

    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkReadProcessInstance(processInstanceId);

    List<ExecutionEntity> nonEventScopeExecutions = filterNonEventScopeExecutions(executionList);
    List<ExecutionEntity> leaves = filterLeaves(nonEventScopeExecutions);
    ExecutionEntity processInstance = filterProcessInstance(executionList);

    // create act instance for process instance
    ActivityInstanceImpl processActInst = createActivityInstance(
      processInstance,
      processInstance.getProcessDefinition(),
      processInstanceId,
      null);
    Map<String, ActivityInstanceImpl> activityInstances = new HashMap<String, ActivityInstanceImpl>();
    activityInstances.put(processInstanceId, processActInst);

    Map<String, TransitionInstanceImpl> transitionInstances = new HashMap<String, TransitionInstanceImpl>();

    for (ExecutionEntity leaf : leaves) {
      // create an activity/transition instance for each leaf that executes a non-scope activity
      if (leaf.getActivityInstanceId() != null) {
        ActivityInstanceImpl leafInstance = createActivityInstance(leaf,
            leaf.getActivity(),
            leaf.getActivityInstanceId(),
            leaf.getParentActivityInstanceId());
        activityInstances.put(leafInstance.getId(), leafInstance);
      }
      else {
        TransitionInstanceImpl transitionInstance = createTransitionInstance(leaf);
        transitionInstances.put(transitionInstance.getId(), transitionInstance);
      }

      // create an activity instance for each scope
      Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping = leaf.createActivityExecutionMapping();
      activityExecutionMapping.remove(leaf.getActivity());
      activityExecutionMapping.remove(leaf.getProcessDefinition());
      LegacyBehavior.removeLegacyNonScopesFromMapping(activityExecutionMapping);

      for (Map.Entry<ScopeImpl, PvmExecutionImpl> scopeExecutionEntry : activityExecutionMapping.entrySet()) {
        ScopeImpl scope = scopeExecutionEntry.getKey();
        PvmExecutionImpl scopeExecution = scopeExecutionEntry.getValue();


        String activityInstanceId = null;
        // for compensation, the rule that the scope execution's activity instance id is always set on the parent
        // does not hold, because throwing compensation events are not scopes themselves
        if (scopeExecution.getParent() != null && scopeExecution.getParent().isCompensationThrowing()) {
          activityInstanceId = scopeExecution.getActivityInstanceId();
        }
        else {
          activityInstanceId = scopeExecution.getParentActivityInstanceId();
        }

        if (activityInstances.containsKey(activityInstanceId)) {
          continue;
        }
        else {
          String parentActivityInstanceId = null;
          PvmExecutionImpl parent = scopeExecution.getParent();
          if (parent != null) {
            parentActivityInstanceId = parent.getParentActivityInstanceId();
          }

          // regardless of the tree structure (compacted or not), the scope's activity instance id
          // is the activity instance id of the parent execution and the parent activity instance id
          // of that is the actual parent activity instance id
          ActivityInstanceImpl scopeInstance = createActivityInstance(
              scopeExecution,
              scope,
              activityInstanceId,
              parentActivityInstanceId);
          activityInstances.put(activityInstanceId, scopeInstance);
        }
      }
    }

    LegacyBehavior.repairParentRelationships(activityInstances.values(), processInstanceId);
    populateChildInstances(activityInstances, transitionInstances);

    return processActInst;
  }

  protected ActivityInstanceImpl createActivityInstance(PvmExecutionImpl scopeExecution, ScopeImpl scope,
      String activityInstanceId, String parentActivityInstanceId) {
    ActivityInstanceImpl actInst = new ActivityInstanceImpl();

    actInst.setId(activityInstanceId);
    actInst.setParentActivityInstanceId(parentActivityInstanceId);
    actInst.setProcessInstanceId(scopeExecution.getProcessInstanceId());
    actInst.setProcessDefinitionId(scopeExecution.getProcessDefinitionId());
    actInst.setBusinessKey(scopeExecution.getBusinessKey());
    actInst.setActivityId(scope.getId());
    actInst.setActivityName(scope.getName());
    if (scope.getId().equals(scopeExecution.getProcessDefinition().getId())) {
      actInst.setActivityType("processDefinition");
    }
    else {
      actInst.setActivityType((String) scope.getProperty("type"));
    }

    List<String> executionIds = new ArrayList<String>();
    executionIds.add(scopeExecution.getId());

    for (PvmExecutionImpl childExecution : scopeExecution.getNonEventScopeExecutions()) {
      // add all concurrent children that are not in an activity or inactive
      if (childExecution.isConcurrent() && (childExecution.getActivityId() == null || !childExecution.isActive())) {
        executionIds.add(childExecution.getId());
      }
    }
    actInst.setExecutionIds(executionIds.toArray(new String[executionIds.size()]));

    return actInst;
  }

  protected TransitionInstanceImpl createTransitionInstance(PvmExecutionImpl execution) {
    TransitionInstanceImpl transitionInstance = new TransitionInstanceImpl();

    // can use execution id as persistent ID for transition as an execution
    // can execute as most one transition at a time.
    transitionInstance.setId(execution.getId());
    transitionInstance.setParentActivityInstanceId(execution.getParentActivityInstanceId());
    transitionInstance.setProcessInstanceId(execution.getProcessInstanceId());
    transitionInstance.setProcessDefinitionId(execution.getProcessDefinitionId());
    transitionInstance.setExecutionId(execution.getId());
    transitionInstance.setActivityId(execution.getActivityId());

    return transitionInstance;
  }

  protected void populateChildInstances(Map<String, ActivityInstanceImpl> activityInstances,
      Map<String, TransitionInstanceImpl> transitionInstances) {
    Map<ActivityInstanceImpl, List<ActivityInstanceImpl>> childActivityInstances
      = new HashMap<ActivityInstanceImpl, List<ActivityInstanceImpl>>();
    Map<ActivityInstanceImpl, List<TransitionInstanceImpl>> childTransitionInstances
      = new HashMap<ActivityInstanceImpl, List<TransitionInstanceImpl>>();

    for (ActivityInstanceImpl instance : activityInstances.values()) {
      if (instance.getParentActivityInstanceId() != null) {
        ActivityInstanceImpl parentInstance = activityInstances.get(instance.getParentActivityInstanceId());
        if (parentInstance == null) {
          throw new ProcessEngineException("No parent activity instance with id " + instance.getParentActivityInstanceId() + " generated");
        }
        putListElement(childActivityInstances, parentInstance, instance);
      }
    }

    for (TransitionInstanceImpl instance : transitionInstances.values()) {
      if (instance.getParentActivityInstanceId() != null) {
        ActivityInstanceImpl parentInstance = activityInstances.get(instance.getParentActivityInstanceId());
        if (parentInstance == null) {
          throw new ProcessEngineException("No parent activity instance with id " + instance.getParentActivityInstanceId() + " generated");
        }
        putListElement(childTransitionInstances, parentInstance, instance);
      }
    }

    for (Map.Entry<ActivityInstanceImpl, List<ActivityInstanceImpl>> entry :
        childActivityInstances.entrySet()) {
      ActivityInstanceImpl instance = entry.getKey();
      List<ActivityInstanceImpl> childInstances = entry.getValue();
      if (childInstances != null) {
        instance.setChildActivityInstances(childInstances.toArray(new ActivityInstanceImpl[childInstances.size()]));
      }
    }

    for (Map.Entry<ActivityInstanceImpl, List<TransitionInstanceImpl>> entry :
      childTransitionInstances.entrySet()) {
    ActivityInstanceImpl instance = entry.getKey();
    List<TransitionInstanceImpl> childInstances = entry.getValue();
    if (childTransitionInstances != null) {
      instance.setChildTransitionInstances(childInstances.toArray(new TransitionInstanceImpl[childInstances.size()]));
    }
  }

  }

  protected <S, T> void putListElement(Map<S, List<T>> mapOfLists, S key, T listElement) {
    List<T> list = mapOfLists.get(key);
    if (list == null) {
      list = new ArrayList<T>();
      mapOfLists.put(key, list);
    }
    list.add(listElement);
  }

  protected ExecutionEntity filterProcessInstance(List<ExecutionEntity> executionList) {
    for (ExecutionEntity execution : executionList) {
      if (execution.isProcessInstanceExecution()) {
        return execution;
      }
    }

    throw new ProcessEngineException("Could not determine process instance execution");
  }

  protected List<ExecutionEntity> filterLeaves(List<ExecutionEntity> executionList) {
    List<ExecutionEntity> leaves = new ArrayList<ExecutionEntity>();
    for (ExecutionEntity execution : executionList) {
      // although executions executing throwing compensation events are not leaves in the tree,
      // they are treated as leaves since their child executions are logical children of their parent scope execution
      if (execution.getNonEventScopeExecutions().isEmpty() || execution.isCompensationThrowing()) {
        leaves.add(execution);
      }
    }
    return leaves;
  }

  protected List<ExecutionEntity> filterNonEventScopeExecutions(List<ExecutionEntity> executionList) {
    List<ExecutionEntity> nonEventScopeExecutions = new ArrayList<ExecutionEntity>();
    for (ExecutionEntity execution : executionList) {
      if (!execution.isEventScope()) {
        nonEventScopeExecutions.add(execution);
      }
    }
    return nonEventScopeExecutions;
  }

  protected List<ExecutionEntity> loadProcessInstance(String processInstanceId, CommandContext commandContext) {

    List<ExecutionEntity> result = null;

    // first try to load from cache
    // check whether the process instance is already (partially) loaded in command context
    List<ExecutionEntity> cachedExecutions = commandContext.getDbEntityManager().getCachedEntitiesByType(ExecutionEntity.class);
    for (ExecutionEntity executionEntity : cachedExecutions) {
      if(processInstanceId.equals(executionEntity.getProcessInstanceId())) {
        // found one execution from process instance
        result = new ArrayList<ExecutionEntity>();
        ExecutionEntity processInstance = executionEntity.getProcessInstance();
        // add process instance
        result.add(processInstance);
        loadChildExecutionsFromCache(processInstance, result);
        break;
      }
    }

    if(result == null) {
      // if the process instance could not be found in cache, load from database
      result = loadFromDb(processInstanceId, commandContext);
    }

    return result;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected List<ExecutionEntity> loadFromDb(final String processInstanceId, final CommandContext commandContext) {
    List<ExecutionEntity> executions = commandContext.runWithoutAuthorization(new Callable<List<ExecutionEntity>>() {
      public List<ExecutionEntity> call() throws Exception {
        return (List) new ExecutionQueryImpl(commandContext)
          .processInstanceId(processInstanceId)
          .list();
      }
    });

    // initialize parent/child sets
    Map<String, List<ExecutionEntity>> executionsByParent = new HashMap<String, List<ExecutionEntity>>();
    for (ExecutionEntity execution : executions) {
      putListElement(executionsByParent, execution.getParentId(), execution);
    }

    for (ExecutionEntity execution : executions) {
      List<ExecutionEntity> children = executionsByParent.get(execution.getId());
      if (children != null) {
        execution.setExecutions(children);
        for (ExecutionEntity child : children) {
          child.setParent(execution);
        }
      }
      else {
        execution.setExecutions(new ArrayList<ExecutionEntity>());
      }
    }

    return executions;
  }

  /**
   * Loads all executions that are part of this process instance tree from the dbSqlSession cache.
   * (optionally querying the db if a child is not already loaded.
   *
   * @param execution the current root execution (already contained in childExecutions)
   * @param childExecutions the list in which all child executions should be collected
   */
  protected void loadChildExecutionsFromCache(ExecutionEntity execution, List<ExecutionEntity> childExecutions) {
    List<ExecutionEntity> childrenOfThisExecution = execution.getExecutions();
    if(childrenOfThisExecution != null) {
      childExecutions.addAll(childrenOfThisExecution);
      for (ExecutionEntity child : childrenOfThisExecution) {
        loadChildExecutionsFromCache(child, childExecutions);
      }
    }
  }



}
