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
package org.camunda.bpm.integrationtest.functional.ejb.request;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.integrationtest.functional.ejb.request.beans.RequestScopedSFSBDelegate;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * This test verifies that if the same @RequestScoped SFSB Bean is invoked multiple times
 * in the context of the same job, we get the same instance.
 *
 * NOTE:
 * - works on Jboss AS
 * - broken on Glassfish, see HEMERA-2454
 *
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class JobExecutorRequestContextSFSBDelegateTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment(name="pa", order=2)
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
      .addClass(RequestScopedSFSBDelegate.class)
      .addAsResource("org/camunda/bpm/integrationtest/functional/ejb/request/JobExecutorRequestContextSFSBDelegateTest.testScopingSFSB.bpmn20.xml");
  }


  @Test
  @OperateOnDeployment("pa")
  public void testScopingSFSB() {

    // verifies that if the same @RequestScoped SFSB Bean is invoked multiple times
    // in the context of the same job, we get the same instance

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testScopingSFSB");

    waitForJobExecutorToProcessAllJobs();

    Object variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    // -> the same bean instance was invoked 2 times!
    Assert.assertEquals(2, variable);

    Task task = taskService.createTaskQuery()
      .processInstanceId(pi.getProcessInstanceId())
      .singleResult();
    taskService.complete(task.getId());

    waitForJobExecutorToProcessAllJobs();

    variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    // now it's '1' again! -> new instance of the bean
    Assert.assertEquals(1, variable);

  }

  @Test
  public void testMultipleInvocations() {

    // this is greater than any Datasource- / EJB- / Thread-Pool size -> make sure all resources are released properly.
    int instances = 100;
    String[] ids = new String[instances];

    for(int i=0; i<instances; i++) {
      ids[i] = runtimeService.startProcessInstanceByKey("testScopingSFSB").getId();
    }

    waitForJobExecutorToProcessAllJobs(60*1000);

    for(int i=0; i<instances; i++) {
      Object variable = runtimeService.getVariable(ids[i], "invocationCounter");
      // -> the same bean instance was invoked 2 times!
      Assert.assertEquals(2, variable);

      taskService.complete(taskService.createTaskQuery().processInstanceId(ids[i]).singleResult().getId());
    }

    waitForJobExecutorToProcessAllJobs(60*1000);

    for(int i=0; i<instances; i++) {
      // now it's '1' again! -> new instance of the bean
      Assert.assertEquals(1, runtimeService.getVariable(ids[i], "invocationCounter"));
    }


  }

}
