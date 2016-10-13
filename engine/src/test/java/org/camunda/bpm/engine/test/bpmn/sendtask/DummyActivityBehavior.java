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
package org.camunda.bpm.engine.test.bpmn.sendtask;

import org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

public class DummyActivityBehavior extends TaskActivityBehavior {

  public static boolean wasExecuted = false;

  public static String currentActivityId = null;
  public static String currentActivityName = null;

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    currentActivityName = execution.getCurrentActivityName();
    currentActivityId = execution.getCurrentActivityId();
    leave(execution);
  }

  @Override
  public void performExecution(ActivityExecution execution) throws Exception {
    wasExecuted = true;
  }

}
