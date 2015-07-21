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
package org.camunda.bpm.integrationtest.jobexecutor;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.integrationtest.jobexecutor.beans.PriorityBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Requires fix for CAM-3163
 *
 * @author Thorben Lindhauer
 */
@RunWith(Arquillian.class)
@Ignore
public class JobPrioritizationDuringDeploymentTest extends AbstractFoxPlatformIntegrationTest {

  protected ProcessEngine engine1;
  protected RuntimeService runtimeService1;
  protected ManagementService managementService1;

  @ArquillianResource
  protected Deployer deployer;

  @Override
  public void setupBeforeTest() {
    // don't lookup the default engine since this method is not executed in the deployment
  }

  // deploy this manually
  @Deployment(name="timerStart", managed = false)
  public static WebArchive createTimerStartDeployment() {
    return initWebArchiveDeployment("pa1.war", "org/camunda/bpm/integrationtest/jobexecutor/jobPriorityEngine.xml")
      .addClass(PriorityBean.class)
      .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/JobPrioritizationDuringDeploymentTest.timerStart.bpmn20.xml");

  }

  @Test
  @InSequence(1)
  public void testPriorityOnTimerStartEvent() {
    // when
    try {
      deployer.deploy("timerStart");

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("deployment should be successful, i.e. bean for timer start event should get resolved");
    }
  }

  @Test
  @OperateOnDeployment("timerStart")
  @InSequence(2)
  public void testAssertPriority() {

    lookupDeployedEngine();

    // then the timer start event job has the priority resolved from the bean
    Job job = managementService1.createJobQuery().activityId("timerStart").singleResult();

    Assert.assertNotNull(job);
    Assert.assertEquals(PriorityBean.PRIORITY, job.getPriority());
  }

  protected void lookupDeployedEngine() {
    ProcessEngineService engineService = BpmPlatform.getProcessEngineService();
    engine1 = engineService.getProcessEngine("engine1");
    runtimeService1 = engine1.getRuntimeService();
    managementService1 = engine1.getManagementService();
  }
}
