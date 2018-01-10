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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ScriptCompilationException;
import org.camunda.bpm.engine.ScriptEvaluationException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
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
  private static final String JUEL = "juel";

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

    if (variable instanceof Double) {
      // jdk 6/7 - rhino returns Double 3.0 for 1+2
      assertEquals(3.0, variable);
    } else if (variable instanceof Integer) {
      // jdk8 - nashorn returns Integer 3 for 1+2
      assertEquals(3, variable);
    }

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

  public void testJuelExpression() {
    deployProcess(JUEL, "${execution.setVariable('foo', 'bar')}");

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    String variableValue = (String) runtimeService.getVariable(pi.getId(), "foo");
    assertEquals("bar", variableValue);
  }

  public void testJuelCapitalizedExpression() {
    deployProcess(JUEL.toUpperCase(), "${execution.setVariable('foo', 'bar')}");

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    String variableValue = (String) runtimeService.getVariable(pi.getId(), "foo");
    assertEquals("bar", variableValue);
  }

  public void testSourceAsExpressionAsVariable() {
    deployProcess(PYTHON, "${scriptSource}");

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptSource", "execution.setVariable('foo', 'bar')");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);

    String variableValue = (String) runtimeService.getVariable(pi.getId(), "foo");
    assertEquals("bar", variableValue);
  }

  public void testSourceAsExpressionAsNonExistingVariable() {
    deployProcess(PYTHON, "${scriptSource}");

    try {
      runtimeService.startProcessInstanceByKey("testProcess");
      fail("Process variable 'scriptSource' not defined");
    }
    catch (ProcessEngineException e) {
      assertTextPresentIgnoreCase("Cannot resolve identifier 'scriptSource'", e.getMessage());
    }
  }

  public void testSourceAsExpressionAsBean() {
    deployProcess(PYTHON, "#{scriptResourceBean.getSource()}");

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptResourceBean", new ScriptResourceBean());
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);

    String variableValue = (String) runtimeService.getVariable(pi.getId(), "foo");
    assertEquals("bar", variableValue);
  }

  public void testSourceAsExpressionWithWhitespace() {
    deployProcess(PYTHON, "\t\n  \t \n  ${scriptSource}");

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptSource", "execution.setVariable('foo', 'bar')");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);

    String variableValue = (String) runtimeService.getVariable(pi.getId(), "foo");
    assertEquals("bar", variableValue);
  }

  public void testJavascriptVariableSerialization() {
    deployProcess(JAVASCRIPT, "execution.setVariable('date', new java.util.Date(0));");

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    Date date = (Date) runtimeService.getVariable(pi.getId(), "date");
    assertEquals(0, date.getTime());

    deployProcess(JAVASCRIPT, "execution.setVariable('myVar', new org.camunda.bpm.engine.test.bpmn.scripttask.MySerializable('test'));");

    pi = runtimeService.startProcessInstanceByKey("testProcess");

    MySerializable myVar = (MySerializable) runtimeService.getVariable(pi.getId(), "myVar");
    assertEquals("test", myVar.getName());
  }

  public void testPythonVariableSerialization() {
    deployProcess(PYTHON, "import java.util.Date\nexecution.setVariable('date', java.util.Date(0))");

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    Date date = (Date) runtimeService.getVariable(pi.getId(), "date");
    assertEquals(0, date.getTime());

    deployProcess(PYTHON, "import org.camunda.bpm.engine.test.bpmn.scripttask.MySerializable\n" +
      "execution.setVariable('myVar', org.camunda.bpm.engine.test.bpmn.scripttask.MySerializable('test'));");

    pi = runtimeService.startProcessInstanceByKey("testProcess");

    MySerializable myVar = (MySerializable) runtimeService.getVariable(pi.getId(), "myVar");
    assertEquals("test", myVar.getName());
  }

  public void testRubyVariableSerialization() {
    deployProcess(RUBY, "require 'java'\n$execution.setVariable('date', java.util.Date.new(0))");

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    Date date = (Date) runtimeService.getVariable(pi.getId(), "date");
    assertEquals(0, date.getTime());

    deployProcess(RUBY, "$execution.setVariable('myVar', org.camunda.bpm.engine.test.bpmn.scripttask.MySerializable.new('test'));");

    pi = runtimeService.startProcessInstanceByKey("testProcess");

    MySerializable myVar = (MySerializable) runtimeService.getVariable(pi.getId(), "myVar");
    assertEquals("test", myVar.getName());
  }

  public void testGroovyVariableSerialization() {
    deployProcess(GROOVY, "execution.setVariable('date', new java.util.Date(0))");

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    Date date = (Date) runtimeService.getVariable(pi.getId(), "date");
    assertEquals(0, date.getTime());

    deployProcess(GROOVY, "execution.setVariable('myVar', new org.camunda.bpm.engine.test.bpmn.scripttask.MySerializable('test'));");

    pi = runtimeService.startProcessInstanceByKey("testProcess");

    MySerializable myVar = (MySerializable) runtimeService.getVariable(pi.getId(), "myVar");
    assertEquals("test", myVar.getName());
  }

  public void testGroovyNotExistingImport() {
    deployProcess(GROOVY, "import unknown");

    try {
      runtimeService.startProcessInstanceByKey("testProcess");
      fail("Should fail during script compilation");
    }
    catch (ScriptCompilationException e) {
      assertTextPresentIgnoreCase("import unknown", e.getMessage());
    }
  }

  public void testGroovyNotExistingImportWithoutCompilation() {
    // disable script compilation
    processEngineConfiguration.setEnableScriptCompilation(false);

    deployProcess(GROOVY, "import unknown");

    try {
      runtimeService.startProcessInstanceByKey("testProcess");
      fail("Should fail during script evaluation");
    }
    catch (ScriptEvaluationException e) {
      assertTextPresentIgnoreCase("import unknown", e.getMessage());
    }
    finally {
      // re-enable script compilation
      processEngineConfiguration.setEnableScriptCompilation(true);
    }
  }

  public void testShouldNotDeployProcessWithMissingScriptElementAndResource() {
    try {
      deployProcess(Bpmn.createExecutableProcess("testProcess")
        .startEvent()
        .scriptTask()
          .scriptFormat(RUBY)
        .userTask()
        .endEvent()
      .done());

      fail("this process should not be deployable");
    } catch (ProcessEngineException e) {
      // happy path
    }
  }

  public void testShouldUseJuelAsDefaultScriptLanguage() {
    deployProcess(Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .scriptTask()
        .scriptText("${true}")
      .userTask()
      .endEvent()
    .done());

    runtimeService.startProcessInstanceByKey("testProcess");

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
  }

  protected void deployProcess(BpmnModelInstance process) {
    Deployment deployment = repositoryService.createDeployment()
        .addModelInstance("testProcess.bpmn", process)
        .deploy();
      deploymentIds.add(deployment.getId());
  }

  protected void deployProcess(String scriptFormat, String scriptText) {
    BpmnModelInstance process = createProcess(scriptFormat, scriptText);
    deployProcess(process);
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

  @org.camunda.bpm.engine.test.Deployment
  public void testPreviousTaskShouldNotHandleException(){
    try {
      runtimeService.startProcessInstanceByKey("process");
      fail();
    }
    // since the NVE extends the ProcessEngineException we have to handle it
    // separately
    catch (NullValueException nve) {
      fail("Shouldn't have received NullValueException");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Invalid format"));
    }
  }

  @org.camunda.bpm.engine.test.Deployment
  public void testSetScriptResultToProcessVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("echo", "hello");
    variables.put("existingProcessVariableName", "one");

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setScriptResultToProcessVariable", variables);

    assertEquals("hello", runtimeService.getVariable(pi.getId(), "existingProcessVariableName"));
    assertEquals(pi.getId(), runtimeService.getVariable(pi.getId(), "newProcessVariableName"));
  }

  @org.camunda.bpm.engine.test.Deployment
  public void testGroovyScriptExecution() {
    try {

      processEngineConfiguration.setAutoStoreScriptVariables(true);
      int[] inputArray = new int[] {1, 2, 3, 4, 5};
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("scriptExecution", CollectionUtil.singletonMap("inputArray", inputArray));

      Integer result = (Integer) runtimeService.getVariable(pi.getId(), "sum");
      assertEquals(15, result.intValue());

    } finally {
      processEngineConfiguration.setAutoStoreScriptVariables(false);
    }
  }

  @org.camunda.bpm.engine.test.Deployment
  public void testGroovySetVariableThroughExecutionInScript() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setScriptVariableThroughExecution");

    // Since 'def' is used, the 'scriptVar' will be script local
    // and not automatically stored as a process variable.
    assertNull(runtimeService.getVariable(pi.getId(), "scriptVar"));
    assertEquals("test123", runtimeService.getVariable(pi.getId(), "myVar"));
  }

  @org.camunda.bpm.engine.test.Deployment
  public void testScriptEvaluationException() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("Process_1").singleResult();
    try {
      runtimeService.startProcessInstanceByKey("Process_1");
    } catch (ScriptEvaluationException e) {
      assertTextPresent("Unable to evaluate script while executing activity 'Failing' in the process definition with id '" + processDefinition.getId() + "'", e.getMessage());
    }
  }
}
