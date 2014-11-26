/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.BpmnParseException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.VariableInstance;
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
    Connector http = Connectors.getConnector(HttpConnector.ID);
    assertNotNull(http);
    Connector soap = Connectors.getConnector(SoapHttpConnector.ID);
    assertNotNull(soap);
    Connector test = Connectors.getConnector(TestConnector.ID);
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

}

