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
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.FoxPlatformIntegrationTest;


@RunWith(Arquillian.class)
public class TestWarDeployment extends FoxPlatformIntegrationTest {
  
  private static final String PROCESS_ARCHIVE = "processArchive";
  private static final String EMPTY_PROCESS_ARCHIVE = "emptyProcessArchive";
  
  @Deployment(name=EMPTY_PROCESS_ARCHIVE)
  public static WebArchive emptyProcessArchive() {
    
    return ShrinkWrap.create(WebArchive.class, "test.war")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "classes/META-INF/processes.xml")
            .addAsLibraries(getFoxPlatformClient());           
  }

  @Deployment(name=PROCESS_ARCHIVE)
  public static WebArchive processArchive() {
    
    return ShrinkWrap.create(WebArchive.class, "test.war")
            .addAsWebInfResource("META-INF/processes.xml", "classes/META-INF/processes.xml")
            .addAsResource("com/camunda/fox/platform/test/testDeployProcessArchive.bpmn20.xml")
            .addAsLibraries(getFoxPlatformClient());           
  }
  
  @Test @OperateOnDeployment(PROCESS_ARCHIVE)
  public void testDeployProcessArchive() {
    ProcessEngine processEngine = ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    Assert.assertNotNull(processEngine);
    RepositoryService repositoryService = processEngine.getRepositoryService();
    long count = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("testDeployProcessArchive")
      .count();
    
    Assert.assertEquals(1, count);
  }
  
        
  @Test @OperateOnDeployment(EMPTY_PROCESS_ARCHIVE)
  public void testEmptyProcessArchive() {            
    ProcessEngine processEngine = ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    Assert.assertNotNull(processEngine);
  }

}
