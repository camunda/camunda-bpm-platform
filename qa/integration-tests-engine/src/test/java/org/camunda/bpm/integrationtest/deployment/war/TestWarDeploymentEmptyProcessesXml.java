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
package org.camunda.bpm.integrationtest.deployment.war;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;


/**
 * Assert that we can deploy a WAR which bundles
 * the client and an empty processes.xml file
 *
 */
@RunWith(Arquillian.class)
public class TestWarDeploymentEmptyProcessesXml extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment("test.war", "META-INF/empty_processes.xml")
        .addAsResource("org/camunda/bpm/integrationtest/testDeployProcessArchive.bpmn20.xml");
  }

  @Test
  public void testDeployProcessArchive() {
    Assert.assertNotNull(processEngine);
    RepositoryService repositoryService = processEngine.getRepositoryService();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("testDeployProcessArchive")
      .list();

    Assert.assertEquals(1, processDefinitions.size());
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery()
      .deploymentId(processDefinitions.get(0).getDeploymentId())
      .singleResult();

    Set<String> registeredProcessApplications = BpmPlatform.getProcessApplicationService().getProcessApplicationNames();

    boolean containsProcessApplication = false;

    // the process application name is used as name for the db deployment
    for (String appName : registeredProcessApplications) {
      if (appName.equals(deployment.getName())) {
        containsProcessApplication = true;
      }
    }
    assertTrue(containsProcessApplication);


    // manually delete process definition here (to clean up)
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

}