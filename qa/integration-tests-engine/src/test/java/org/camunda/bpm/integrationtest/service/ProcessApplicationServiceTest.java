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
package org.camunda.bpm.integrationtest.service;

import java.util.Arrays;
import java.util.Set;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class ProcessApplicationServiceTest extends AbstractFoxPlatformIntegrationTest {
  
  @Deployment(name="test1")
  public static WebArchive app1() {    
    return initWebArchiveDeployment("test1.war")
            .addAsResource("org/camunda/bpm/integrationtest/testDeployProcessArchive.bpmn20.xml");
  }
  
  @Deployment(name="test2")
  public static WebArchive app2() {    
    return initWebArchiveDeployment("test2.war")
            .addAsResource("org/camunda/bpm/integrationtest/testDeployProcessArchiveWithoutActivitiCdi.bpmn20.xml");
  }
  
  @Test
  @OperateOnDeployment("test1")
  public void testProcessApplicationsDeployed() {
    
    ProcessApplicationService processApplicationService = BpmPlatform.getProcessApplicationService();
    
    Set<String> processApplicationNames = processApplicationService.getProcessApplicationNames();

    // check if the new applications are deployed with allowed names
    processApplicationNames.retainAll(Arrays.asList(new String [] {"test1", "test2", "/test1", "/test2"}));

    Assert.assertEquals(2, processApplicationNames.size());

    for (String appName : processApplicationNames) {
      ProcessApplicationInfo processApplicationInfo = processApplicationService.getProcessApplicationInfo(appName);
      
      Assert.assertNotNull(processApplicationInfo);
      Assert.assertNotNull(processApplicationInfo.getName());
      Assert.assertEquals(1, processApplicationInfo.getDeploymentInfo().size());      
    }
    
  }


}
