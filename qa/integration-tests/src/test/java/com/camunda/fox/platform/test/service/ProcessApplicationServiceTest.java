/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.test.service;

import java.util.Set;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

/**
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class ProcessApplicationServiceTest extends AbstractFoxPlatformIntegrationTest {
  
  @Deployment(name="test1")
  public static WebArchive app1() {    
    return initWebArchiveDeployment("test1.war")
            .addAsResource("com/camunda/fox/platform/test/testDeployProcessArchive.bpmn20.xml");
  }
  
  @Deployment(name="test2")
  public static WebArchive app2() {    
    return initWebArchiveDeployment("test2.war")
            .addAsResource("com/camunda/fox/platform/test/testDeployProcessArchiveWithoutActivitiCdi.bpmn20.xml");
  }
  
  @Test
  @OperateOnDeployment("test1")
  public void testProcessApplicationsDeployed() {
    
    ProcessApplicationService processApplicationService = BpmPlatform.getProcessApplicationService();
    
    Set<String> processApplicationNames = processApplicationService.getProcessApplicationNames();
    
    Assert.assertEquals(2, processApplicationNames.size());
    
    for (String appName : processApplicationNames) {
      ProcessApplicationInfo processApplicationInfo = processApplicationService.getProcessApplicationInfo(appName);
      
      Assert.assertNotNull(processApplicationInfo);
      Assert.assertNotNull(processApplicationInfo.getName());
      Assert.assertEquals(1, processApplicationInfo.getDeploymentInfo().size());      
    }
    
  }


}
