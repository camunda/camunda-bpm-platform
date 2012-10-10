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
package com.camunda.fox.platform.test.jobexecutor;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.jobexecutor.beans.DemoDelegate;
import com.camunda.fox.platform.test.jobexecutor.beans.DemoVariableClass;
import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

/**
 * Test fox platform container job exectuor.
 * FAILING ATM!
 * Expected a job with an exception but it isn't left in db with 0 retries, instead it is completely removed from the job table!
 *  
 * @author christian.lipphardt@camunda.com
 */
@RunWith(Arquillian.class)
public class FailingJobBoundaryTimerWithDelegateVariablesTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
            .addClass(DemoDelegate.class)
            .addClass(DemoVariableClass.class)
            .addAsResource("com/camunda/fox/platform/test/jobexecutor/ImmediatelyFailing.bpmn20.xml")
            .addAsWebInfResource("persistence.xml", "classes/META-INF/persistence.xml");
  }

  @Inject
  private RuntimeService runtimeService;
  @Inject
  private ManagementService managementService;

  @Test
  public void testFailingJobBoundaryTimerWithDelegateVariables() throws InterruptedException {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("ImmediatelyFailing");

    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(1, jobs.size());
    assertEquals(3, jobs.get(0).getRetries());
    
    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(pi.getProcessInstanceId()).activityId("usertask1").count());
    assertEquals(2, runtimeService.createExecutionQuery().processInstanceId(pi.getProcessInstanceId()).count());
    
    waitForJobExecutorToProcessAllJobs(21000, 5000);

    assertEquals(0, managementService.createJobQuery().executable().count()); // should be 0, because it has failed 3 times
    assertEquals(1, managementService.createJobQuery().withException().count()); // should be 1, because job failed!
    
    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(pi.getProcessInstanceId()).activityId("usertask1").count());
    assertEquals(2, runtimeService.createExecutionQuery().processInstanceId(pi.getProcessInstanceId()).count());
    
    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId()); // complete task with failed job => complete process
    
    assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(pi.getProcessInstanceId()).count());
    assertEquals(0, managementService.createJobQuery().count()); // should be 0, because process is finished.
  }

}
