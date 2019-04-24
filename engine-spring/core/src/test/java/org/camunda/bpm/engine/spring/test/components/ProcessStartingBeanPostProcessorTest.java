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
package org.camunda.bpm.engine.spring.test.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Josh Long
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:org/camunda/bpm/engine/spring/test/components/ProcessStartingBeanPostProcessorTest-context.xml")
public class ProcessStartingBeanPostProcessorTest {

	private Logger log = Logger.getLogger(getClass().getName());

	@Autowired
	private ProcessEngine processEngine;

	@Autowired
	private ProcessInitiatingPojo processInitiatingPojo;

	@Autowired
	private RepositoryService repositoryService;

	@Before
	public void before() {
	  repositoryService.createDeployment()
	    .addClasspathResource("org/camunda/bpm/engine/spring/test/autodeployment/autodeploy.b.bpmn20.xml")
	    .addClasspathResource("org/camunda/bpm/engine/spring/test/components/waiter.bpmn20.xml")
	    .deploy();
	}

	@After
  public void after() {
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    processEngine.close();
    processEngine = null;
    processInitiatingPojo = null;
    repositoryService = null;
  }

	@Test
	public void testReturnedProcessInstance() throws Throwable {
		String processInstanceId = this.processInitiatingPojo.startProcessA(22);
		assertNotNull("the process instance id should not be null", processInstanceId);
	}

	@Test
	public void testReflectingSideEffects() throws Throwable {
		assertNotNull("the processInitiatingPojo mustn't be null.", this.processInitiatingPojo);

		this.processInitiatingPojo.reset();

		assertEquals(this.processInitiatingPojo.getMethodState(), 0);

		this.processInitiatingPojo.startProcess(53);

		assertEquals(this.processInitiatingPojo.getMethodState(), 1);
	}

	@Test
	public void testUsingBusinessKey() throws Throwable {
		long id = 5;
		String businessKey = "usersKey" + System.currentTimeMillis();
		ProcessInstance pi = processInitiatingPojo.enrollCustomer(businessKey, id);
		assertEquals("the business key of the resultant ProcessInstance should match " +
				"the one specified through the AOP-intercepted method" ,businessKey, pi.getBusinessKey());

	}

	@Test
	public void testLaunchingProcessInstance() {
		long id = 343;
		String processInstance = processInitiatingPojo.startProcessA(id);
		Long customerId = (Long) processEngine.getRuntimeService().getVariable(processInstance, "customerId");
		assertEquals("the process variable should both exist and be equal to the value given, " + id, customerId, (Long) id);
		log.info("the customerId fromt he ProcessInstance is " + customerId);
		assertNotNull("processInstanc can't be null", processInstance);
		assertNotNull("the variable should be non-null", customerId);
	}
}
