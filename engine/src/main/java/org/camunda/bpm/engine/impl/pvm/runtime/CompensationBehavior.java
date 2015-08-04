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

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * Contains the oddities required by compensation
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
    return execution.isScope() && activity != null && isCompensationHandler(activity);
  }

  //TODO: consolidate with philipp's code
  protected static boolean isCompensationHandler(ScopeImpl activity) {
    Boolean isForCompensation = (Boolean) activity.getProperty(BpmnParse.PROPERTYNAME_IS_FOR_COMPENSATION);
    if (isForCompensation != null && isForCompensation) {
      return true;
    }
    else {
      return false;
    }
  }
}
