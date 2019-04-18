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
package org.camunda.bpm.integrationtest.functional.classloading.ear;
import javax.transaction.SystemException;

import org.camunda.bpm.integrationtest.functional.classloading.beans.ExampleDelegate;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * <p>Deploys an EAR application which contains a WAR process archive, and a client application deployed as a war</p>
 *
 * <p>This test ensures that when the process is started from the client,
 * it is able to make the context switch to the process archvie and resolve classes from the
 * process archive.</p>
 *
 *
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class TestJavaDelegateResolution_ClientAsLibInWebModule extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static EnterpriseArchive createProcessArchiveDeplyoment() {
    WebArchive processArchiveWar = initWebArchiveDeployment()
      .addClass(ExampleDelegate.class)
      .addAsResource("org/camunda/bpm/integrationtest/functional/classloading/JavaDelegateResolutionTest.testResolveClass.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/functional/classloading/JavaDelegateResolutionTest.testResolveClassFromJobExecutor.bpmn20.xml");

    return ShrinkWrap.create(EnterpriseArchive.class, "test-app.ear")
      .addAsModule(processArchiveWar);
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
      Class.forName("org.camunda.bpm.integrationtest.functional.classloading.ExampleDelegate");
      Assert.fail("CNFE expected");
    }catch (ClassNotFoundException e) {
      // expected
    }

    // but the process can since it performs context switch to the process archive vor execution
    runtimeService.startProcessInstanceByKey("testResolveClass");
  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveClassFromJobExecutor() throws InterruptedException, SystemException {

    runtimeService.startProcessInstanceByKey("testResolveClassFromJobExecutor");

    Assert.assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    waitForJobExecutorToProcessAllJobs();

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());

  }

}
