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
package com.camunda.fox.platform.test.services;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ejb.EJB;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.api.ProcessEngineService.ProcessEngineStartOperation;
import com.camunda.fox.platform.test.services.util.ProcessEngineConfigurationImpl;


@RunWith(Arquillian.class)
public class PlatformServicesTest {
  
  public final static String PROCESS_ARCHIVE_SERVICE_NAME =
          "java:global/" +
          "camunda-fox-platform/" +
          "process-engine/" +
          "PlatformService!com.camunda.fox.platform.api.ProcessArchiveService";
  
  public final static String PROCESS_ENGINE_SERVICE_NAME =
          "java:global/" +
          "camunda-fox-platform/" +
          "process-engine/" +
          "PlatformService!com.camunda.fox.platform.api.ProcessArchiveService";  
  
  @Deployment
  public static WebArchive processArchive() {    
    return ShrinkWrap.create(WebArchive.class, "test.war")
            .addClass(ProcessEngineConfigurationImpl.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");           
  }
  
  @EJB(lookup=PROCESS_ENGINE_SERVICE_NAME)
  private ProcessEngineService processEngineService;
  
  @Test
  public void testStartStopProcessEngine() throws InterruptedException, ExecutionException {
    Assert.assertEquals(1, processEngineService.getProcessEngines().size());
    
    ProcessEngineConfigurationImpl configurationImpl = new ProcessEngineConfigurationImpl(false, "testEngine1", "java:jboss/datasources/ExampleDS", "audit", true, false);
    Future<ProcessEngineStartOperation> startProcessEngine = processEngineService.startProcessEngine(configurationImpl);
    ProcessEngineStartOperation processEngineStartOperation = startProcessEngine.get();
    
    Assert.assertEquals(2, processEngineService.getProcessEngines().size());
    
    processEngineService.stopProcessEngine(processEngineStartOperation.getProcessenEngine());
    
    Assert.assertEquals(1, processEngineService.getProcessEngines().size());
  }
  
}
