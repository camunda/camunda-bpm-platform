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
package org.camunda.bpm.integrationtest.deployment.cfg;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>This test ensures that we can use a custom location for the <em>processes.xml</em> file.</p>
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class TestCustomProcessesXmlFileLocation extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {    
    
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
        .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
        .addAsLibraries(DeploymentHelper.getEngineCdi())
        .addAsResource("org/camunda/bpm/integrationtest/deployment/cfg/processes.xml", "my/alternate/location/processes.xml")
        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(CustomProcessApplication.class)
        .addAsResource("org/camunda/bpm/integrationtest/deployment/cfg/invoice-it.bpmn20.xml");
    
    return archive;
    
  }
  
  @Test
  public void testDeployProcessArchive() {
    Assert.assertNotNull(processEngine);
    RepositoryService repositoryService = processEngine.getRepositoryService();
    long count = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("invoice-it")
      .count();
    
    Assert.assertEquals(1, count);
  }
  
  

}
