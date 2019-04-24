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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 *
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public class SequentialMultiInstanceActivityBehavior extends MultiInstanceActivityBehavior {

  protected static final BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  @Override
  protected void createInstances(ActivityExecution execution, int nrOfInstances) throws Exception {

    prepareScope(execution, nrOfInstances);
    setLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES, 1);

    ActivityImpl innerActivity = getInnerActivity(execution.getActivity());
    performInstance(execution, innerActivity, 0);
  }

  public void complete(ActivityExecution scopeExecution) {
    int loopCounter = getLoopVariable(scopeExecution, LOOP_COUNTER) + 1;
    int nrOfInstances = getLoopVariable(scopeExecution, NUMBER_OF_INSTANCES);
    int nrOfCompletedInstances = getLoopVariable(scopeExecution, NUMBER_OF_COMPLETED_INSTANCES) + 1;

    setLoopVariable(scopeExecution, NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances);

    if (loopCounter == nrOfInstances || completionConditionSatisfied(scopeExecution)) {
      leave(scopeExecution);
    }
    else {
      PvmActivity innerActivity = getInnerActivity(scopeExecution.getActivity());
      performInstance(scopeExecution, innerActivity, loopCounter);
    }
  }

  public void concurrentChildExecutionEnded(ActivityExecution scopeExecution, ActivityExecution endedExecution) {
    // cannot happen
  }

  protected void prepareScope(ActivityExecution scopeExecution, int totalNumberOfInstances) {
    setLoopVariable(scopeExecution, NUMBER_OF_INSTANCES, totalNumberOfInstances);
    setLoopVariable(scopeExecution, NUMBER_OF_COMPLETED_INSTANCES, 0);
  }

  public List<ActivityExecution> initializeScope(ActivityExecution scopeExecution, int nrOfInstances) {
    if (nrOfInstances > 1) {
      LOG.unsupportedConcurrencyException(scopeExecution.toString(), this.getClass().getSimpleName());
    }

    List<ActivityExecution> executions = new ArrayList<ActivityExecution>();

    prepareScope(scopeExecution, nrOfInstances);
    setLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES, nrOfInstances);

    if (nrOfInstances > 0) {
      setLoopVariable(scopeExecution, LOOP_COUNTER, 0);
      executions.add(scopeExecution);
    }

    return executions;
  }

  @Override
  public ActivityExecution createInnerInstance(ActivityExecution scopeExecution) {

    if (hasLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES) && getLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES) > 0) {
      throw LOG.unsupportedConcurrencyException(scopeExecution.toString(), this.getClass().getSimpleName());
    }
    else {
      int nrOfInstances = getLoopVariable(scopeExecution, NUMBER_OF_INSTANCES);

      setLoopVariable(scopeExecution, LOOP_COUNTER, nrOfInstances);
      setLoopVariable(scopeExecution, NUMBER_OF_INSTANCES, nrOfInstances + 1);
      setLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES, 1);
    }

    return scopeExecution;
  }

  @Override
  public void destroyInnerInstance(ActivityExecution scopeExecution) {
    removeLoopVariable(scopeExecution, LOOP_COUNTER);

    int nrOfActiveInstances = getLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES);
    setLoopVariable(scopeExecution, NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances - 1);
  }

}
