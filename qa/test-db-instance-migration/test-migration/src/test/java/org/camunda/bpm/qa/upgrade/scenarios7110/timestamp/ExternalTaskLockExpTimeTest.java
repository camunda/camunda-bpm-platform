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
package org.camunda.bpm.qa.upgrade.scenarios7110.timestamp;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Nikola Koevski
 */
@ScenarioUnderTest("ExternalTaskLockExpTimeScenario")
@Origin("7.11.0")
public class ExternalTaskLockExpTimeTest extends AbstractTimestampMigrationTest {

  protected static final Date LOCK_EXP_TIME =  new Date(TIME + 1000L);
  protected static final String TOPIC_NAME = "externalTaskLockExpTimeTestTopic";

  @ScenarioUnderTest("initExternalTaskLockExpTime.1")
  @Test
  public void testLockExpirationTimeConversion() {

    // when
    ExternalTask externalTask = externalTaskService.createExternalTaskQuery()
      .topicName(TOPIC_NAME)
      .singleResult();

    // assume
    assertNotNull(externalTask);

    // then
    assertThat(externalTask.getLockExpirationTime(), is(LOCK_EXP_TIME));
  }
}