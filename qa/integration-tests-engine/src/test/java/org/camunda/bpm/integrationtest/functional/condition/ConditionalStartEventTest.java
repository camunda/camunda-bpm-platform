/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.integrationtest.functional.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.integrationtest.functional.condition.bean.ConditionalBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConditionalStartEventTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createProcessArchiveDeployment() {
    return initWebArchiveDeployment()
      .addClass(ConditionalBean.class)
      .addAsResource("org/camunda/bpm/integrationtest/functional/condition/ConditionalStartEventTest.bpmn20.xml");
  }

  @Test
  public void testStartInstanceWithBeanCondition() {
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(1, eventSubscriptions.size());
    assertEquals(EventType.CONDITONAL.name(), eventSubscriptions.get(0).getEventType());

    List<ProcessInstance> instances = runtimeService
        .createConditionEvaluation()
        .setVariable("foo", 1)
        .evaluateStartConditions();

    assertEquals(1, instances.size());

    assertNotNull(runtimeService.createProcessInstanceQuery().processDefinitionKey("conditionalEventProcess").singleResult());

    VariableInstance vars = runtimeService.createVariableInstanceQuery().singleResult();
    assertEquals(vars.getProcessInstanceId(), instances.get(0).getId());
    assertEquals(1, vars.getValue());
  }
}
