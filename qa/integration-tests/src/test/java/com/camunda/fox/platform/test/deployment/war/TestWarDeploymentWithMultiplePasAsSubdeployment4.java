/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.camunda.fox.platform.test.deployment.war;

import junit.framework.Assert;

import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;
import com.camunda.fox.platform.test.util.DeploymentHelper;
import com.camunda.fox.platform.test.util.TestHelper;

/**
 * 
 * <pre>
 *   |-- My-Application.war
 *       |-- WEB-INF
 *           |-- classes
 *               |-- META-INF/processes.xml 
 *                      defines pa1 using classpath:directory/
 *                      defines pa2 using classpath:alternateDirectory/
 *               |-- process0.bpmn
 *                   
 *           |-- lib/
 *               |-- pa2.jar 
 *                   |-- process0.bpmn    
 *                   |-- directory/process1.bpmn
 *                       
 *               |-- pa3.jar 
 *                   |-- process0.bpmn 
 *                   |-- alternateDirectory/process2.bpmn                   
 * </pre> 
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class TestWarDeploymentWithMultiplePasAsSubdeployment4 extends AbstractFoxPlatformIntegrationTest {
  
  public final static String PROCESSES_XML = 
    "<process-application xmlns=\"http://www.camunda.org/schema/1.0/ProcessApplication\">" +
          
      "<process-archive name=\"pa1\">" +
        "<properties>" +        
          "<property name=\"isDeleteUponUndeploy\">true</property>" +
          "<property name=\"resourceRootPath\">classpath:directory/</property>" +
        "</properties>" +  
      "</process-archive>" +
        
      "<process-archive name=\"pa2\">" +
      "<properties>" +        
        "<property name=\"isDeleteUponUndeploy\">true</property>" +
        "<property name=\"resourceRootPath\">classpath:alternateDirectory/</property>" +
      "</properties>" +  
    "</process-archive>" +
  
    "</process-application>";  
    
  @Deployment
  public static WebArchive processArchive() {    

    Asset pa2ProcessesXml = TestHelper.getStringAsAssetWithReplacements(
            PROCESSES_XML, 
            new String[][]{});
    
    
    Asset[] processAssets = TestHelper.generateProcessAssets(9);
        
    JavaArchive pa2 = ShrinkWrap.create(JavaArchive.class, "pa2.jar")            
            .addAsResource(processAssets[0], "process0.bpmn")
            .addAsResource(processAssets[1], "directory/process1.bpmn");
            
    
    JavaArchive pa3 = ShrinkWrap.create(JavaArchive.class, "pa3.jar")
            .addAsResource(processAssets[0], "process0.bpmn")
            .addAsResource(processAssets[2], "alternateDirectory/process2.bpmn");
       
    return ShrinkWrap.create(WebArchive.class, "test.war")
            .addAsResource(pa2ProcessesXml, "META-INF/processes.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsLibraries(DeploymentHelper.getFoxPlatformClient())
            
            .addAsLibraries(pa2)
            .addAsLibraries(pa3)

            .addClass(AbstractFoxPlatformIntegrationTest.class);    
  }
  
  @Test
  public void testDeployProcessArchive() {
    
    assertProcessNotDeployed("process-0");
    assertProcessDeployed   ("process-1", "pa1");
    assertProcessDeployed   ("process-2", "pa2");
    
  }
  
  protected void assertProcessNotDeployed(String processKey) {
    
    long count = repositoryService
        .createProcessDefinitionQuery()
        .latestVersion()
        .processDefinitionKey(processKey)
        .count();
    
    Assert.assertEquals("Process with key "+processKey+ " should not be deployed", 0, count);
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

}
