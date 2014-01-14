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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public class IntermediateCatchEventActivityBehavior extends AbstractBpmnActivityBehavior {

  protected boolean isAfterEventBasedGateway;

  public IntermediateCatchEventActivityBehavior(boolean isAfterEventBasedGateway) {
    this.isAfterEventBasedGateway = isAfterEventBasedGateway;
  }

  public void execute(ActivityExecution execution) throws Exception {
    if (isAfterEventBasedGateway) {
      leave(execution);

    } else {
      // Do nothing: waitstate behavior
    }
  }

  public boolean isAfterEventBasedGateway() {
    return isAfterEventBasedGateway;
  }

  @Override
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    leave(execution);
  }
}

