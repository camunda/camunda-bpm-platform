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

import org.junit.Assert;

import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.camunda.bpm.integrationtest.util.TestHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * <p>This test verifies that a WAR deployment can process multiple subdeployments that define process archives</p>
 * 
 * <pre>
 *   |-- My-Application.war
 *       |-- WEB-INF
 *           |-- classes
 *               |-- MEATA-INF/processes.xml               (1)
 *                   |-- process0.bpmn    
 *                   |-- directory/process1.bpmn
 *                   |-- alternateDirectory/process2.bpmn 
 *           |-- lib/
 *               |-- pa2.jar 
 *                   |-- process3.bpmn    
 *                   |-- directory/process4.bpmn
 *                   |-- alternateDirectory/process5.bpmn
 *                    
 *               |-- pa3.jar
 *                   |-- process6.bpmn    
 *                   |-- directory/process7.bpmn
 *                   |-- alternateDirectory/process8.bpmn
 * </pre> 
 * 
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class TestWarDeploymentWithMultiplePasAsSubdeployment5 extends AbstractFoxPlatformIntegrationTest {
  
  public final static String PROCESSES_XML = 
    "<process-application xmlns=\"http://www.camunda.org/schema/1.0/ProcessApplication\">" +
          
      "<process-archive name=\"PA_NAME\">" +
        "<properties>" +        
          "<property name=\"isDeleteUponUndeploy\">true</property>" +
        "</properties>" +  
      "</process-archive>" +
  
    "</process-application>";  
  
  @Deployment
  public static WebArchive processArchive() {    
    
    Asset pa1ProcessesXml = TestHelper.getStringAsAssetWithReplacements(
            PROCESSES_XML, 
            new String[][]{new String[]{"PA_NAME","PA1"}});

    Asset[] processAssets = TestHelper.generateProcessAssets(9);
        
    JavaArchive pa2 = ShrinkWrap.create(JavaArchive.class, "pa2.jar")
            .addAsResource(processAssets[3], "process3.bpmn")
            .addAsResource(processAssets[4], "directory/process4.bpmn")
            .addAsResource(processAssets[5], "alternateDirectory/process5.bpmn");
    
    JavaArchive pa3 = ShrinkWrap.create(JavaArchive.class, "pa3.jar")
            .addAsResource(processAssets[6], "process6.bpmn")
            .addAsResource(processAssets[7], "directory/process7.bpmn")
            .addAsResource(processAssets[8], "alternateDirectory/process8.bpmn");
    
    WebArchive deployment = ShrinkWrap.create(WebArchive.class, "test.war")
            .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
            .addAsLibraries(DeploymentHelper.getEngineCdi())
            
            .addAsLibraries(pa2)
            .addAsLibraries(pa3)
            
            .addAsResource(pa1ProcessesXml, "META-INF/processes.xml")
            .addAsResource(processAssets[0], "process0.bpmn")
            .addAsResource(processAssets[1], "directory/process1.bpmn")
            .addAsResource(processAssets[2], "alternateDirectory/process2.bpmn")
            
            .addClass(AbstractFoxPlatformIntegrationTest.class);    
    
    TestContainer.addContainerSpecificResources(deployment);
    
    return deployment;
  }
  
  @Test
  public void testDeployProcessArchive() {
    
    assertProcessDeployed("process-0", "PA1");
    assertProcessDeployed("process-1", "PA1");
    assertProcessDeployed("process-2", "PA1");
    assertProcessNotDeployed("process-3");
    assertProcessNotDeployed("process-4");
    assertProcessNotDeployed("process-5");
    assertProcessNotDeployed("process-6");
    assertProcessNotDeployed("process-7");
    assertProcessNotDeployed("process-8");
    
  }
  
  protected void assertProcessDeployed(String processKey, String expectedDeploymentName) {
    
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .latestVersion()
        .processDefinitionKey(processKey)
        .singleResult();    
    
    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentId(processDefinition.getDeploymentId());
    
    Assert.assertEquals(expectedDeploymentName, deploymentQuery.singleResult().getName());
    
  }
  
  protected void assertProcessNotDeployed(String processKey) {
    
    long count = repositoryService
        .createProcessDefinitionQuery()
        .latestVersion()
        .processDefinitionKey(processKey)
        .count();
    
    Assert.assertEquals("Process with key "+processKey+ " should not be deployed", 0, count);
  }

}
