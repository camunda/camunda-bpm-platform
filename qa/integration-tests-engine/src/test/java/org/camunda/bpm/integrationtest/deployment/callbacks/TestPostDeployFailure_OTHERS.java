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
package org.camunda.bpm.integrationtest.deployment.callbacks;

import org.junit.Assert;

import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
@Ignore
public class TestPostDeployFailure_OTHERS extends AbstractFoxPlatformIntegrationTest {
  
  @Deployment(name="fail")
  public static WebArchive createDeployment1() {    
   return TestPostDeployFailure_JBOSS.createDeployment1();    
  }
  
  @Deployment(name="checker")
  public static WebArchive createDeployment2() {    
    return initWebArchiveDeployment("checker.war");
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
