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
package org.camunda.bpm.engine.test.bpmn.el;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Test;

/**
 * @author Frederik Heremans
 */
public class ExpressionManagerTest extends PluggableProcessEngineTest {

  protected String deploymentId;

  @After
  public void clear() {
    Mocks.reset();

    if (deploymentId != null) {
      repositoryService.deleteDeployment(deploymentId, true);
      deploymentId = null;
    }
  }

  @Deployment
  @Test
  public void testMethodExpressions() {
    // Process contains 2 service tasks. one containing a method with no params, the other
    // contains a method with 2 params. When the process completes without exception,
    // test passed.
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("aString", "abcdefgh");
    runtimeService.startProcessInstanceByKey("methodExpressionProcess", vars);

    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("methodExpressionProcess").count());
  }

  @Deployment
  @Test
  public void testExecutionAvailable() {
    Map<String, Object> vars = new HashMap<String, Object>();

    vars.put("myVar", new ExecutionTestVariable());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testExecutionAvailableProcess", vars);

    // Check of the testMethod has been called with the current execution
    String value = (String) runtimeService.getVariable(processInstance.getId(), "testVar");
    assertNotNull(value);
    assertEquals("myValue", value);
  }

  @Deployment
  @Test
  public void testAuthenticatedUserIdAvailable() {
    try {
      // Setup authentication
      identityService.setAuthenticatedUserId("frederik");
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testAuthenticatedUserIdAvailableProcess");

      // Check if the variable that has been set in service-task is the authenticated user
      String value = (String) runtimeService.getVariable(processInstance.getId(), "theUser");
      assertNotNull(value);
      assertEquals("frederik", value);
    } finally {
      // Cleanup
      identityService.clearAuthentication();
    }
  }

  @Deployment
  @Test
  public void testResolvesVariablesFromDifferentScopes() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("assignee", "michael");

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("michael", task.getAssignee());

    variables.put("assignee", "johnny");
    ProcessInstance secondInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    task = taskService.createTaskQuery().processInstanceId(secondInstance.getId()).singleResult();
    assertEquals("johnny", task.getAssignee());
  }

  @Deployment
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Test
  public void testSetVariableByExpressionFromListener() {
    // given
    runtimeService.startProcessInstanceByKey("fieldInjectionTest", Variables.putValue("myCounter", 5));
    // when
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    // then
    assertEquals(1L, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myCounter", 6).count());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testJuelExpressionWithNonPublicClass() {
    final BpmnModelInstance process = Bpmn.createExecutableProcess("testProcess")
        .startEvent()
          .exclusiveGateway()
            .condition("true", "${list.contains('foo')}")
            .userTask("userTask")
          .moveToLastGateway()
            .condition("false", "${!list.contains('foo')}")
            .endEvent()
        .done();

    deploymentId = repositoryService.createDeployment()
        .addModelInstance("testProcess.bpmn", process)
        .deploy()
        .getId();

    runtimeService.startProcessInstanceByKey("testProcess",
        Variables.createVariables().putValue("list", Arrays.asList("foo", "bar")));

    HistoricActivityInstance userTask = historyService.createHistoricActivityInstanceQuery()
        .activityId("userTask")
        .singleResult();
    assertThat(userTask).isNotNull();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldCompareWithBigDecimal() {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("testProcess")
        .startEvent()
          .exclusiveGateway()
            .condition("true", "${total.compareTo(myValue) >= 0}")
            .userTask("userTask")
          .moveToLastGateway()
            .condition("false", "${total.compareTo(myValue) < 0}")
            .endEvent()
        .done();

    deploymentId = repositoryService.createDeployment()
        .addModelInstance("testProcess.bpmn", process)
        .deploy()
        .getId();

    // when
    runtimeService.startProcessInstanceByKey("testProcess",
        Variables.createVariables()
            .putValue("total", new BigDecimal(123))
            .putValue("myValue", new BigDecimal(0)));

    // then
    HistoricActivityInstance userTask = historyService.createHistoricActivityInstanceQuery()
        .activityId("userTask")
        .singleResult();
    assertThat(userTask).isNotNull();
  }

  @Deployment
  @Test
  public void shouldResolveMethodExpressionTwoParametersSameType() {
    // given process with two service tasks that resolve expression and store the result as variable
    Map<String, Object> vars = new HashMap<>();
    vars.put("myVar", new ExpressionTestParameter());

    // when the process is started
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", vars);

    // then no exceptions are thrown and two variables are saved
    boolean task1Var = (boolean) runtimeService.getVariable(processInstance.getId(), "task1Var");
    assertThat(task1Var).isTrue();
    String task2Var = (String) runtimeService.getVariable(processInstance.getId(), "task2Var");
    assertEquals("lastParam", task2Var);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldResolveMethodExpressionWithOneNullParameter() {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("testProcess")
        .startEvent()
          .exclusiveGateway()
            .condition("true", "${myBean.myMethod(execution.getVariable('v'), "
                + "execution.getVariable('w'), execution.getVariable('x'), "
                + "execution.getVariable('y'), execution.getVariable('z'))}")
            .userTask("userTask")
          .moveToLastGateway()
            .condition("false", "${false}")
            .endEvent()
        .done();

    deploymentId = repositoryService.createDeployment()
        .addModelInstance("testProcess.bpmn", process)
        .deploy()
        .getId();

    Mocks.register("myBean", new MyBean());

    // when
    runtimeService.startProcessInstanceByKey("testProcess",
        Variables.createVariables()
            .putValue("v", "a")
            .putValue("w", null)
            .putValue("x", "b")
            .putValue("y", "c")
            .putValue("z", "d"));

    // then
    HistoricActivityInstance userTask = historyService.createHistoricActivityInstanceQuery()
        .activityId("userTask")
        .singleResult();

    assertThat(userTask).isNotNull();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldResolveMethodExpressionWithTwoNullParameter() {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("testProcess")
        .startEvent()
          .exclusiveGateway()
            .condition("true", "${myBean.myMethod(execution.getVariable('v'), "
                + "execution.getVariable('w'), execution.getVariable('x'), "
                + "execution.getVariable('y'), execution.getVariable('z'))}")
            .userTask("userTask")
          .moveToLastGateway()
            .condition("false", "${false}")
            .endEvent()
        .done();

    deploymentId = repositoryService.createDeployment()
        .addModelInstance("testProcess.bpmn", process)
        .deploy()
        .getId();

    Mocks.register("myBean", new MyBean());

    // when
    runtimeService.startProcessInstanceByKey("testProcess",
        Variables.createVariables()
            .putValue("v", "a")
            .putValue("w", null)
            .putValue("x", "b")
            .putValue("y", null)
            .putValue("z", "d"));

    // then
    HistoricActivityInstance userTask = historyService.createHistoricActivityInstanceQuery()
        .activityId("userTask")
        .singleResult();

    assertThat(userTask).isNotNull();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldResolveMethodExpressionWithNoNullParameter() {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("testProcess")
        .startEvent()
          .exclusiveGateway()
            .condition("true", "${myBean.myMethod(execution.getVariable('v'), "
                + "execution.getVariable('w'), execution.getVariable('x'), "
                + "execution.getVariable('y'), execution.getVariable('z'))}")
            .userTask("userTask")
          .moveToLastGateway()
            .condition("false", "${false}")
            .endEvent()
        .done();

    deploymentId = repositoryService.createDeployment()
        .addModelInstance("testProcess.bpmn", process)
        .deploy()
        .getId();

    Mocks.register("myBean", new MyBean());

    // when
    runtimeService.startProcessInstanceByKey("testProcess",
        Variables.createVariables()
            .putValue("v", "a")
            .putValue("w", "b")
            .putValue("x", "c")
            .putValue("y", "d")
            .putValue("z", "e"));

    // then
    HistoricActivityInstance userTask = historyService.createHistoricActivityInstanceQuery()
        .activityId("userTask")
        .singleResult();

    assertThat(userTask).isNotNull();
  }

  /*
   * The following method expression tests are inspired by the OverloadedMethodTest from Eclipse Expressly:
   * https://github.com/eclipse-ee4j/expressly.
   */ 
  @Test
  public void shouldInvokeMethodWithStringArg() {
    // given
    String expression = "myBean.myStringMethod('foo')";
    String expectedOutput = "foo";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldInvokeMethodWithNullStringArg() {
    // given
    String expression = "myBean.myStringMethod(execution.getVariable('foo'))";
    String expectedOutput = "";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldInvokeMethodWithNullPrimitiveArg() {
    // given
    String expression = "myBean.myIntMethod(execution.getVariable('foo'))";
    int expectedOutput = 0;
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldInvokeMethodWithNullObjectArg() {
    // given
    String expression = "myBean.myObjectMethod(execution.getVariable('foo'))";
    Object expectedOutput = null;
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithNoArg() {
    // given
    String expression = "myBean.methodWithNoArg()";
    String expectedOutput = "methodWithNoArg";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldFailResolveMethodExpressionNonExistingMethod() {
    // given
    String expression = "myBean.methodNotExisted()";
    // when
    assertThatThrownBy(() -> assertMethodExpressionResult(expression, null))
    // then
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Unknown method");
  }

  @Test
  public void shouldResolveMethodExpressionWithOverloadedSingleArg_1() {
    // given
    String expression = "myBean.methodWithSingleArg(i1)";
    String expectedOutput = "I1";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithOverloadedSingleArg_2() {
    // given
    String expression = "myBean.methodWithSingleArg(i2)";
    String expectedOutput = "I2Impl";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithOverloadedSingleArg_3() {
    // given
    String expression = "myBean.methodWithSingleArg(i12)";
    String expectedOutput = "I1AndI2Impl";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithOverloadedDoubleArgs_1() {
    // given
    String expression = "myBean.methodWithDoubleArgs(i1, i2)";
    String expectedOutput = "I1Impl, I2";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithOverloadedDoubleArgs_2() {
    // given
    String expression = "myBean.methodWithDoubleArgs(i12, i2)";
    String expectedOutput = "I1, I2";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithOverloadedDoubleArgs_3() {
    // given
    String expression = "myBean.methodWithDoubleArgs(i12, i12)";
    String expectedOutput = "I1AndI2Impl, I1AndI2Impl";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithOverloadedDoubleArgs_4() {
    // given
    String expression = "myBean.methodWithDoubleArgs(i12s, i12)";
    String expectedOutput = "I1AndI2Impl, I1AndI2Impl";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithOverloadedDoubleArgs_5() {
    // given
    String expression = "myBean.methodWithDoubleArgs(i12s, i12s)";
    String expectedOutput = "I1AndI2Impl, I1AndI2Impl";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithAmbiguousArgs_1() {
    // given
    String expression = "myBean.methodWithAmbiguousArgs(i12, i2)";
    String expectedOutput = "I1AndI2Impl, I2";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithAmbiguousArgs_2() {
    // given
    String expression = "myBean.methodWithAmbiguousArgs(i1, i12)";
    String expectedOutput = "I1, I1AndI2Impl";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldFailResolveMethodExpressionWithAmbiguousArgs() {
    // given
    String expression = "myBean.methodWithAmbiguousArgs(i12, i12)";
    assertThatThrownBy(() -> assertMethodExpressionResult(expression, null))
    // then
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Unknown method");
  }

  @Test
  public void shouldResolveMethodExpressionWithCoercibleArgs_1() {
    // given
    String expression = "myBean.methodWithCoercibleArgs('foo', 'bar')";
    String expectedOutput = "String, String";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithCoercibleArgs_2() {
    // given
    String expression = "myBean.methodWithCoercibleArgs(i1, i12)";
    String expectedOutput = "String, String";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithCoercibleArgs_3() {
    // given
    String expression = "myBean.methodWithCoercibleArgs2(i1, 12345678)";
    String expectedOutput = "String, String";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithCoercibleArgs_4() {
    // given
    String expression = "myBean.methodWithCoercibleArgs2(i1, intVal)";
    String expectedOutput = "String, String";
    Mocks.register("intVal", 12345678);
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithCoercibleArgs_5() {
    // given
    String expression = "myBean.methodWithCoercibleArgs2(12345678, 12345678)";
    String expectedOutput = "Integer, Integer";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithCoercibleArgs_6() {
    // given
    String expression = "myBean.methodWithCoercibleArgs2(intVal, intVal)";
    String expectedOutput = "Integer, Integer";
    Mocks.register("intVal", 12345678);
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithVarArgs_1() {
    // given
    String expression = "myBean.methodWithVarArgs(i1)";
    String expectedOutput = "I1, I1...";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithVarArgs_2() {
    // given
    String expression = "myBean.methodWithVarArgs(i1, i1)";
    String expectedOutput = "I1, I1...";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithVarArgs_3() {
    // given
    String expression = "myBean.methodWithVarArgs(i12, i1, i12)";
    String expectedOutput = "I1, I1...";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithVarArgs_4() {
    // given
    String expression = "myBean.methodWithVarArgs2(i1)";
    String expectedOutput = "I1, I1AndI2Impl...";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithVarArgs_5() {
    // given
    String expression = "myBean.methodWithVarArgs2(i12)";
    String expectedOutput = "I1, I1AndI2Impl...";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithVarArgs_6() {
    // given
    String expression = "myBean.methodWithVarArgs2(i1, i1)";
    String expectedOutput = "I1, I1...";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  @Test
  public void shouldResolveMethodExpressionWithVarArgs_7() {
    // given
    String expression = "myBean.methodWithVarArgs2(i1, i12)";
    String expectedOutput = "I1, I1AndI2Impl...";
    // when & then
    assertMethodExpressionResult(expression, expectedOutput);
  }

  protected void assertMethodExpressionResult(String expression, Object result) {
    BpmnModelInstance process = Bpmn.createExecutableProcess("testProcess")
        .startEvent()
        .serviceTask()
          .camundaExpression("${" + expression + "}")
          .camundaResultVariable("output")
        .userTask()
        .endEvent()
        .done();

    deploymentId = repositoryService.createDeployment()
        .addModelInstance("testProcess.bpmn", process)
        .deploy()
        .getId();

    Mocks.register("myBean", new MyBean());
    Mocks.register("i1", new I1Impl());
    Mocks.register("i2", new I2Impl());
    Mocks.register("i12", new I1AndI2Impl());
    Mocks.register("i12s", new I1AndI2ImplSub());

    // when
    runtimeService.startProcessInstanceByKey("testProcess");

    // then
    VariableInstance output = runtimeService.createVariableInstanceQuery().variableName("output").singleResult();
    assertThat(output.getValue()).isEqualTo(result);
  }

  public static class MyBean {

    public String methodWithNoArg() {
      return "methodWithNoArg";
    }

    public String methodWithSingleArg(I1 i1) {
      return "I1";
    }

    public String methodWithSingleArg(I2 i2) {
      return "I2";
    }

    public String methodWithSingleArg(I2Impl i2) {
      return "I2Impl";
    }

    public String methodWithSingleArg(I1AndI2Impl i1) {
      return "I1AndI2Impl";
    }

    public String methodWithDoubleArgs(I1 i1, I2 i2) {
      return "I1, I2";
    }

    public String methodWithDoubleArgs(I1Impl i1, I2 i2) {
      return "I1Impl, I2";
    }

    public String methodWithDoubleArgs(I1AndI2Impl i1, I1AndI2Impl i2) {
      return "I1AndI2Impl, I1AndI2Impl";
    }

    public String methodWithAmbiguousArgs(I1AndI2Impl i1, I2 i2) {
      return "I1AndI2Impl, I2";
    }

    public String methodWithAmbiguousArgs(I1 i1, I1AndI2Impl i2) {
      return "I1, I1AndI2Impl";
    }

    public String methodWithCoercibleArgs(String s1, String s2) {
      return "String, String";
    }

    public String methodWithCoercibleArgs2(String s1, String s2) {
      return "String, String";
    }

    public String methodWithCoercibleArgs2(Integer s1, Integer s2) {
      return "Integer, Integer";
    }

    public String methodWithVarArgs(I1 i1, I1... i2) {
      return "I1, I1...";
    }

    public String methodWithVarArgs2(I1 i1, I1... i2) {
      return "I1, I1...";
    }

    public String methodWithVarArgs2(I1 i1, I1AndI2Impl... i2) {
      return "I1, I1AndI2Impl...";
    }

    public boolean myMethod(String v, String w, String x, String y, String z) {
      return true;
    }

    public String myStringMethod(String v) {
      return v;
    }

    public int myIntMethod(int v) {
      return v;
    }

    public I1 myObjectMethod(I1 v) {
      return v;
    }
  }

  public static interface I1 {}
  public static interface I2 {}
  public static class I1Impl implements I1 {}
  public static class I2Impl implements I2 {}
  public static class I1AndI2Impl implements I1, I2 {}
  public static class I1AndI2ImplSub extends I1AndI2Impl {}

}