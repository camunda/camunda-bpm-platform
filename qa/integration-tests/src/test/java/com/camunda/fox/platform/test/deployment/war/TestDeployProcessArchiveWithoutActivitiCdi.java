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

import javax.ejb.EJB;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.util.MyTestDelegate;
import com.camunda.fox.processarchive.ProcessArchiveSupport;

// this test assures that we can use the fox platform to execute processes
// without activiti-cdi. 
// we deploy the com.camunda.fox.processarchive.* classes which lookup the
// ProcessArchiveService and install the process archive.
// the process engine can now be accessed using the ProcessArchiveSupport EJB 

@RunWith(Arquillian.class)
public class TestDeployProcessArchiveWithoutActivitiCdi {

  @Deployment
  public static JavaArchive createDeployment() {

    return ShrinkWrap.create(JavaArchive.class, "test.jar")
            .addAsManifestResource("META-INF/processes.xml", "processes.xml")
            .addPackages(true, "com.camunda.fox.processarchive")
            .addClass(MyTestDelegate.class)
            .addAsManifestResource("ARQUILLIAN-MANIFEST-JBOSS7.MF", "MANIFEST.MF")
            .addAsResource("com/camunda/fox/platform/test/testDeployProcessArchiveWithoutActivitiCdi.bpmn20.xml");

  }
   
  @EJB
  protected ProcessArchiveSupport processArchiveSupport;
    
  @Test
  public void testDeploy() {
    final ProcessEngine processEngine = processArchiveSupport.getProcessEngine();    
    final RuntimeService runtimeService = processEngine.getRuntimeService();
    
    MyTestDelegate.setInvoked(false);
    
    runtimeService.startProcessInstanceByKey("testDeployProcessArchive");
    
    Assert.assertTrue(MyTestDelegate.isInvoked());
    
    
  }

}
