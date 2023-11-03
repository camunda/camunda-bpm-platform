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
package org.camunda.connect.plugin;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.BpmnParseException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.connect.ConnectorException;
import org.camunda.connect.Connectors;
import org.camunda.connect.httpclient.HttpConnector;
import org.camunda.connect.httpclient.soap.SoapHttpConnector;
import org.camunda.connect.plugin.util.TestConnector;
import org.camunda.connect.spi.Connector;

public class ConnectProcessEnginePluginTest extends PluggableProcessEngineTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    TestConnector.responseParameters.clear();
    TestConnector.requestParameters = null;
  }

  public void testConnectorsRegistered() {
    Connector<?> http = Connectors.getConnector(HttpConnector.ID);
    assertNotNull(http);
    Connector<?> soap = Connectors.getConnector(SoapHttpConnector.ID);
    assertNotNull(soap);
    Connector<?> test = Connectors.getConnector(TestConnector.ID);
    assertNotNull(test);
  }

  public void testConnectorIdMissing() {
    try {
      repositoryService.createDeployment().addClasspathResource("org/camunda/connect/plugin/ConnectProcessEnginePluginTest.testConnectorIdMissing.bpmn")
        .deploy();
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      assertFalse(e instanceof BpmnParseException);
    }
  }

  @Deployment
  public void testConnectorIdUnknown() {
    try {
      runtimeService.startProcessInstanceByKey("testProcess");
      fail("Exception expected");
    }
    catch (ConnectorException e) {
      // expected
    }
  }

  @Deployment
  public void testConnectorInvoked() {
    String outputParamValue = "someOutputValue";
    String inputVariableValue = "someInputVariableValue";

    TestConnector.responseParameters.put("someOutputParameter", outputParamValue);

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("someInputVariable", inputVariableValue);
    runtimeService.startProcessInstanceByKey("testProcess", vars);

    // validate input parameter
    assertNotNull(TestConnector.requestParameters.get("reqParam1"));
    assertEquals(inputVariableValue, TestConnector.requestParameters.get("reqParam1"));

    // validate connector output
    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("out1").singleResult();
    assertNotNull(variable);
    assertEquals(outputParamValue, variable.getValue());
  }

  @Deployment
  public void testConnectorWithScriptInputOutputMapping() {
    int x = 3;
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("x", x);
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    // validate input parameter
    Object in = TestConnector.requestParameters.get("in");
    assertNotNull(in);
    assertEquals(2 * x, in);

    // validate output parameter
    VariableInstance out = runtimeService.createVariableInstanceQuery().variableName("out").singleResult();
    assertNotNull(out);
    assertEquals(3 * x, out.getValue());
  }


  @Deployment
  public void testConnectorWithSetVariableInOutputMapping() {
    // given process with set variable on connector in output mapping

    // when start process
    runtimeService.startProcessInstanceByKey("testProcess");

    // then variable x is set and no exception is thrown
    VariableInstance out = runtimeService.createVariableInstanceQuery().variableName("x").singleResult();
    assertEquals(1, out.getValue());
  }

  @Deployment(resources="org/camunda/connect/plugin/ConnectProcessEnginePluginTest.testConnectorWithThrownExceptionInScriptInputOutputMapping.bpmn")
  public void testConnectorBpmnErrorThrownInScriptInputMappingIsHandledByBoundaryEvent() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "in");
    variables.put("exception", new BpmnError("error"));
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    //we will only reach the user task if the BPMNError from the script was handled by the boundary event
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName(), is("User Task"));
  }

  @Deployment(resources="org/camunda/connect/plugin/ConnectProcessEnginePluginTest.testConnectorWithThrownExceptionInScriptInputOutputMapping.bpmn")
  public void testConnectorRuntimeExceptionThrownInScriptInputMappingIsNotHandledByBoundaryEvent() {
    String exceptionMessage = "myException";
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "in");
    variables.put("exception", new RuntimeException(exceptionMessage));
    try {
      runtimeService.startProcessInstanceByKey("testProcess", variables);
    } catch(RuntimeException re){
      assertThat(re.getMessage(), containsString(exceptionMessage));
    }
  }

  @Deployment(resources="org/camunda/connect/plugin/ConnectProcessEnginePluginTest.testConnectorWithThrownExceptionInScriptInputOutputMapping.bpmn")
  public void testConnectorBpmnErrorThrownInScriptOutputMappingIsHandledByBoundaryEvent() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "out");
    variables.put("exception", new BpmnError("error"));
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    //we will only reach the user task if the BPMNError from the script was handled by the boundary event
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName(), is("User Task"));
  }

  @Deployment(resources="org/camunda/connect/plugin/ConnectProcessEnginePluginTest.testConnectorWithThrownExceptionInScriptInputOutputMapping.bpmn")
  public void testConnectorRuntimeExceptionThrownInScriptOutputMappingIsNotHandledByBoundaryEvent() {
    String exceptionMessage = "myException";
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "out");
    variables.put("exception", new RuntimeException(exceptionMessage));
    try {
      runtimeService.startProcessInstanceByKey("testProcess", variables);
    } catch(RuntimeException re){
      assertThat(re.getMessage(), containsString(exceptionMessage));
    }
  }

  @Deployment(resources="org/camunda/connect/plugin/ConnectProcessEnginePluginTest.testConnectorWithThrownExceptionInScriptResourceInputOutputMapping.bpmn")
  public void testConnectorBpmnErrorThrownInScriptResourceInputMappingIsHandledByBoundaryEvent() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "in");
    variables.put("exception", new BpmnError("error"));
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    //we will only reach the user task if the BPMNError from the script was handled by the boundary event
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName(), is("User Task"));
  }

  @Deployment(resources="org/camunda/connect/plugin/ConnectProcessEnginePluginTest.testConnectorWithThrownExceptionInScriptResourceInputOutputMapping.bpmn")
  public void testConnectorRuntimeExceptionThrownInScriptResourceInputMappingIsNotHandledByBoundaryEvent() {
    String exceptionMessage = "myException";
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "in");
    variables.put("exception", new RuntimeException(exceptionMessage));
    try {
      runtimeService.startProcessInstanceByKey("testProcess", variables);
    } catch(RuntimeException re){
      assertThat(re.getMessage(), containsString(exceptionMessage));
    }
  }

  @Deployment(resources="org/camunda/connect/plugin/ConnectProcessEnginePluginTest.testConnectorWithThrownExceptionInScriptResourceInputOutputMapping.bpmn")
  public void testConnectorBpmnErrorThrownInScriptResourceOutputMappingIsHandledByBoundaryEvent() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "out");
    variables.put("exception", new BpmnError("error"));
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    //we will only reach the user task if the BPMNError from the script was handled by the boundary event
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName(), is("User Task"));
  }

  @Deployment(resources="org/camunda/connect/plugin/ConnectProcessEnginePluginTest.testConnectorWithThrownExceptionInScriptResourceInputOutputMapping.bpmn")
  public void testConnectorRuntimeExceptionThrownInScriptResourceOutputMappingIsNotHandledByBoundaryEvent() {
    String exceptionMessage = "myException";
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "out");
    variables.put("exception", new RuntimeException(exceptionMessage));
    try {
      runtimeService.startProcessInstanceByKey("testProcess", variables);
    } catch(RuntimeException re){
      assertThat(re.getMessage(), containsString(exceptionMessage));
    }
  }

  @Deployment(resources="org/camunda/connect/plugin/ConnectProcessEnginePluginTest.testConnectorBpmnErrorThrownInScriptResourceNoAsyncAfterJobIsCreated.bpmn")
  public void testConnectorBpmnErrorThrownInScriptResourceNoAsyncAfterJobIsCreated() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "in");
    variables.put("exception", new BpmnError("error"));

    // when
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    // then
    // we will only reach the user task if the BPMNError from the script was handled by the boundary event
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName(), is("User Task"));

    // no job is created
    assertThat(managementService.createJobQuery().count(), is(0l));
  }

  @Deployment
  public void testFollowingExceptionIsNotHandledByConnector(){
    try {
      runtimeService.startProcessInstanceByKey("testProcess");
    } catch(RuntimeException re){
      assertThat(re.getMessage(), containsString("Invalid format"));
    }
  }

  @Deployment
  public void testSendTaskWithConnector() {
    String outputParamValue = "someSendTaskOutputValue";
    String inputVariableValue = "someSendTaskInputVariableValue";

    TestConnector.responseParameters.put("someOutputParameter", outputParamValue);

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("someInputVariable", inputVariableValue);
    ProcessInstance processInstance = runtimeService
        .startProcessInstanceByKey("process_sending_with_connector", vars);

    // validate input parameter
    assertNotNull(TestConnector.requestParameters.get("reqParam1"));
    assertEquals(inputVariableValue, TestConnector.requestParameters.get("reqParam1"));

    // validate connector output
    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("out1").singleResult();
    assertNotNull(variable);
    assertEquals(outputParamValue, variable.getValue());
  }

  @Deployment
  public void testIntermediateMessageThrowEventWithConnector() {
    String outputParamValue = "someMessageThrowOutputValue";
    String inputVariableValue = "someMessageThrowInputVariableValue";

    TestConnector.responseParameters.put("someOutputParameter", outputParamValue);

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("someInputVariable", inputVariableValue);
    ProcessInstance processInstance = runtimeService
        .startProcessInstanceByKey("process_sending_with_connector", vars);

    // validate input parameter
    assertNotNull(TestConnector.requestParameters.get("reqParam1"));
    assertEquals(inputVariableValue, TestConnector.requestParameters.get("reqParam1"));

    // validate connector output
    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("out1").singleResult();
    assertNotNull(variable);
    assertEquals(outputParamValue, variable.getValue());
  }

  @Deployment
  public void testMessageEndEventWithConnector() {
    String outputParamValue = "someMessageEndOutputValue";
    String inputVariableValue = "someMessageEndInputVariableValue";

    TestConnector.responseParameters.put("someOutputParameter", outputParamValue);

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("someInputVariable", inputVariableValue);
    ProcessInstance processInstance = runtimeService
        .startProcessInstanceByKey("process_sending_with_connector", vars);
    assertProcessEnded(processInstance.getId());

    // validate input parameter
    assertNotNull(TestConnector.requestParameters.get("reqParam1"));
    assertEquals(inputVariableValue, TestConnector.requestParameters.get("reqParam1"));

    // validate connector output
    HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().variableName("out1").singleResult();
    assertNotNull(variable);
    assertEquals(outputParamValue, variable.getValue());
  }

}
