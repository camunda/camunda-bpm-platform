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
package org.camunda.bpm.engine.test.bpmn.event.conditional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * @author smirnov
 *
 */
public class SetMultipleSameVariableDelegate implements JavaDelegate {

  private static final String VARIABLE_NAME = AbstractConditionalEventTestCase.VARIABLE_NAME + "2";

  public void execute(DelegateExecution execution) throws Exception {
    execution.getProcessInstance().setVariable(VARIABLE_NAME, 1);
    execution.getProcessInstance().setVariable(VARIABLE_NAME, 1);
    execution.getProcessInstance().setVariable(VARIABLE_NAME, 1);
  }
}
