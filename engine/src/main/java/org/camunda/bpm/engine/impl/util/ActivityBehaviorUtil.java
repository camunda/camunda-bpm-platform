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
package org.camunda.bpm.engine.impl.util;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Roman Smirnov
 *
 */
public class ActivityBehaviorUtil {

  public static CmmnActivityBehavior getActivityBehavior(CmmnExecution execution) {
    String id = execution.getId();

    CmmnActivity activity = execution.getActivity();
    ensureNotNull(PvmException.class, "Case execution '"+id+"' has no current activity.", "activity", activity);

    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    ensureNotNull(PvmException.class, "There is no behavior specified in "+activity+" for case execution '"+id+"'.", "behavior", behavior);

    return behavior;
  }

  public static ActivityBehavior getActivityBehavior(PvmExecutionImpl execution) {
    String id = execution.getId();

    PvmActivity activity = execution.getActivity();
    ensureNotNull(PvmException.class, "Execution '"+id+"' has no current activity.", "activity", activity);

    ActivityBehavior behavior = activity.getActivityBehavior();
    ensureNotNull(PvmException.class, "There is no behavior specified in "+activity+" for execution '"+id+"'.", "behavior", behavior);

    return behavior;
  }

}
