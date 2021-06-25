/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.bpmn.scripttask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.Date;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;

public class ScriptTaskNashornTest extends AbstractScriptTaskTest {

  private static final String NASHORN = "nashorn";

  @Test
  public void testJavascriptProcessVarVisibility() {

    deployProcess(NASHORN,

        // GIVEN
        // an execution variable 'foo'
        "execution.setVariable('foo', 'a');"

        // THEN
        // there should be a script variable defined
      + "if (typeof foo !== 'undefined') { "
      + "  throw 'Variable foo should be defined as script variable.';"
      + "}"

        // GIVEN
        // a script variable with the same name
      + "var foo = 'b';"

        // THEN
        // it should not change the value of the execution variable
      + "if(execution.getVariable('foo') != 'a') {"
      + "  throw 'Execution should contain variable foo';"
      + "}"

        // AND
        // it should override the visibility of the execution variable
      + "if(foo != 'b') {"
      + "  throw 'Script variable must override the visibiltity of the execution variable.';"
      + "}"

    );

    // GIVEN
    // that we start an instance of this process
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // THEN
    // the script task can be executed without exceptions
    // the execution variable is stored and has the correct value
    Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
    assertEquals("a", variableValue);

  }

  @Test
  public void testJavascriptFunctionInvocation() {

    deployProcess(NASHORN,

        // GIVEN
        // a function named sum
        "function sum(a,b){"
      + "  return a+b;"
      + "};"

        // THEN
        // i can call the function
      + "var result = sum(1,2);"

      + "execution.setVariable('foo', result);"

    );

    // GIVEN
    // that we start an instance of this process
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // THEN
    // the variable is defined
    Object variable = runtimeService.getVariable(pi.getId(), "foo");
    assertThat(variable).isIn(3, 3.0);

  }

  @Test
  public void testJsVariable() {

    String scriptText = "var foo = 1;";

    deployProcess(NASHORN, scriptText);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");
    Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
    assertNull(variableValue);

  }

  @Test
  public void testJavascriptVariableSerialization() {
    deployProcess(NASHORN, "execution.setVariable('date', new java.util.Date(0));");

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    Date date = (Date) runtimeService.getVariable(pi.getId(), "date");
    assertEquals(0, date.getTime());

    deployProcess(NASHORN, "execution.setVariable('myVar', new org.camunda.bpm.engine.test.bpmn.scripttask.MySerializable('test'));");

    pi = runtimeService.startProcessInstanceByKey("testProcess");

    MySerializable myVar = (MySerializable) runtimeService.getVariable(pi.getId(), "myVar");
    assertEquals("test", myVar.getName());
  }

  @Test
  public void shouldLoadExternalScript() {
    // GIVEN
    // an external JS file with a function

    deployProcess(NASHORN,
        // WHEN
        // we load a function from an external file
        "load(\"" + getNormalizedResourcePath("/org/camunda/bpm/engine/test/bpmn/scripttask/sum.js") + "\");"
        // THEN
        // we can use that function
      + "execution.setVariable('foo', sum(3, 4));"
    );

    // WHEN
    // we start an instance of this process
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // THEN
    // the script task can be executed without exceptions
    // the execution variable is stored and has the correct value
    Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
    assertThat(variableValue).isInstanceOf(Number.class);
    assertThat(((Number) variableValue).intValue()).isEqualTo(7);
  }

}
