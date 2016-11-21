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
package org.camunda.bpm.engine.impl.pvm.runtime.operation;

import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.PvmLogger;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.Callback;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

import static org.camunda.bpm.engine.impl.util.ActivityBehaviorUtil.getActivityBehavior;

/**
 * @author Tom Baeyens
 */
public class PvmAtomicOperationActivityExecute implements PvmAtomicOperation {

  private final static PvmLogger LOG = PvmLogger.PVM_LOGGER;

  public boolean isAsync(PvmExecutionImpl execution) {
    return false;
  }

  public void execute(PvmExecutionImpl execution) {
    execution.activityInstanceStarted();

    execution.continueIfExecutionDoesNotAffectNextOperation(new Callback<PvmExecutionImpl, Void>() {
      @Override
      public Void callback(PvmExecutionImpl execution) {
        if (execution.getActivity().isScope()) {
          execution.dispatchEvent(null);
        }
        return null;
      }
    }, new Callback<PvmExecutionImpl, Void>() {

      @Override
      public Void callback(PvmExecutionImpl execution) {

        ActivityBehavior activityBehavior = getActivityBehavior(execution);

        ActivityImpl activity = execution.getActivity();
        LOG.debugExecutesActivity(execution, activity, activityBehavior.getClass().getName());

        try {
          activityBehavior.execute(execution);
        } catch (RuntimeException e) {
          throw e;
        } catch (Exception e) {
          throw new PvmException("couldn't execute activity <" + activity.getProperty("type") + " id=\"" + activity.getId() + "\" ...>: " + e.getMessage(), e);
        }
        return null;
      }
    }, execution);
  }

  public String getCanonicalName() {
    return "activity-execute";
  }

  public boolean isAsyncCapable() {
    return false;
  }
}
