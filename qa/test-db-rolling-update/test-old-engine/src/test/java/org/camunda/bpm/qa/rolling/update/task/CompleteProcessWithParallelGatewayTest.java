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
package org.camunda.bpm.qa.rolling.update.task;

import java.util.List;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.rolling.update.AbstractRollingUpdateTestCase;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * This test ensures that the old engine can complete an
 * existing process with parallel gateway and user task on the new schema.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@ScenarioUnderTest("ProcessWithParallelGatewayScenario")
public class CompleteProcessWithParallelGatewayTest extends AbstractRollingUpdateTestCase {

  @Test
  @ScenarioUnderTest("init.none.1")
  public void testCompleteProcessWithParallelGateway() {
    //given an already started process instance with two user tasks
    ProcessInstance oldInstance = rule.processInstance();
    Assert.assertNotNull(oldInstance);

    List<Task> tasks = rule.taskQuery().list();
    Assert.assertEquals(2, tasks.size());

    //when completing the user tasks
    for (Task task : tasks) {
      rule.getTaskService().complete(task.getId());
    }

    //then there exists no more tasks
    //and the process instance is also completed
    Assert.assertEquals(0, rule.taskQuery().count());
    rule.assertScenarioEnded();
  }


  @Test
  @ScenarioUnderTest("init.complete.one.1")
  public void testCompleteProcessWithParallelGatewayAndSingleUserTask() {
    //given an already started process instance
    ProcessInstance oldInstance = rule.processInstance();
    Assert.assertNotNull(oldInstance);

    //with one completed user task
    HistoricTaskInstanceQuery historicTaskQuery = rule.getHistoryService()
            .createHistoricTaskInstanceQuery()
            .processInstanceId(oldInstance.getId())
            .finished();
    Assert.assertEquals(1, historicTaskQuery.count());

    //and one waiting
    Task task = rule.taskQuery().singleResult();
    Assert.assertNotNull(task);

    //when completing the user task
    rule.getTaskService().complete(task.getId());

    //then there exists no more tasks
    Assert.assertEquals(0, rule.taskQuery().count());
    //and two historic tasks
    Assert.assertEquals(2, historicTaskQuery.count());
    //and the process instance is also completed
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.complete.two.1")
  public void testQueryHistoricProcessWithParallelGateway() {
    //given an already finished process instance with parallel gateway and two user tasks
    HistoricProcessInstance historicProcessInstance = rule.historicProcessInstance();

    //when query history
    HistoricTaskInstanceQuery historicTaskQuery = rule.getHistoryService()
            .createHistoricTaskInstanceQuery()
            .processInstanceId(historicProcessInstance.getId());

    //then two historic user tasks are returned
    Assert.assertEquals(2, historicTaskQuery.count());
  }

}
