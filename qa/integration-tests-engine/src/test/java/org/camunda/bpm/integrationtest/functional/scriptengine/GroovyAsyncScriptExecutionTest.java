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
package org.camunda.bpm.integrationtest.functional.scriptengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class GroovyAsyncScriptExecutionTest extends AbstractFoxPlatformIntegrationTest {

  protected static String process =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
      "<definitions id=\"definitions\" \r\n" +
      "  xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\r\n" +
      "  xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\"\r\n" +
      "  targetNamespace=\"Examples\">\r\n" +
      "  <process id=\"process\" isExecutable=\"true\" camunda:historyTimeToLive=\"P180D\">\r\n" +
      "    <startEvent id=\"theStart\" />\r\n" +
      "    <sequenceFlow id=\"flow1\" sourceRef=\"theStart\" targetRef=\"theScriptTask\" />\r\n" +
      "    <scriptTask id=\"theScriptTask\" name=\"Execute script\" scriptFormat=\"groovy\" camunda:asyncBefore=\"true\">\r\n" +
      "      <script>execution.setVariable(\"foo\", S(\"&lt;bar /&gt;\").name())</script>\r\n" +
      "    </scriptTask>\r\n" +
      "    <sequenceFlow id=\"flow2\" sourceRef=\"theScriptTask\" targetRef=\"theTask\" />\r\n" +
      "    <userTask id=\"theTask\" name=\"my task\" />\r\n" +
      "    <sequenceFlow id=\"flow3\" sourceRef=\"theTask\" targetRef=\"theEnd\" />\r\n" +
      "    <endEvent id=\"theEnd\" />\r\n" +
      "  </process>\r\n" +
      "</definitions>";

  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {
    WebArchive deployment = ShrinkWrap.create(WebArchive.class, "client.war")
            .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
            .addClass(AbstractFoxPlatformIntegrationTest.class)
            .addAsLibraries(DeploymentHelper.getEngineCdi());
    TestContainer.addContainerSpecificResourcesForNonPa(deployment);
    return deployment;
  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void shouldSetVariable() {
    String deploymentId = repositoryService.createDeployment()
        .addString("process.bpmn", process)
        .deploy()
        .getId();

    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    waitForJobExecutorToProcessAllJobs(30000);

    Object foo = runtimeService.getVariable(processInstanceId, "foo");
    assertNotNull(foo);
    assertEquals("bar", foo);

    repositoryService.deleteDeployment(deploymentId, true);
  }
}
