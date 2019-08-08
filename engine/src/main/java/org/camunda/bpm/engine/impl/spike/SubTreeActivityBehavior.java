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
package org.camunda.bpm.engine.impl.spike;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.Callback;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

public class SubTreeActivityBehavior extends AbstractBpmnActivityBehavior implements CompositeActivityBehavior {

  public static final String LOOP_RANGE_START = "loopRangeStart";
  public static final String LOOP_RANGE_END = "loopRangeEnd";
  public static final String INSTANCES_FINISHED = "subTreeFinished";

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    int rangeStart = (int) execution.getVariable(LOOP_RANGE_START);
    int rangeEnd = (int) execution.getVariable(LOOP_RANGE_END);
    execution.setVariableLocal(INSTANCES_FINISHED, 0);

    PvmActivity childActivity = execution.getActivity().getActivities().get(0);

    List<ActivityExecution> children = new ArrayList<>();
    for (int i = rangeStart; i <= rangeEnd; i++) {
      ActivityExecution child = createConcurrentExecution(execution);
      children.add(child);
    }

    for (int i = rangeStart; i <= rangeEnd; i++) {
      ActivityExecution child = children.get(i - rangeStart);
      performInstance(child, childActivity, i);
    }

  }


  protected void performInstance(ActivityExecution execution, PvmActivity activity, int loopCounter) {
    execution.setVariableLocal(MultiInstanceActivityBehavior.LOOP_COUNTER, loopCounter);
//    evaluateCollectionVariable(execution, loopCounter);
    execution.setEnded(false);
    execution.setActive(true);
    execution.executeActivity(activity);
  }


  protected ActivityExecution createConcurrentExecution(ActivityExecution scopeExecution) {
    ActivityExecution concurrentChild = scopeExecution.createExecution();
    scopeExecution.forceUpdate();
    concurrentChild.setConcurrent(true);
    concurrentChild.setScope(false);
    return concurrentChild;
  }


  @Override
  public void concurrentChildExecutionEnded(ActivityExecution scopeExecution,
      ActivityExecution endedExecution) {

    ActivityExecution multiInstanceScopeExecution = scopeExecution.getParent().getParent();

    int nrOfCompletedInstances = (int) multiInstanceScopeExecution.getVariableLocal(MultiInstanceActivityBehavior.NUMBER_OF_COMPLETED_INSTANCES) + 1;
    multiInstanceScopeExecution.setVariableLocal(MultiInstanceActivityBehavior.NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances);
    int nrOfActiveInstances = (int) multiInstanceScopeExecution.getVariableLocal(MultiInstanceActivityBehavior.NUMBER_OF_ACTIVE_INSTANCES) - 1;
    multiInstanceScopeExecution.setVariableLocal(MultiInstanceActivityBehavior.NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances);
    int instancesFinished = (int) scopeExecution.getVariableLocal(INSTANCES_FINISHED) + 1;
    scopeExecution.setVariableLocal(INSTANCES_FINISHED, instancesFinished);

    // inactivate the concurrent execution
    endedExecution.inactivate();
    endedExecution.setActivityInstanceId(null);

    // join
    scopeExecution.forceUpdate();
    // TODO: should the completion condition be evaluated on the scopeExecution or on the endedExecution?
    if( //completionConditionSatisfied(endedExecution) ||
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


  private boolean allExecutionsEnded(ActivityExecution scopeExecution,
      ActivityExecution endedExecution) {
    int instancesFinished = (int) scopeExecution.getVariableLocal(INSTANCES_FINISHED);
    int loopStart = (int) scopeExecution.getVariable(LOOP_RANGE_START);
    int loopEnd = (int) scopeExecution.getVariable(LOOP_RANGE_END);

    int nrOfInstances = loopEnd - loopStart + 1;

    // TODO: is it necessary to count inactive sibling executions as well?

    return instancesFinished == nrOfInstances;
  }


  @Override
  public void complete(ActivityExecution scopeExecution) {
    throw new RuntimeException("not implemented");

  }

}
