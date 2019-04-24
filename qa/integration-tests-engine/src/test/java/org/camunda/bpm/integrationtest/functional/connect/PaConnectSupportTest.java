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
package org.camunda.bpm.integrationtest.functional.connect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.connect.Connectors;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>Smoketest Make sure camunda connect can be used in a process application </p>
 *
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class PaConnectSupportTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createDeployment() {
    return initWebArchiveDeployment()
      .addAsResource("org/camunda/bpm/integrationtest/functional/connect/PaConnectSupportTest.connectorServiceTask.bpmn20.xml")
      .addClass(TestConnector.class)
      .addClass(TestConnectorRequest.class)
      .addClass(TestConnectorResponse.class)
      .addClass(TestConnectors.class);
  }

  @Test
  public void httpConnectorShouldBeAvailable() {
    assertNotNull(Connectors.http());
  }

  @Test
  public void soapConnectorShouldBeAvailable() {
    assertNotNull(Connectors.soap());
  }

  @Test
  public void connectorServiceTask() {
    TestConnector connector = new TestConnector();
    TestConnectors.registerConnector(connector);

    runtimeService.startProcessInstanceByKey("testProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    String payload = (String) taskService.getVariable(task.getId(), "payload");
    assertEquals("Hello world!", payload);

    TestConnectors.unregisterConnector(connector.getId());
  }

}
