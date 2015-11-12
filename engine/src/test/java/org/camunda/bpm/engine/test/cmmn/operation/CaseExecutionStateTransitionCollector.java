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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.test.TestLogger;
import org.slf4j.Logger;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionStateTransitionCollector implements CaseExecutionListener {

  private final static Logger LOG = TestLogger.TEST_LOGGER.getLogger();

  public List<String> stateTransitions = new ArrayList<String>();

  public void notify(DelegateCaseExecution planItem) throws Exception {
    CmmnExecution execution = (CmmnExecution) planItem;

    String activityId = execution.getEventSource().getId();

    CaseExecutionState previousState = execution.getPreviousState();
    String previousStateName = "()";
    if (!previousState.equals(CaseExecutionState.NEW)) {
      previousStateName = previousState.toString();
    }

    CaseExecutionState newState = execution.getCurrentState();

    String stateTransition = previousStateName + " --" + execution.getEventName() + "(" + activityId + ")--> " + newState;

    LOG.debug("collecting state transition: " +  stateTransition);

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
