package org.camunda.bpm.engine.cdi.test.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.cdi.test.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;

/**
 * @author Michael Scholz
 */
public class ProcessVariableMapTest extends CdiProcessEngineTestCase {

  private static final String VARNAME_1 = "aVariable";
  private static final String VARNAME_2 = "anotherVariable";

  @Test
  public void testProcessVariableMap() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    VariableMap variables = (VariableMap) getBeanInstance("processVariableMap");
    assertNotNull(variables);

    ///////////////////////////////////////////////////////////////////
    // Put a variable via BusinessProcess and get it via VariableMap //
    ///////////////////////////////////////////////////////////////////
    String aValue = "aValue";
    businessProcess.setVariable(VARNAME_1, Variables.stringValue(aValue));

    // Legacy API
    assertEquals(aValue, variables.get(VARNAME_1));

    // Typed variable API
    TypedValue aTypedValue = variables.getValueTyped(VARNAME_1);
    assertEquals(ValueType.STRING, aTypedValue.getType());
    assertEquals(aValue, aTypedValue.getValue());
    assertEquals(aValue, variables.getValue(VARNAME_1, String.class));

    // Type API with wrong type
    try {
      variables.getValue(VARNAME_1, Integer.class);
      fail("ClassCastException expected!");
    } catch(ClassCastException ex) {
      assertEquals("Cannot cast variable named 'aVariable' with value 'aValue' to type 'class java.lang.Integer'.", ex.getMessage());
    }

    ///////////////////////////////////////////////////////////////////
    // Put a variable via VariableMap and get it via BusinessProcess //
    ///////////////////////////////////////////////////////////////////
    String anotherValue = "anotherValue";
    variables.put(VARNAME_2, Variables.stringValue(anotherValue));

    // Legacy API
    assertEquals(anotherValue, businessProcess.getVariable(VARNAME_2));

    // Typed variable API
    TypedValue anotherTypedValue = businessProcess.getVariableTyped(VARNAME_2);
    assertEquals(ValueType.STRING, anotherTypedValue.getType());
    assertEquals(anotherValue, anotherTypedValue.getValue());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testProcessVariableMapLocal() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);
    businessProcess.startProcessByKey("businessProcessBeanTest");

    VariableMap variables = (VariableMap) getBeanInstance("processVariableMapLocal");
    assertNotNull(variables);

    ///////////////////////////////////////////////////////////////////
    // Put a variable via BusinessProcess and get it via VariableMap //
    ///////////////////////////////////////////////////////////////////
    String aValue = "aValue";
    businessProcess.setVariableLocal(VARNAME_1, Variables.stringValue(aValue));

    // Legacy API
    assertEquals(aValue, variables.get(VARNAME_1));

    // Typed variable API
    TypedValue aTypedValue = variables.getValueTyped(VARNAME_1);
    assertEquals(ValueType.STRING, aTypedValue.getType());
    assertEquals(aValue, aTypedValue.getValue());
    assertEquals(aValue, variables.getValue(VARNAME_1, String.class));

    // Type API with wrong type
    try {
      variables.getValue(VARNAME_1, Integer.class);
      fail("ClassCastException expected!");
    } catch(ClassCastException ex) {
      assertEquals("Cannot cast variable named 'aVariable' with value 'aValue' to type 'class java.lang.Integer'.", ex.getMessage());
    }

    ///////////////////////////////////////////////////////////////////
    // Put a variable via VariableMap and get it via BusinessProcess //
    ///////////////////////////////////////////////////////////////////
    String anotherValue = "anotherValue";
    variables.put(VARNAME_2, Variables.stringValue(anotherValue));

    // Legacy API
    assertEquals(anotherValue, businessProcess.getVariableLocal(VARNAME_2));

    // Typed variable API
    TypedValue anotherTypedValue = businessProcess.getVariableLocalTyped(VARNAME_2);
    assertEquals(ValueType.STRING, anotherTypedValue.getType());
    assertEquals(anotherValue, anotherTypedValue.getValue());
  }
}
