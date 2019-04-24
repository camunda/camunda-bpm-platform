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
package org.camunda.bpm.integrationtest.functional.classloading.war;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.functional.classloading.beans.ExampleSignallableActivityBehavior;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 *
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class SignallableActivityBehaviorResolutionTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createProcessArchiveDeplyoment() {
    return initWebArchiveDeployment()
            .addClass(ExampleSignallableActivityBehavior.class)
            .addAsResource("org/camunda/bpm/integrationtest/functional/classloading/SignallableActivityBehaviorResolutionTest.testResolveClass.bpmn20.xml")
            .addAsResource("org/camunda/bpm/integrationtest/functional/classloading/SignallableActivityBehaviorResolutionTest.testResolveClassFromJobExecutor.bpmn20.xml");
  }

  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "client.war")
            .addClass(AbstractFoxPlatformIntegrationTest.class);

    TestContainer.addContainerSpecificResources(webArchive);

    return webArchive;

  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveClass() {
    // assert that we cannot load the delegate here:
    try {
      Class.forName("org.camunda.bpm.integrationtest.functional.classloading.beans.ExampleSignallableActivityBehavior");
      Assert.fail("CNFE expected");
    }catch (ClassNotFoundException e) {
      // expected
    }

    // but the process can since it performs context switch to the process archive for execution
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testResolveClass");

    runtimeService.signal(processInstance.getId());

  }


  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveClassFromJobExecutor() throws InterruptedException {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testResolveClassFromJobExecutor");

    Assert.assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());

    waitForJobExecutorToProcessAllJobs();

    Assert.assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());

    runtimeService.signal(processInstance.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());

  }

}
