/*
 * Copyright 2016 camunda services GmbH.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.engine.impl.tree;

import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.ActivityInstanceState;

import java.util.ArrayList;
import java.util.List;

/**
 * Collector to collect all ActivityExecutionTuple, which have conditional events.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ConditionalEventCollector implements TreeVisitor<ActivityExecutionTuple> {

  /**
   * The already visited scopes, since we go from bottom to the top and we don't want to collect scopes/activities twice.
   */
  private final List<ScopeImpl> visited;

  /**
   * The current execution, on which the collection ends.
   */
  private ExecutionEntity currentExecution;

  /**
   * The collected tuple, which consist of the execution and the corresponding activity.
   */
  private final List<ActivityExecutionTuple> tuples = new ArrayList<ActivityExecutionTuple>();

  public ConditionalEventCollector(List<ScopeImpl> visited, ExecutionEntity currentExecution) {
    this.visited = visited;
    this.currentExecution = currentExecution;
  }

  @Override
  public void visit(ActivityExecutionTuple obj) {
    ExecutionEntity tupleExecution = (ExecutionEntity) obj.getExecution();
    ScopeImpl tupleScope = (ScopeImpl) obj.getScope();

    //If the current execution is not the process instance AND the tuple execution is the parent flow scope execution
    //-> we are to high and have to end.
    if (!currentExecution.isProcessInstanceExecution()
      && obj.getExecution() == currentExecution.getParent().getFlowScopeExecution()) {
      return;
    }

    //If the tuple execution is not in the default state AND is not compacted
    //OR the tuple scope activity is not a scope
    //-> we are not in a valid state and have to end.
    if ( (!tupleExecution.isInState(ActivityInstanceState.DEFAULT)
        && !tupleExecution.isCompacted())
      || !tupleScope.isScope()) {
      return;
    }

    //If the tuple scope activity is in the list of visited scopes
    //-> we was there already before and have to end.
    if (visited.contains(tupleScope)) {
      return;
    }

    //If the tuple execution is on transition AND the destination is the tuple scope activity
    //-> the conditional activity should not be triggered since we have not even reached the activity.
    TransitionImpl transition = tupleExecution.getTransition();
    if (transition != null && transition.getDestination() == tupleScope) {
        return;
    }

    //If the tuple scope activity contains conditional event definitions we collect the tuple.
    if (!tupleScope.getProperties().get(BpmnProperties.CONDITIONAL_EVENT_DEFINITIONS).isEmpty()) {
      tuples.add(obj);
    }
    visited.add(tupleScope);
  }

  public List<ActivityExecutionTuple> getTuples() {
    return tuples;
  }
}