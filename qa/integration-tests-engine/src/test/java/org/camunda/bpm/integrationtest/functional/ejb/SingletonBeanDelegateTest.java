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
package org.camunda.bpm.integrationtest.functional.ejb;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.functional.ejb.beans.SingletonBeanClientDelegate;
import org.camunda.bpm.integrationtest.functional.ejb.beans.SingletonBeanDelegate;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;



/**
 * Testcase verifying various ways to use a Singleton as a JavaDelegate
 *
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class SingletonBeanDelegateTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
      .addClass(SingletonBeanDelegate.class)
      .addClass(SingletonBeanClientDelegate.class)
      .addAsResource("org/camunda/bpm/integrationtest/functional/ejb/SingletonBeanDelegateTest.testBeanResolution.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/functional/ejb/SingletonBeanDelegateTest.testBeanResolutionFromClient.bpmn20.xml");
  }


  @Test
  public void testBeanResolution() {

    // this testcase first resolves the Singleton synchronouly and then from the JobExecutor

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testBeanResolution");

    Assert.assertEquals(true, runtimeService.getVariable(pi.getId(), SingletonBeanDelegate.class.getName()));

    runtimeService.setVariable(pi.getId(), SingletonBeanDelegate.class.getName(), false);

    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());

    waitForJobExecutorToProcessAllJobs();

    Assert.assertEquals(true, runtimeService.getVariable(pi.getId(), SingletonBeanDelegate.class.getName()));

    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());

  }


  @Test
  public void testBeanResolutionfromClient() {

    // this testcase invokes a CDI bean that injects the EJB

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testBeanResolutionfromClient");

    Assert.assertEquals(true, runtimeService.getVariable(pi.getId(), SingletonBeanDelegate.class.getName()));

    runtimeService.setVariable(pi.getId(), SingletonBeanDelegate.class.getName(), false);

    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());

    waitForJobExecutorToProcessAllJobs();

    Assert.assertEquals(true, runtimeService.getVariable(pi.getId(), SingletonBeanDelegate.class.getName()));

    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());
  }

  @Test
  public void testMultipleInvocations() {

    // this is greater than any Datasource- / EJB- / Thread-Pool size -> make sure all resources are released properly.
    int instances = 100;
    String[] ids = new String[instances];

    for(int i=0; i<instances; i++) {
      ids[i] = runtimeService.startProcessInstanceByKey("testBeanResolutionfromClient").getId();
      Assert.assertEquals("Incovation=" + i, true, runtimeService.getVariable(ids[i], SingletonBeanDelegate.class.getName()));
      runtimeService.setVariable(ids[i], SingletonBeanDelegate.class.getName(), false);
      taskService.complete(taskService.createTaskQuery().processInstanceId(ids[i]).singleResult().getId());
    }

    waitForJobExecutorToProcessAllJobs(60*1000);

    for(int i=0; i<instances; i++) {
      Assert.assertEquals("Incovation=" + i, true, runtimeService.getVariable(ids[i], SingletonBeanDelegate.class.getName()));
      taskService.complete(taskService.createTaskQuery().processInstanceId(ids[i]).singleResult().getId());
    }

  }

}
