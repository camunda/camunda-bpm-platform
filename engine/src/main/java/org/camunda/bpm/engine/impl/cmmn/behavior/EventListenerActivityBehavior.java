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
package org.camunda.bpm.engine.impl.cmmn.behavior;

import org.camunda.bpm.engine.exception.cmmn.CaseIllegalStateTransitionException;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;

/**
 * @author Roman Smirnov
 *
 */
public class EventListenerActivityBehavior extends EventListenerOrMilestoneActivityBehavior {

  public void created(CmmnActivityExecution execution) {
    // TODO: implement this:

    // (1) in case of a UserEventListener there is nothing to do!

    // (2) in case of TimerEventListener we have to check
    // whether the timer must be triggered, when a transition
    // on another plan item or case file item happens!
  }

  protected String getTypeName() {
    return "event listener";
  }

  protected boolean isAtLeastOneEntryCriteriaSatisfied(CmmnActivityExecution execution) {
    return false;
  }

  public void fireEntryCriteria(CmmnActivityExecution execution) {
    throw new CaseIllegalStateTransitionException("Cannot trigger case execution '"+execution.getId()+"': entry criteria are not allowed for event listener.");
  }

}
