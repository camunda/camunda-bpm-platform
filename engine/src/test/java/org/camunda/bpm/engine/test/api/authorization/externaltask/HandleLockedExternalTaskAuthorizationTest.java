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

import java.util.List;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * Represents a base class for authorization test cases to handle
 * an already locked (single) external task.
 * 
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public abstract class HandleLockedExternalTaskAuthorizationTest extends HandleExternalTaskAuthorizationTest {

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  public void testCompleteExternalTask() {

    // given
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("oneExternalTaskProcess");
    List<LockedExternalTask> tasks = engineRule.getExternalTaskService()
        .fetchAndLock(5, "workerId")
        .topic("externalTaskTopic", 5000L)
        .execute();

    LockedExternalTask task = tasks.get(0);

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("processInstanceId", processInstance.getId())
      .bindResource("processDefinitionKey", "oneExternalTaskProcess")
      .start();

    testExternalTaskApi(task);

    // then
    if (authRule.assertScenario(scenario)) {      
      assertExternalTaskResults();
    }
  }
  
  /**
   * Tests or either executes the external task api.
   * The given locked external task is used to test there api.
   * 
   * @param task the external task which should be tested
   */
  public abstract void testExternalTaskApi(LockedExternalTask task);
  
  /**
   *  Contains assertions for the external task results, which are executed after the external task 
   *  was executed.
   */
  public abstract void assertExternalTaskResults();

}
