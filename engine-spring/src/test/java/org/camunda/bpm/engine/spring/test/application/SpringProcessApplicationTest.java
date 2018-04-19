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
package org.camunda.bpm.engine.spring.test.application;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.spring.application.SpringProcessApplication;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>Testcases for {@link SpringProcessApplication}</p>
 *
 * @author Daniel Meyer
 *
 */
public class SpringProcessApplicationTest {

  @Test
  public void testProcessApplicationDeployment() {

    // initially no applications are deployed:
    Assert.assertEquals(0, BpmPlatform.getProcessApplicationService().getProcessApplicationNames().size());

    // start a spring application context
    AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext("org/camunda/bpm/engine/spring/test/application/SpringProcessApplicationDeploymentTest-context.xml");
    applicationContext.start();

    // assert that there is a process application deployed with the name of the process application bean
    Assert.assertNotNull(BpmPlatform.getProcessApplicationService()
      .getProcessApplicationInfo("myProcessApplication"));

    // close the spring application context
    applicationContext.close();

    // after closing the application context, the process application is undeployed.
    Assert.assertNull(BpmPlatform.getProcessApplicationService()
      .getProcessApplicationInfo("myProcessApplication"));

  }

  @Test
  public void testDeployProcessArchive() {

    // start a spring application context
    AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext("org/camunda/bpm/engine/spring/test/application/SpringProcessArchiveDeploymentTest-context.xml");
    applicationContext.start();

    // assert the process archive is deployed:
    ProcessEngine processEngine = BpmPlatform.getDefaultProcessEngine();
    Assert.assertNotNull(processEngine.getRepositoryService().createDeploymentQuery().deploymentName("pa").singleResult());

    applicationContext.close();

    // assert the process is undeployed
    Assert.assertNull(processEngine.getRepositoryService().createDeploymentQuery().deploymentName("pa").singleResult());

  }

  @Test
  public void testPostDeployRegistrationPa() {
    // this test verifies that a process application is able to register a deployment from the @PostDeploy callback:

    AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext("org/camunda/bpm/engine/spring/test/application/PostDeployRegistrationPaTest-context.xml");
    applicationContext.start();

    ProcessEngine processEngine = BpmPlatform.getDefaultProcessEngine();

    // create a manual deployment:
    Deployment deployment = processEngine.getRepositoryService()
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/spring/test/application/process.bpmn20.xml")
      .deploy();

    // lookup the process application spring bean:
    PostDeployRegistrationPa processApplication = applicationContext.getBean("customProcessApplicaiton", PostDeployRegistrationPa.class);

    Assert.assertFalse(processApplication.isPostDeployInvoked());
    processApplication.deploy();
    Assert.assertTrue(processApplication.isPostDeployInvoked());

    // the process application was not invoked
    Assert.assertFalse(processApplication.isInvoked());

    // start process instance:
    processEngine.getRuntimeService()
      .startProcessInstanceByKey("startToEnd");

    // now the process application was invoked:
    Assert.assertTrue(processApplication.isInvoked());

    // undeploy PA
    Assert.assertFalse(processApplication.isPreUndeployInvoked());
    processApplication.undeploy();
    Assert.assertTrue(processApplication.isPreUndeployInvoked());

    // manually undeploy the process
    processEngine.getRepositoryService()
      .deleteDeployment(deployment.getId(), true);

    applicationContext.close();

  }

  @Test
  public void testPostDeployWithNestedContext() {
    /*
     * This test case checks if the process application deployment is done when
     * application context is refreshed, but not when child contexts are
     * refreshed.
     * 
     * As a side test it checks if events thrown in the PostDeploy-method are
     * catched by the main application context.
     */

    AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext(
        "org/camunda/bpm/engine/spring/test/application/PostDeployWithNestedContext-context.xml");
    applicationContext.start();

    // lookup the process application spring bean:
    PostDeployWithNestedContext processApplication = applicationContext.getBean("customProcessApplicaiton", PostDeployWithNestedContext.class);

    Assert.assertFalse(processApplication.isDeployOnChildRefresh());
    Assert.assertTrue(processApplication.isLateEventTriggered());

    processApplication.undeploy();
    applicationContext.close();
  }

}
