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
package org.camunda.bpm.integrationtest.deployment.callbacks;

import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;

import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class PostDeployFailureTest_OTHERS extends AbstractFoxPlatformIntegrationTest {
  
  @Deployment(name="fail")
  public static Archive<?> createDeployment1() {
   return processArchiveDeployment(PostDeployFailureTest_JBOSS.createDeployment1());
  }
  
  @Deployment(name="checker")
  public static Archive<?> createDeployment2() {
    return processArchiveDeployment(initWebArchiveDeployment("checker.war"));
  }
  
  @Test
  @OperateOnDeployment("checker")
  public void test() {
    
    // make sure the deployment of the first app was rolled back
    
    long count = processEngine.getRepositoryService()
      .createDeploymentQuery()
      .count();
    
    Assert.assertEquals(1, count);
       
  }
  
}
