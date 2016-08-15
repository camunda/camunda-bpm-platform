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
package org.camunda.bpm.qa.rolling.upgrade.externalTask;

import java.util.List;
import static junit.framework.TestCase.assertEquals;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@ScenarioUnderTest("ProcessWithExternalTaskScenario")
@Origin("7.5.0")
public class CompleteProcessWithExternalTaskTest {

  public static final String WORKER_ID = "workerId";
  public static final String TOPIC_NAME = "externalTaskTopic";
  public static final long LOCK_TIME = 5 * 60 * 1000;

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testCompleteProcessWithExternalTask() {
    //given process with external task
    List<LockedExternalTask> externalTasks = rule.getExternalTaskService().fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(1, externalTasks.size());

    //when external task is completed
    rule.getExternalTaskService().complete(externalTasks.get(0).getId(), WORKER_ID);

    // then no external task exist anymore
    externalTasks = rule.getExternalTaskService().fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(0, externalTasks.size());

    //and process instance is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.fetch.1")
  public void testCompleteProcessWithFetchedExternalTask() {
    //given process with fetched external task
    ExternalTask task = rule.getExternalTaskService().createExternalTaskQuery().locked().workerId(WORKER_ID).singleResult();
    Assert.assertNotNull(task);

    //when external task is completed
    rule.getExternalTaskService().complete(task.getId(), WORKER_ID);

    //then process instance ends
    rule.assertScenarioEnded();
  }
}
