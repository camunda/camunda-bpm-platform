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
package org.camunda.bpm.integrationtest.functional.classloading.jobexecution;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * See CAM-10258
 */
@RunWith(Arquillian.class)
public class ClassloadingDuringJobExecutionTest extends AbstractFoxPlatformIntegrationTest {
  protected static String process =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
      "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" targetNamespace=\"Examples\">\r\n" +
      "  <process id=\"Process_1\" name=\"ServiceTask_Throw_BMPN_Error\" isExecutable=\"true\">\r\n" +
      "    <startEvent id=\"StartEvent_1\">\r\n" +
      "    </startEvent>\r\n" +
      "    <sequenceFlow id=\"SequenceFlow_03wj6bv\" sourceRef=\"StartEvent_1\" targetRef=\"Task_1bkcm2v\" />\r\n" +
      "    <endEvent id=\"EndEvent_0joyvpc\">\r\n" +
      "    </endEvent>\r\n" +
      "    <sequenceFlow id=\"SequenceFlow_0mt1p11\" sourceRef=\"Task_1bkcm2v\" targetRef=\"EndEvent_0joyvpc\" />\r\n" +
      "    <serviceTask id=\"Task_1bkcm2v\" name=\"Throw BPMN Error\" camunda:asyncBefore=\"true\" camunda:expression=\"${true}\">\r\n" +
      "      <extensionElements>\r\n" +
      "        <camunda:inputOutput>\r\n" +
      "          <camunda:outputParameter name=\"output\">\r\n" +
      "            <camunda:script scriptFormat=\"Javascript\">throw new org.camunda.bpm.engine.delegate.BpmnError(\"Test error thrown\");</camunda:script>\r\n" +
      "          </camunda:outputParameter>\r\n" +
      "        </camunda:inputOutput>\r\n" +
      "      </extensionElements>\r\n" +
      "    </serviceTask>\r\n" +
      "  </process>\r\n" +
      "</definitions>\r\n";

  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {
    WebArchive deployment = ShrinkWrap.create(WebArchive.class, "client.war")
            .addClass(AbstractFoxPlatformIntegrationTest.class);
    TestContainer.addContainerSpecificResourcesForNonPa(deployment);
    return deployment;
  }

  @Test
  public void shouldLoadBPMNErorClassUsedInGroovyScriptDuringJobExecution() {
    // given
    String deploymentId = repositoryService.createDeployment()
        .addString("process.bpmn", process)
        .deploy().getId();
    runtimeService.startProcessInstanceByKey("Process_1");

    // when
    waitForJobExecutorToProcessAllJobs();

    // then
    List<Job> failedJobs = managementService.createJobQuery().noRetriesLeft().list();
    assertTrue(failedJobs.size() > 0);
    for (Job job : failedJobs) {
      String jobExceptionStacktrace = managementService.getJobExceptionStacktrace(job.getId());
      assertFalse(jobExceptionStacktrace.contains("ClassNotFoundException"));
      assertTrue(jobExceptionStacktrace.contains("Test error thrown"));
    }
    // clean up
    repositoryService.deleteDeployment(deploymentId, true);
  }
}
