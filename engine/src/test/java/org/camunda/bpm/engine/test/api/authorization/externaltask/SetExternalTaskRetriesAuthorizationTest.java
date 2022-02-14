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
package org.camunda.bpm.engine.test.api.authorization.externaltask;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Parameterized.class)
public class SetExternalTaskRetriesAuthorizationTest extends HandleExternalTaskAuthorizationTest {

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  public void testSetRetries() {

    // given
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("oneExternalTaskProcess");
    ExternalTask task = engineRule.getExternalTaskService().createExternalTaskQuery().singleResult();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("processInstanceId", processInstance.getId())
      .bindResource("processDefinitionKey", "oneExternalTaskProcess")
      .start();

    engineRule.getExternalTaskService().setRetries(task.getId(), 5);

    // then
    if (authRule.assertScenario(scenario)) {
      task = engineRule.getExternalTaskService().createExternalTaskQuery().singleResult();
      Assert.assertEquals(5, (int) task.getRetries());
    }

  }
}
