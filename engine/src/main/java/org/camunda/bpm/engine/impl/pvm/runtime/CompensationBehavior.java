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

import java.util.Map;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * Contains the oddities required by compensation due to the execution structures it creates.
 * Anything that is a cross-cutting concern, but requires some extra compensation-specific conditions, should go here.
 *
 * @author Thorben Lindhauer
 */
public class CompensationBehavior {

  /**
   * With compensation, we have a dedicated scope execution for every handler, even if the handler is not
   * a scope activity; this must be respected when invoking end listeners, etc.
   */
  public static boolean executesNonScopeCompensationHandler(PvmExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();

    return execution.isScope() && activity != null && activity.isCompensationHandler() && !activity.isScope();
  }

  public static boolean isCompensationThrowing(PvmExecutionImpl execution) {
    ActivityImpl currentActivity = execution.getActivity();
    if (currentActivity != null) {
      Boolean isCompensationThrowing = (Boolean) currentActivity.getProperty(BpmnParse.PROPERTYNAME_THROWS_COMPENSATION);
      if (isCompensationThrowing != null && isCompensationThrowing) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether an execution is responsible for default compensation handling.
   *
   * This is the case if
   * <ul>
   *   <li>the execution has an activity
   *   <li>the execution is a scope
   *   <li>the activity is a scope
   *   <li>the execution has children
   *   <li>the execution does not throw compensation
   * </ul>
   */
  public static boolean executesDefaultCompensationHandler(PvmExecutionImpl scopeExecution) {
    ActivityImpl currentActivity = scopeExecution.getActivity();

    if (currentActivity != null) {
      return scopeExecution.isScope()
          && currentActivity.isScope()
          && !scopeExecution.getNonEventScopeExecutions().isEmpty()
          && !isCompensationThrowing(scopeExecution);
    }
    else {
      return false;
    }
  }

  public static String getParentActivityInstanceId(PvmExecutionImpl execution) {
    Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping = execution.createActivityExecutionMapping();
    PvmExecutionImpl parentScopeExecution = activityExecutionMapping.get(execution.getActivity().getFlowScope());

    return parentScopeExecution.getParentActivityInstanceId();
  }

}
