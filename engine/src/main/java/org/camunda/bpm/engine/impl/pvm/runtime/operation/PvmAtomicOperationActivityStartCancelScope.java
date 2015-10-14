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

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * Cancel scope operation performed when an execution starts at an {@link ActivityImpl#isCancelActivity()}
 * activity. This is used when an execution is set to the activity without entering it through a transition.
 * See {@link PvmAtomicOperationCancelActivity} for more details on "cancel scope" behavior.
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public class PvmAtomicOperationActivityStartCancelScope extends PvmAtomicOperationCancelActivity {

  public String getCanonicalName() {
    return "activity-start-cancel-scope";
  }

  protected void activityCancelled(PvmExecutionImpl execution) {
    execution.setActivityInstanceId(null);
    execution.performOperation(ACTIVITY_START_CREATE_SCOPE);
  }

  protected PvmActivity getCancellingActivity(PvmExecutionImpl execution) {
    return execution.getNextActivity();
  }

  public boolean isAsyncCapable() {
    return false;
  }

}
