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
package org.camunda.bpm.engine.test.api.runtime.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;

/**
 * @author Thorben Lindhauer
 */
public class AssertVariableInstancesDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {

    // validate integer variable
    Integer expectedIntValue = 1234;
    assertEquals(expectedIntValue, execution.getVariable("anIntegerVariable"));
    assertEquals(expectedIntValue, execution.getVariableTyped("anIntegerVariable").getValue());
    assertEquals(ValueType.INTEGER, execution.getVariableTyped("anIntegerVariable").getType());
    assertNull(execution.getVariableLocal("anIntegerVariable"));
    assertNull(execution.getVariableLocalTyped("anIntegerVariable"));

    // set an additional local variable
    execution.setVariableLocal("aStringVariable", "aStringValue");

    String expectedStringValue = "aStringValue";
    assertEquals(expectedStringValue, execution.getVariable("aStringVariable"));
    assertEquals(expectedStringValue, execution.getVariableTyped("aStringVariable").getValue());
    assertEquals(ValueType.STRING, execution.getVariableTyped("aStringVariable").getType());
    assertEquals(expectedStringValue, execution.getVariableLocal("aStringVariable"));
    assertEquals(expectedStringValue, execution.getVariableLocalTyped("aStringVariable").getValue());
    assertEquals(ValueType.STRING, execution.getVariableLocalTyped("aStringVariable").getType());

    SimpleSerializableBean objectValue = (SimpleSerializableBean) execution.getVariable("anObjectValue");
    assertNotNull(objectValue);
    assertEquals(10, objectValue.getIntProperty());
    ObjectValue variableTyped = execution.getVariableTyped("anObjectValue");
    assertEquals(10, variableTyped.getValue(SimpleSerializableBean.class).getIntProperty());
    assertEquals(Variables.SerializationDataFormats.JAVA.getName(), variableTyped.getSerializationDataFormat());

    objectValue = (SimpleSerializableBean) execution.getVariable("anUntypedObjectValue");
    assertNotNull(objectValue);
    assertEquals(30, objectValue.getIntProperty());
    variableTyped = execution.getVariableTyped("anUntypedObjectValue");
    assertEquals(30, variableTyped.getValue(SimpleSerializableBean.class).getIntProperty());
    assertEquals(Context.getProcessEngineConfiguration().getDefaultSerializationFormat(), variableTyped.getSerializationDataFormat());

  }


}
