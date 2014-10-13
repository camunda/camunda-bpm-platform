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
package org.camunda.bpm.engine.test.cmmn.listener;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateCaseVariableInstance;
import org.camunda.bpm.engine.delegate.CaseVariableListener;

/**
 * @author Thorben Lindhauer
 *
 */
public class LogAndUpdateVariableListener implements CaseVariableListener {

  protected static List<DelegateCaseVariableInstance> invocations = new ArrayList<DelegateCaseVariableInstance>();

  public void notify(DelegateCaseVariableInstance variableInstance) throws Exception {
    invocations.add(variableInstance);

    if ("variable".equals(variableInstance.getName()) && "value1".equals(variableInstance.getValue())) {
      variableInstance.getSourceExecution().setVariable("variable", "value2");
    }
  }

  public static List<DelegateCaseVariableInstance> getInvocations() {
    return invocations;
  }

  public static void reset() {
    invocations = new ArrayList<DelegateCaseVariableInstance>();
  }



}
