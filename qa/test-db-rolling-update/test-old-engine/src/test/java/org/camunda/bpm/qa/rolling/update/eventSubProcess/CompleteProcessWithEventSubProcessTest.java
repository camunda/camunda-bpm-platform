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
package org.camunda.bpm.qa.rolling.update.eventSubProcess;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.rolling.update.AbstractRollingUpdateTestCase;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@ScenarioUnderTest("ProcessWithEventSubProcessScenario")
public class CompleteProcessWithEventSubProcessTest extends AbstractRollingUpdateTestCase {

  @Test
  @ScenarioUnderTest("init.1")
  public void testCompleteProcessWithEventSubProcess() {
    //given process within event sub process
    ProcessInstance oldInstance = rule.processInstance();
    Assert.assertNotNull(oldInstance);
    Job job = rule.jobQuery().singleResult();
    Assert.assertNotNull(job);

    //when job is executed
    rule.getManagementService().executeJob(job.getId());

    //then delegate fails and event sub process is called
    Task task = rule.getTaskService()
                    .createTaskQuery()
                    .processInstanceId(oldInstance.getId())
                    .taskName("TaskInEventSubProcess").singleResult();
    Assert.assertNotNull(task);
    rule.getTaskService().complete(task.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.error.1")
  public void testCompleteProcessWithInEventSubProcess() {
    //given process within event sub process
    ProcessInstance oldInstance = rule.processInstance();
    Task task = rule.getTaskService()
                    .createTaskQuery()
                    .processInstanceId(oldInstance.getId())
                    .taskName("TaskInEventSubProcess").singleResult();
    Assert.assertNotNull(task);

    //when task is completed
    rule.getTaskService().complete(task.getId());

    //process instance is ended
    rule.assertScenarioEnded();
  }
}
