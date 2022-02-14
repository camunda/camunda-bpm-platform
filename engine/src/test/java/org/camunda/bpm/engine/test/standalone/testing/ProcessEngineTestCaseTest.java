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
package org.camunda.bpm.engine.test.standalone.testing;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.Test;


/**
 * @author Joram Barrez
 * @author Falko Menge (camunda)
 */
public class ProcessEngineTestCaseTest extends ProcessEngineTestCase {

  @Deployment
  @Test
  public void testSimpleProcess() {
    runtimeService.startProcessInstanceByKey("simpleProcess");

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());

    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  @Test
  public void testRequiredHistoryLevelAudit() {

    assertThat(currentHistoryLevel()).isIn(ProcessEngineConfiguration.HISTORY_AUDIT, ProcessEngineConfiguration.HISTORY_FULL);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void testRequiredHistoryLevelActivity() {

    assertThat(currentHistoryLevel()).isIn(ProcessEngineConfiguration.HISTORY_ACTIVITY,
        ProcessEngineConfiguration.HISTORY_AUDIT,
        ProcessEngineConfiguration.HISTORY_FULL);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Test
  public void testRequiredHistoryLevelFull() {

    assertThat(currentHistoryLevel()).isEqualTo(ProcessEngineConfiguration.HISTORY_FULL);
  }

  protected String currentHistoryLevel() {
    return processEngine.getProcessEngineConfiguration().getHistory();
  }

}
