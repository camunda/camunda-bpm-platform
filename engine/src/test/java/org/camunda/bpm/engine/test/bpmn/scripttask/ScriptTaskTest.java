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
package org.camunda.bpm.engine.test.bpmn.scripttask;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 *
 * @author Daniel Meyer (Javascript)
 * @author Sebastian Menski (Python)
 * @author Nico Rehwaldt (Ruby)
 * @author Christian Lipphardt (Groovy)
 *
 */
public class ScriptTaskTest extends PluggableProcessEngineTestCase {

  private static final String JAVASCRIPT = "javascript";
  private static final String PYTHON = "python";
  private static final String RUBY = "ruby";
  private static final String GROOVY = "groovy";

  private List<String> deploymentIds = new ArrayList<String>();

  protected void tearDown() throws Exception {
    for (String deploymentId : deploymentIds) {
      repositoryService.deleteDeployment(deploymentId, true);
    }
  }

  public void testJavascriptProcessVarVisibility() {

    deployProcess(JAVASCRIPT,

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

  public void testPythonProcessVarAssignment() {

    deployProcess(PYTHON,

        // GIVEN
        // an execution variable 'foo'
        "execution.setVariable('foo', 'a')\n"

        // THEN
        // there should be a script variable defined
      + "if not foo:\n"
      + "    raise Exception('Variable foo should be defined as script variable.')\n"

        // GIVEN
        // a script variable with the same name
      + "foo = 'b'\n"

        // THEN
        // it should not change the value of the execution variable
      + "if execution.getVariable('foo') != 'a':\n"
      + "    raise Exception('Execution should contain variable foo')\n"

        // AND
        // it should override the visibility of the execution variable
      + "if foo != 'b':\n"
      + "    raise Exception('Script variable must override the visibiltity of the execution variable.')\n"

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

  public void testRubyProcessVarVisibility() {

    deployProcess(RUBY,

        // GIVEN
        // an execution variable 'foo'
        "$execution.setVariable('foo', 'a')\n"

        // THEN
        // there should NOT be a script variable defined (this is unsupported in Ruby binding)
      + "raise 'Variable foo should be defined as script variable.' if !$foo.nil?\n"

        // GIVEN
        // a script variable with the same name
      + "$foo = 'b'\n"

        // THEN
        // it should not change the value of the execution variable
      + "if $execution.getVariable('foo') != 'a'\n"
      + "  raise 'Execution should contain variable foo'\n"
      + "end\n"

        // AND
        // it should override the visibility of the execution variable
      + "if $foo != 'b'\n"
      + "  raise 'Script variable must override the visibiltity of the execution variable.'\n"
      + "end"

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

  public void testGroovyProcessVarVisibility() {

    deployProcess(GROOVY,

        // GIVEN
        // an execution variable 'foo'
        "execution.setVariable('foo', 'a')\n"

        // THEN
        // there should be a script variable defined
      + "if ( !foo ) {\n"
      + "  throw new Exception('Variable foo should be defined as script variable.')\n"
      + "}\n"

        // GIVEN
        // a script variable with the same name
      + "foo = 'b'\n"

        // THEN
        // it should not change the value of the execution variable
      + "if (execution.getVariable('foo') != 'a') {\n"
      + "  throw new Exception('Execution should contain variable foo')\n"
      + "}\n"

        // AND
        // it should override the visibility of the execution variable
      + "if (foo != 'b') {\n"
      + "  throw new Exception('Script variable must override the visibiltity of the execution variable.')\n"
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

  public void testJavascriptFunctionInvocation() {

    deployProcess(JAVASCRIPT,

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
    assertEquals(3.0, variable);

  }

  public void testPythonFunctionInvocation() {

    deployProcess(PYTHON,

        // GIVEN
        // a function named sum
        "def sum(a, b):\n"
      + "    return a + b\n"

        // THEN
        // i can call the function
      + "result = sum(1,2)\n"
      + "execution.setVariable('foo', result)"

    );

    // GIVEN
    // that we start an instance of this process
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // THEN
    // the variable is defined
    Object variable = runtimeService.getVariable(pi.getId(), "foo");
    assertEquals(3, variable);

  }

  public void testRubyFunctionInvocation() {

    deployProcess(RUBY,

        // GIVEN
        // a function named sum
        "def sum(a, b)\n"
      + "    return a + b\n"
      + "end\n"

        // THEN
        // i can call the function
      + "result = sum(1,2)\n"

      + "$execution.setVariable('foo', result)\n"

    );

    // GIVEN
    // that we start an instance of this process
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // THEN
    // the variable is defined
    Object variable = runtimeService.getVariable(pi.getId(), "foo");
    assertEquals(3l, variable);

  }

  public void testGroovyFunctionInvocation() {

    deployProcess(GROOVY,

        // GIVEN
        // a function named sum
        "def sum(a, b) {\n"
      + "    return a + b\n"
      + "}\n"

        // THEN
        // i can call the function
      + "result = sum(1,2)\n"

      + "execution.setVariable('foo', result)\n"

    );

    // GIVEN
    // that we start an instance of this process
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // THEN
    // the variable is defined
    Object variable = runtimeService.getVariable(pi.getId(), "foo");
    assertEquals(3, variable);

  }

  public void testJsVariable() {

    String scriptText = "var foo = 1;";

    deployProcess(JAVASCRIPT, scriptText);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");
    Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
    assertNull(variableValue);

  }

  public void testPythonVariable() {

    String scriptText = "foo = 1";

    deployProcess(PYTHON, scriptText);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");
    Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
    assertNull(variableValue);

  }

  public void testRubyVariable() {

    String scriptText = "foo = 1";

    deployProcess(RUBY, scriptText);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");
    Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
    assertNull(variableValue);

  }

  public void testGroovyVariable() {

    String scriptText = "def foo = 1";

    deployProcess(GROOVY, scriptText);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");
    Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
    assertNull(variableValue);

  }

  protected void deployProcess(String scriptFormat, String scriptText) {
    BpmnModelInstance process = createProcess(scriptFormat, scriptText);
    Deployment deployment = repositoryService.createDeployment()
      .addModelInstance("testProcess.bpmn", process)
      .deploy();
    deploymentIds.add(deployment.getId());
  }

  protected BpmnModelInstance createProcess(String scriptFormat, String scriptText) {

    return Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .scriptTask()
        .scriptFormat(scriptFormat)
        .scriptText(scriptText)
      .userTask()
      .endEvent()
    .done();

  }

  public void testAutoStoreScriptVarsOff() {
    assertFalse(processEngineConfiguration.isAutoStoreScriptVariables());
  }

}
