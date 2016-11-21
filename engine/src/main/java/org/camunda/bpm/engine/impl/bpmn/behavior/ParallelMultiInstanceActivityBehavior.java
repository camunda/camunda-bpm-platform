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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.parser.MigratingInstanceParseContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.MigrationObserverBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.Callback;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Meyer
 *
 */
public class ParallelMultiInstanceActivityBehavior extends MultiInstanceActivityBehavior implements MigrationObserverBehavior {

  @Override
  protected void createInstances(ActivityExecution execution, int nrOfInstances) throws Exception {
    PvmActivity innerActivity = getInnerActivity(execution.getActivity());

    // initialize the scope and create the desired number of child executions
    prepareScopeExecution(execution, nrOfInstances);

    List<ActivityExecution> concurrentExecutions = new ArrayList<ActivityExecution>();
    for (int i = 0; i < nrOfInstances; i++) {
      concurrentExecutions.add(createConcurrentExecution(execution));
    }

    // start the concurrent child executions
    // start executions in reverse order (order will be reversed again in command context with the effect that they are
    // actually be started in correct order :) )
    for (int i = (nrOfInstances - 1); i >= 0; i--) {
      ActivityExecution activityExecution = concurrentExecutions.get(i);
      performInstance(activityExecution, innerActivity, i);
    }
  }

  protected void prepareScopeExecution(ActivityExecution scopeExecution, int nrOfInstances) {
    // set the MI-body scoped variables
    setLoopVariable(scopeExecution, NUMBER_OF_INSTANCES, nrOfInstances);
    setLoopVariable(scopeExecution, NUMBER_OF_COMPLETED_INSTANCES, 0);
    setLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES, nrOfInstances);
    scopeExecution.setActivity(null);
    scopeExecution.inactivate();
  }

  protected ActivityExecution createConcurrentExecution(ActivityExecution scopeExecution) {
    ActivityExecution concurrentChild = scopeExecution.createExecution();
    scopeExecution.forceUpdate();
    concurrentChild.setConcurrent(true);
    concurrentChild.setScope(false);
    return concurrentChild;
  }

  @Override
  public void concurrentChildExecutionEnded(ActivityExecution scopeExecution, ActivityExecution endedExecution) {

    int nrOfCompletedInstances = getLoopVariable(scopeExecution, NUMBER_OF_COMPLETED_INSTANCES) + 1;
    setLoopVariable(scopeExecution, NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances);
    int nrOfActiveInstances = getLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES) - 1;
    setLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances);

    // inactivate the concurrent execution
    endedExecution.inactivate();
    endedExecution.setActivityInstanceId(null);

    // join
    scopeExecution.forceUpdate();
    // TODO: should the completion condition be evaluated on the scopeExecution or on the endedExecution?
    if(completionConditionSatisfied(endedExecution) ||
        allExecutionsEnded(scopeExecution, endedExecution)) {

      ArrayList<ActivityExecution> childExecutions = new ArrayList<ActivityExecution>(((PvmExecutionImpl) scopeExecution).getNonEventScopeExecutions());
      for (ActivityExecution childExecution : childExecutions) {
        // delete all not-ended instances; these are either active (for non-scope tasks) or inactive but have no activity id (for subprocesses, etc.)
        if (childExecution.isActive() || childExecution.getActivity() == null) {
          ((PvmExecutionImpl)childExecution).deleteCascade("Multi instance completion condition satisfied.");
        }
        else {
          childExecution.remove();
        }
      }

      scopeExecution.setActivity((PvmActivity) endedExecution.getActivity().getFlowScope());
      scopeExecution.setActive(true);
      leave(scopeExecution);
    } else {
      ((ExecutionEntity) scopeExecution).dispatchDelayedEventsAndPerformOperation((Callback<PvmExecutionImpl, Void>) null);
    }
  }

  protected boolean allExecutionsEnded(ActivityExecution scopeExecution, ActivityExecution endedExecution) {
    int numberOfInactiveConcurrentExecutions = endedExecution.findInactiveConcurrentExecutions(endedExecution.getActivity()).size();
    int concurrentExecutions = scopeExecution.getExecutions().size();

    // no active instances exist and all concurrent executions are inactive
    return getLocalLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES) <= 0 &&
           numberOfInactiveConcurrentExecutions == concurrentExecutions;
  }

  @Override
  public void complete(ActivityExecution scopeExecution) {
    // can't happen
  }

  @Override
  public List<ActivityExecution> initializeScope(ActivityExecution scopeExecution, int numberOfInstances) {

    prepareScopeExecution(scopeExecution, numberOfInstances);

    List<ActivityExecution> executions = new ArrayList<ActivityExecution>();
    for (int i = 0; i < numberOfInstances; i++) {
      ActivityExecution concurrentChild = createConcurrentExecution(scopeExecution);
      setLoopVariable(concurrentChild, LOOP_COUNTER, i);
      executions.add(concurrentChild);
    }

    return executions;
  }

  @Override
  public ActivityExecution createInnerInstance(ActivityExecution scopeExecution) {
    // even though there is only one instance, there is always a concurrent child
    ActivityExecution concurrentChild = createConcurrentExecution(scopeExecution);

    int nrOfInstances = getLoopVariable(scopeExecution, NUMBER_OF_INSTANCES);
    setLoopVariable(scopeExecution, NUMBER_OF_INSTANCES, nrOfInstances + 1);
    int nrOfActiveInstances = getLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES);
    setLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances + 1);

    setLoopVariable(concurrentChild, LOOP_COUNTER, nrOfInstances);

    return concurrentChild;
  }

  @Override
  public void destroyInnerInstance(ActivityExecution concurrentExecution) {

    ActivityExecution scopeExecution = concurrentExecution.getParent();
    concurrentExecution.remove();
    scopeExecution.forceUpdate();

    int nrOfActiveInstances = getLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES);
    setLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances - 1);

  }

  @Override
  public void migrateScope(ActivityExecution scopeExecution) {
    // migrate already completed instances
    for (ActivityExecution child : scopeExecution.getExecutions()) {
      if (!child.isActive()) {
        ((PvmExecutionImpl) child).setProcessDefinition(((PvmExecutionImpl) scopeExecution).getProcessDefinition());
      }
    }
  }

  @Override
  public void onParseMigratingInstance(MigratingInstanceParseContext parseContext, MigratingActivityInstance migratingInstance) {
    ExecutionEntity scopeExecution = migratingInstance.resolveRepresentativeExecution();

    List<ActivityExecution> concurrentInActiveExecutions =
        scopeExecution.findInactiveChildExecutions(getInnerActivity((ActivityImpl) migratingInstance.getSourceScope()));

    // variables on ended inner instance executions need not be migrated anywhere
    // since they are also not represented in the tree of migrating instances, we remove
    // them from the parse context here to avoid a validation exception
    for (ActivityExecution execution : concurrentInActiveExecutions) {
      for (VariableInstanceEntity variable : ((ExecutionEntity) execution).getVariablesInternal()) {
        parseContext.consume(variable);
      }
    }

  }

}
