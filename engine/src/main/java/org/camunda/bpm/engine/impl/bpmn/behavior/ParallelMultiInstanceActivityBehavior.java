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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Daniel Meyer
 *
 */
public class ParallelMultiInstanceActivityBehavior extends MultiInstanceActivityBehavior {

  @Override
  protected void createInstances(ActivityExecution execution, int nrOfInstances) throws Exception {
    PvmActivity innerActivity = getInnerActivity(execution);

    prepareScopeExecution(execution, nrOfInstances);

    // create the concurrent child executions
    List<ActivityExecution> concurrentExecutions = new ArrayList<ActivityExecution>();
    for (int i = 0; i < nrOfInstances; i++) {
      ActivityExecution concurrentChild = execution.createExecution();
      concurrentChild.setConcurrent(true);
      concurrentChild.setScope(false);
      concurrentExecutions.add(concurrentChild);
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
    }
  }

  protected boolean allExecutionsEnded(ActivityExecution scopeExecution, ActivityExecution endedExecution) {
    return getLocalLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES) <= 0;
  }

  @Override
  public void complete(ActivityExecution scopeExecution) {
    // can't happen
  }

  @Override
  public ActivityExecution initializeScope(ActivityExecution scopeExecution) {

    prepareScopeExecution(scopeExecution, 1);

    // even though there is only one instance, there is always a concurrent child
    ActivityExecution concurrentChild = scopeExecution.createExecution();
    concurrentChild.setConcurrent(true);
    concurrentChild.setScope(false);

    setLoopVariable(concurrentChild, LOOP_COUNTER, 0);

    return concurrentChild;
  }

  @Override
  public void concurrentExecutionCreated(ActivityExecution scopeExecution, ActivityExecution concurrentExecution) {
    int nrOfInstances = getLoopVariable(scopeExecution, NUMBER_OF_INSTANCES);
    setLoopVariable(scopeExecution, NUMBER_OF_INSTANCES, nrOfInstances + 1);
    int nrOfActiveInstances = getLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES);
    setLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances + 1);

    setLoopVariable(concurrentExecution, LOOP_COUNTER, nrOfInstances);
  }

  @Override
  public void concurrentExecutionDeleted(ActivityExecution scopeExecution, ActivityExecution concurrentExecution) {
    int nrOfActiveInstances = getLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES);
    setLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances - 1);
  }

}
