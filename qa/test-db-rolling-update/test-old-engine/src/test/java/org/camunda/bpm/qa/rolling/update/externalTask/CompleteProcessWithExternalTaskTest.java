/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.qa.rolling.update.externalTask;

import java.util.List;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.junit.Assert;
import org.junit.Test;
import static junit.framework.TestCase.assertEquals;
import org.camunda.bpm.qa.rolling.update.AbstractRollingUpdateTestCase;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@ScenarioUnderTest("ProcessWithExternalTaskScenario")
public class CompleteProcessWithExternalTaskTest extends AbstractRollingUpdateTestCase {

  public static final long LOCK_TIME = 5 * 60 * 1000;

  @Test
  @ScenarioUnderTest("init.1")
  public void testCompleteProcessWithExternalTask() {


    //given process with external task
    List<LockedExternalTask> externalTasks = rule.getExternalTaskService().fetchAndLock(1, rule.getBuisnessKey())
      .topic(rule.getTag(), LOCK_TIME)
      .execute();
    assertEquals(1, externalTasks.size());

    //when external task is completed
    rule.getExternalTaskService().complete(externalTasks.get(0).getId(), rule.getBuisnessKey());

    //then process instance is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.fetch.1")
  public void testCompleteProcessWithFetchedExternalTask() {
    //given process with locked external task
    ExternalTask task = rule.getExternalTaskService()
                            .createExternalTaskQuery()
                            .locked()
                            .topicName(rule.getTag())
                            .workerId(rule.getBuisnessKey())
                            .singleResult();
    Assert.assertNotNull(task);

    //when external task is completed
    rule.getExternalTaskService().complete(task.getId(), rule.getBuisnessKey());

    //then no locked external task with worker id exists
    task = rule.getExternalTaskService()
                            .createExternalTaskQuery()
                            .locked()
                            .topicName(rule.getTag())
                            .workerId(rule.getBuisnessKey())
                            .singleResult();
    assertNull(task);
  }
}
