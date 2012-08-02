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

package com.camunda.fox.platform.test.deployment.ear;

import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.deployment.ear.beans.EeComponent;
import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;
import com.camunda.fox.platform.test.util.DeploymentHelper;

/**
 * This test verifies that a process archive packaging the fox platform client
 * can be packaged inside an EAR application.
 * 
 * @author Daniel Meyer
 * 
 */
@RunWith(Arquillian.class)
public class TestFoxPlatformClientAsEjbModule_pasAsEjbModule extends AbstractFoxPlatformIntegrationTest {

  
  /**
   * This only works if EAR classloader isolation is turned OFF (which is the default in AS7)
   * 
   * test-application.ear          
   *    |-- pa.jar   
   *        |-- META-INF/processes.xml         
   *        |-- com/camunda/fox/platform/test/deployment/ear/paAsEjbModule-process.bpmn20.xml
   *                                               
   *    |-- fox-platform-client.jar  
   *        |-- META-INF/MANIFEST.MF 
   *                                               
   *    |-- test.war                                                  
   *        |-- META-INF/MANIFEST.MF 
   *        |-- WEB-INF/beans.xml
   *        |-- + test classes
   *        
   */   
  @Deployment
  public static EnterpriseArchive paAsEjbModule() throws Exception {    
    
    JavaArchive processArchive1Jar = ShrinkWrap.create(JavaArchive.class, "pa.jar")
      .addClass(EeComponent.class) // need to add at least one EE component, otherwise the jar is not detected as an EJB module by Jboss AS
      .addAsResource("com/camunda/fox/platform/test/deployment/ear/paAsEjbModule-process.bpmn20.xml")
      .addAsResource("com/camunda/fox/platform/test/deployment/ear/paAsEjbModule-pa.xml", "META-INF/processes.xml");
    
    JavaArchive foxPlatformClientJar = DeploymentHelper.getFoxPlatformClient();   
    
    WebArchive testJar = ShrinkWrap.create(WebArchive.class, "paAsEjbModule-test.war")
      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
      .addClass(AbstractFoxPlatformIntegrationTest.class)
      .addClass(TestFoxPlatformClientAsEjbModule_pasAsEjbModule.class);

    return ShrinkWrap.create(EnterpriseArchive.class, "paAsEjbModule.ear")            
      .addAsModule(processArchive1Jar)
      .addAsModule(foxPlatformClientJar)
      .addAsModule(testJar);    
  }
  
  @Test
  public void testPaAsEjbModule() {
    ProcessEngine processEngine = ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    Assert.assertNotNull(processEngine);
    RepositoryService repositoryService = processEngine.getRepositoryService();
    long count = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("paAsEjbModule-process")
      .count();    
    Assert.assertEquals(1, count);   
  }

}
