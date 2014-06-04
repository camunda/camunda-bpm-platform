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
package org.camunda.bpm.engine.test.cmmn.operation;

import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ACTIVE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.AVAILABLE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.CLOSED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.COMPLETED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.DISABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ENABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.FAILED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.SUSPENDED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionStateTransitionCollector implements CaseExecutionListener {

  private static Logger log = Logger.getLogger(CaseExecutionStateTransitionCollector.class.getName());

  protected static Map<Integer, CaseExecutionState> states;

  static {
    states = new HashMap<Integer, CaseExecutionState>();

    states.put(ACTIVE.getStateCode(), ACTIVE);
    states.put(AVAILABLE.getStateCode(), AVAILABLE);
    states.put(CLOSED.getStateCode(), CLOSED);
    states.put(COMPLETED.getStateCode(), COMPLETED);
    states.put(DISABLED.getStateCode(), DISABLED);
    states.put(ENABLED.getStateCode(), ENABLED);
    states.put(FAILED.getStateCode(), FAILED);
    states.put(SUSPENDED.getStateCode(), SUSPENDED);
    states.put(TERMINATED.getStateCode(), TERMINATED);
  }

  public List<String> stateTransitions = new ArrayList<String>();

  public void notify(DelegateCaseExecution planItem) throws Exception {
    CmmnExecution execution = (CmmnExecution) planItem;

    String activityId = execution.getEventSource().getId();

    CaseExecutionState previousState = states.get(execution.getPreviousState());
    String previousStateName = "()";
    if (previousState != null) {
      previousStateName = previousState.toString();
    }

    CaseExecutionState newState = states.get(execution.getCurrentState());

    String stateTransition = previousStateName + " --" + execution.getEventName() + "(" + activityId + ")--> " + newState;

    log.fine("collecting state transition: " +  stateTransition);

    stateTransitions.add(stateTransition);
  }

  public String toString() {
    StringBuilder text = new StringBuilder();
    for (String event: stateTransitions) {
      text.append(event);
      text.append("\n");
    }
    return text.toString();

  }

}
