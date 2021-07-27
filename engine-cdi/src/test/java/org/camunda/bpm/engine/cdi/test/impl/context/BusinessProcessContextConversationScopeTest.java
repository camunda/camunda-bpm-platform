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
package org.camunda.bpm.engine.cdi.test.impl.context;

import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.cdi.test.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.cdi.test.impl.beans.CreditCard;
import org.camunda.bpm.engine.cdi.test.impl.beans.ProcessScopedMessageBean;
import org.camunda.bpm.engine.test.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class BusinessProcessContextConversationScopeTest extends CdiProcessEngineTestCase {

  @Test
  @Deployment
  public void testConversationalBeanStoreFlush() throws Exception {

    getBeanInstance(BusinessProcess.class).setVariable("testVariable", "testValue");
    String pid =  getBeanInstance(BusinessProcess.class).startProcessByKey("testConversationalBeanStoreFlush").getId();

    getBeanInstance(BusinessProcess.class).associateExecutionById(pid);

    // assert that the variable assigned on the businessProcess bean is flushed
    assertEquals("testValue", runtimeService.getVariable(pid, "testVariable"));

    // assert that the value set to the message bean in the first service task is flushed
    assertEquals("Greetings from Berlin", getBeanInstance(ProcessScopedMessageBean.class).getMessage());

    // complete the task to allow the process instance to terminate
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
  }
    
}
