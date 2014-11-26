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
package org.camunda.bpm.engine.cdi.test.api.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.cdi.test.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.cdi.test.impl.beans.DeclarativeProcessController;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessVariableLocalTypedTest extends CdiProcessEngineTestCase {

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/cdi/test/api/annotation/CompleteTaskTest.bpmn20.xml")
  public void testProcessVariableLocalTypeAnnotation() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    VariableMap variables = Variables.createVariables().putValue("injectedLocalValue", "camunda");
    businessProcess.startProcessByKey("keyOfTheProcess", variables);

    TypedValue value = getBeanInstance(DeclarativeProcessController.class).getInjectedLocalValue();
    assertNotNull(value);
    assertTrue(value instanceof StringValue);
    assertEquals(ValueType.STRING, value.getType());
    assertEquals("camunda", value.getValue());
  }

}
