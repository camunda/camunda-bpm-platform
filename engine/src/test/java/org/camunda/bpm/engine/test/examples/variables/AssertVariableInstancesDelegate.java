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
package org.camunda.bpm.engine.test.examples.variables;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.PersistentVariableInstance;
import org.camunda.bpm.engine.delegate.ProcessEngineVariableType;
import org.junit.Assert;

/**
 * @author Thorben Lindhauer
 */
public class AssertVariableInstancesDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    // set an additional local variable
    execution.setVariableLocal("aStringVariable", "aStringValue");

    // getVariableInstances()
    Map<String, PersistentVariableInstance> variableInstances = execution.getVariableInstances();

    Assert.assertEquals(2, variableInstances.size());

    PersistentVariableInstance intVariable = variableInstances.get("anIntegerVariable");
    PersistentVariableInstance stringVariable = variableInstances.get("aStringVariable");

    VariableAssertionUtil.assertVariableHasValueAndType(intVariable, 1234, ProcessEngineVariableType.INTEGER.getName());
    VariableAssertionUtil.assertVariableHasValueAndType(stringVariable, "aStringValue", ProcessEngineVariableType.STRING.getName());

    // getVariableInstancesLocal()
    variableInstances = execution.getVariableInstancesLocal();

    Assert.assertEquals(1, variableInstances.size());

    stringVariable = variableInstances.get("aStringVariable");

    VariableAssertionUtil.assertVariableHasValueAndType(stringVariable, "aStringValue", ProcessEngineVariableType.STRING.getName());

    // getVariableInstance(name)
    intVariable = execution.getVariableInstance("anIntegerVariable");

    Assert.assertNotNull(intVariable);
    VariableAssertionUtil.assertVariableHasValueAndType(intVariable, 1234, ProcessEngineVariableType.INTEGER.getName());

    // getVariableInstanceLocal(name)
    stringVariable = execution.getVariableInstanceLocal("aStringVariable");

    Assert.assertNotNull(stringVariable);
    VariableAssertionUtil.assertVariableHasValueAndType(stringVariable, "aStringValue", ProcessEngineVariableType.STRING.getName());

    intVariable = execution.getVariableInstanceLocal("anIntegerVariable");
    Assert.assertNull(intVariable);
  }


}
