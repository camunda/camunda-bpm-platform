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

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 *
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public class SequentialMultiInstanceActivityBehavior extends MultiInstanceActivityBehavior {

  protected void createInstances(ActivityExecution execution, int nrOfInstances) throws Exception {
    setLoopVariable(execution, NUMBER_OF_INSTANCES, nrOfInstances);
    setLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, 0);
    setLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES, 1);

    performInstance(execution, 0);
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
      performInstance(scopeExecution, loopCounter);
    }
  }

  public void concurrentChildExecutionEnded(ActivityExecution scopeExecution, ActivityExecution endedExecution) {
    // cannot happen
  }

}
