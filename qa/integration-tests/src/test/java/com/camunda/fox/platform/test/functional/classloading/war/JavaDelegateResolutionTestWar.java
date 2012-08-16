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
package com.camunda.fox.platform.test.functional.classloading.war;
import javax.transaction.SystemException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.functional.classloading.beans.ExampleDelegate;
import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

/**
 * <p>Deploys two different WAR applications, a process archive and a client application.</p>
 * 
 * <p>This test ensures that when the process is started from the client,
 * it is able to make the context switch to the process archvie and resolve classes from the 
 * process archive.</p> 
 * 
 * 
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class JavaDelegateResolutionTestWar extends AbstractFoxPlatformIntegrationTest {
    
  @Deployment
  public static WebArchive createProcessArchiveDeplyoment() {    
    return initWebArchiveDeployment()
            .addClass(ExampleDelegate.class)            
            .addAsResource("com/camunda/fox/platform/test/functional/classloading/JavaDelegateResolutionTest.testResolveClass.bpmn20.xml")
            .addAsResource("com/camunda/fox/platform/test/functional/classloading/JavaDelegateResolutionTest.testResolveClassFromJobExecutor.bpmn20.xml");
  }
  
  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {    
    return ShrinkWrap.create(WebArchive.class, "client.war")
            .addClass(AbstractFoxPlatformIntegrationTest.class);
  }
  
  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveClass() {   
    // assert that we cannot load the delegate here:
    try {
      Class.forName("com.camunda.fox.platform.test.functional.classloading.ExampleDelegate");
      Assert.fail("CNFE expected");
    }catch (ClassNotFoundException e) {
      // expected
    }
    
    // but the process can since it performs context switch to the process archvie vor execution
    runtimeService.startProcessInstanceByKey("testResolveClass");    
  }
  
  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveClassFromJobExecutor() throws InterruptedException, SystemException {
    
    runtimeService.startProcessInstanceByKey("testResolveClassFromJobExecutor");        
    
    Assert.assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    
    waitForJobExecutorToProcessAllJobs(16000, 100);
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
  }
  
}
