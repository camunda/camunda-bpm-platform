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
package org.camunda.bpm.integrationtest.functional.cdi;
import org.camunda.bpm.engine.cdi.impl.util.BeanManagerLookup;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.integrationtest.functional.cdi.beans.ExampleDelegateBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
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
 * <p>Deploys two different applications, a process archive and a cleint application.</p>
 *
 * <p>This test ensures that when the process is started from the client,
 * it is able to make the context switch to the process archvie and resolve cdi beans
 * from the process archive.</p>
 *
 *
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class CdiDelegateBeanResolutionTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
            .addClass(ExampleDelegateBean.class)
            .addAsResource("org/camunda/bpm/integrationtest/functional/cdi/CdiDelegateBeanResolutionTest.testResolveBean.bpmn20.xml")
            .addAsResource("org/camunda/bpm/integrationtest/functional/cdi/CdiDelegateBeanResolutionTest.testResolveBeanFromJobExecutor.bpmn20.xml");
  }

  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {
     WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "client.war")
            .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
            .addClass(ProgrammaticBeanLookup.class)
            .addClass(BeanManagerLookup.class)
            .addClass(AbstractFoxPlatformIntegrationTest.class)
            .addAsLibraries(DeploymentHelper.getEngineCdi());

     TestContainer.addContainerSpecificResourcesEmbedCdiLib(webArchive);

     return webArchive;
  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveBean() {
    try {
      // assert that we cannot resolve the bean here:
      ProgrammaticBeanLookup.lookup("exampleDelegateBean");
      Assert.fail("exception expected");
    }catch (Throwable e) {
      // expected
    }

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("testResolveBean").count());
    // but the process engine can:
    runtimeService.startProcessInstanceByKey("testResolveBean");

    Assert.assertEquals(0,runtimeService.createProcessInstanceQuery().processDefinitionKey("testResolveBean").count());
  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveBeanFromJobExecutor() {

    Assert.assertEquals(0,runtimeService.createProcessInstanceQuery().processDefinitionKey("testResolveBeanFromJobExecutor").count());
    runtimeService.startProcessInstanceByKey("testResolveBeanFromJobExecutor");
    Assert.assertEquals(1,runtimeService.createProcessInstanceQuery().processDefinitionKey("testResolveBeanFromJobExecutor").count());

    waitForJobExecutorToProcessAllJobs();

    Assert.assertEquals(0,runtimeService.createProcessInstanceQuery().processDefinitionKey("testResolveBeanFromJobExecutor").count());

  }

}
