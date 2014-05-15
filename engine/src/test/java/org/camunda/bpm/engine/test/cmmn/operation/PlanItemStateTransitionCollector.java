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

import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.ACTIVE;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.AVAILABLE;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.CLOSED;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.COMPLETED;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.DISABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.ENABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.FAILED;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.SUSPENDED;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.TERMINATED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegatePlanItem;
import org.camunda.bpm.engine.delegate.PlanItemListener;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState;

/**
 * @author Roman Smirnov
 *
 */
public class PlanItemStateTransitionCollector implements PlanItemListener {

  private static Logger log = Logger.getLogger(PlanItemStateTransitionCollector.class.getName());

  protected static Map<Integer, PlanItemState> states;

  static {
    states = new HashMap<Integer, PlanItemState>();

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

  public void notify(DelegatePlanItem planItem) throws Exception {
    CmmnExecution execution = (CmmnExecution) planItem;

    String activityId = execution.getEventSource().getId();

    PlanItemState previousState = states.get(execution.getPreviousState());
    String previousStateName = "()";
    if (previousState != null) {
      previousStateName = previousState.toString();
    }

    PlanItemState newState = states.get(execution.getState());

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
