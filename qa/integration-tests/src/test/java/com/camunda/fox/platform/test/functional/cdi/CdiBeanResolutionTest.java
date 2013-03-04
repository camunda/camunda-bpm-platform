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
package com.camunda.fox.platform.test.functional.cdi;

import org.activiti.cdi.impl.util.BeanManagerLookup;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.functional.cdi.beans.ExampleBean;
import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;
import com.camunda.fox.platform.test.util.TestContainer;

/**
 * <p>Deploys two different applications, a process archive and a cleint application.</p>
 * 
 * <p>This test ensures that when the process is started from the client,
 * it is able to make the context switch to the process archvie and resolve cdi beans 
 * from the process archive.</p> 
 * 
 * 
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class CdiBeanResolutionTest extends AbstractFoxPlatformIntegrationTest {
    
  @Deployment
  public static WebArchive processArchive() {    
    return initWebArchiveDeployment()
            .addClass(ExampleBean.class)            
            .addAsResource("com/camunda/fox/platform/test/functional/cdi/CdiBeanResolutionTest.testResolveBean.bpmn20.xml")
            .addAsResource("com/camunda/fox/platform/test/functional/cdi/CdiBeanResolutionTest.testResolveBeanFromJobExecutor.bpmn20.xml");
  }
  
  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {    
    WebArchive deployment = ShrinkWrap.create(WebArchive.class, "client.war")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addClass(ProgrammaticBeanLookup.class)
            .addClass(BeanManagerLookup.class)
            .addClass(AbstractFoxPlatformIntegrationTest.class);
    
    TestContainer.addContainerSpecificResources(deployment);
    
    return deployment;
  }
    
  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveBean() {   
    try {
      // assert that we cannot resolve the bean here:
      ProgrammaticBeanLookup.lookup("exampleBean");
      Assert.fail("exception expected");
    }catch (Exception e) {
      // expected
    }
    
    Assert.assertEquals(0,runtimeService.createProcessInstanceQuery().count());
    // but the process engine can:
    runtimeService.startProcessInstanceByKey("testResolveBean");
    
    Assert.assertEquals(0,runtimeService.createProcessInstanceQuery().count());
  }
  
  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveBeanFromJobExecutor() {
   
    Assert.assertEquals(0,runtimeService.createProcessInstanceQuery().count());
    runtimeService.startProcessInstanceByKey("testResolveBeanFromJobExecutor");
    Assert.assertEquals(1,runtimeService.createProcessInstanceQuery().count());
    
    waitForJobExecutorToProcessAllJobs(16000, 500);    
    
    Assert.assertEquals(0,runtimeService.createProcessInstanceQuery().count());    
    
  }
  
}
