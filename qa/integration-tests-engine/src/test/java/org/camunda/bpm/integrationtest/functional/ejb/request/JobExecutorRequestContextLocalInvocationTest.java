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
import org.camunda.bpm.integrationtest.functional.cdi.beans.RequestScopedDelegateBean;
import org.camunda.bpm.integrationtest.functional.ejb.request.beans.InvocationCounter;
import org.camunda.bpm.integrationtest.functional.ejb.request.beans.InvocationCounterDelegateBean;
import org.camunda.bpm.integrationtest.functional.ejb.request.beans.InvocationCounterDelegateBeanLocal;
import org.camunda.bpm.integrationtest.functional.ejb.request.beans.InvocationCounterService;
import org.camunda.bpm.integrationtest.functional.ejb.request.beans.InvocationCounterServiceBean;
import org.camunda.bpm.integrationtest.functional.ejb.request.beans.InvocationCounterServiceLocal;
import org.camunda.bpm.integrationtest.functional.ejb.request.beans.RequestScopedSFSBDelegate;
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
 * This test verifies that if a delegate bean invoked from the Job Executor
 * calls a LOCAL SLSB from a different deployment, the RequestContest is active there as well.
 *
 * NOTE:
 * - does not work on Jboss (Bug in Jboss AS?) SEE HEMERA-2453
 * - not implemented on Glassfish
 *
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class JobExecutorRequestContextLocalInvocationTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment(name="pa", order=2)
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
      .addClass(RequestScopedDelegateBean.class)
      .addClass(RequestScopedSFSBDelegate.class)
      .addClass(InvocationCounterDelegateBean.class)
      .addClass(InvocationCounterDelegateBeanLocal.class)
      .addAsResource("org/camunda/bpm/integrationtest/functional/ejb/request/JobExecutorRequestContextLocalInvocationTest.testContextPropagationEjbLocal.bpmn20.xml")
      .addAsWebInfResource("org/camunda/bpm/integrationtest/functional/ejb/request/jboss-deployment-structure.xml","jboss-deployment-structure.xml");
  }

  @Deployment(order=1)
  public static WebArchive delegateDeployment() {

    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "service.war")
      .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
      .addClass(AbstractFoxPlatformIntegrationTest.class)
      .addClass(InvocationCounter.class) // @RequestScoped CDI bean
      .addClass(InvocationCounterService.class) // interface (remote)
      .addClass(InvocationCounterServiceLocal.class) // interface (local)
      .addClass(InvocationCounterServiceBean.class); // @Stateless ejb

    TestContainer.addContainerSpecificResourcesForNonPa(webArchive);

    return webArchive;
  }

  @Test
  @OperateOnDeployment("pa")
  public void testRequestContextPropagationEjbLocal() throws Exception{

    // This fails with  WELD-001303 No active contexts for scope type javax.enterprise.context.RequestScoped as well

//    InvocationCounterServiceLocal service = InitialContext.doLookup("java:/" +
//    "global/" +
//    "service/" +
//    "InvocationCounterServiceBean!org.camunda.bpm.integrationtest.functional.cdi.beans.InvocationCounterServiceLocal");
//
//    service.getNumOfInvocations();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testContextPropagationEjbLocal");

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

}
