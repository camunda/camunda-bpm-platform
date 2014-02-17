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
package org.camunda.bpm.engine.impl.pvm.runtime;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;


/**
 * @author Daniel Meyer
 *
 */
public abstract class AtomicOperationActivityInstanceEnd extends AbstractEventAtomicOperation {

  private final static Logger log = Logger.getLogger(AtomicOperationActivityInstanceEnd.class.getName());

  @Override
  protected InterpretableExecution eventNotificationsStarted(InterpretableExecution execution) {

    // hack around execution tree structure not being in sync with activity instance concept:
    // if we end a scope activity, take remembered activity instance from parent and set on
    // execution before calling END listeners.
    ActivityExecution parent = execution.getParent();
    ActivityImpl activity = (ActivityImpl)execution.getActivity();
    if(parent != null && execution.isScope() && activity != null && activity.isScope() && (activity.getActivityBehavior() instanceof CompositeActivityBehavior)) {

      if(log.isLoggable(Level.FINE)) {
        log.fine("[LEAVE] "+ execution + ": "+execution.getActivityInstanceId() );
      }

      // use remembered activity instance id from parent
      execution.setActivityInstanceId(parent.getActivityInstanceId());
      // make parent go one scope up.
      parent.leaveActivityInstance();

    }

    return execution;

  }

  @Override
  protected void eventNotificationsCompleted(InterpretableExecution execution) {
    // make execution leave the activity instance
    execution.leaveActivityInstance();
  }

  protected boolean isSkipNotifyListeners(InterpretableExecution execution) {
    // listeners are skipped if this execution is not part of an activity instance.
    return execution.getActivityInstanceId() == null;
  }

}
