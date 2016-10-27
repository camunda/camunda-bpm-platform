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
package org.camunda.bpm.engine.impl.cmmn.operation;

import static org.camunda.bpm.engine.impl.util.ActivityBehaviorUtil.getActivityBehavior;

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.core.operation.AbstractEventAtomicOperation;

/**
 * @author Roman Smirnov
 *
 */
public abstract class AbstractCmmnEventAtomicOperation extends AbstractEventAtomicOperation<CmmnExecution> implements CmmnAtomicOperation {

  protected CmmnActivity getScope(CmmnExecution execution) {
    return execution.getActivity();
  }

  public boolean isAsync(CmmnExecution execution) {
    return false;
  }

  protected void eventNotificationsCompleted(CmmnExecution execution) {
    repetition(execution);
    preTransitionNotification(execution);
    performTransitionNotification(execution);
    postTransitionNotification(execution);
  }

  protected void repetition(CmmnExecution execution) {
    CmmnActivityBehavior behavior = getActivityBehavior(execution);
    behavior.repeat(execution, getEventName());
  }

  protected void preTransitionNotification(CmmnExecution execution) {

  }

  protected void performTransitionNotification(CmmnExecution execution) {
    String eventName = getEventName();

    CmmnExecution parent = execution.getParent();

    if (parent != null) {
      parent.handleChildTransition(execution, eventName);
    }
  }

  protected void postTransitionNotification(CmmnExecution execution) {
    // noop
  }

}
