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
package org.camunda.bpm.spring.boot.starter;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.camunda.bpm.spring.boot.starter.test.nonpa.TestEventCaptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import jakarta.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = {TestApplication.class},
  webEnvironment = WebEnvironment.NONE
)
@ActiveProfiles("noeventing")
@Transactional
public class CamundaEventingDisabledIT extends AbstractCamundaAutoConfigurationIT {

  @Autowired
  private RuntimeService runtime;

  @Autowired
  private TaskService taskService;

  @Autowired
  private TestEventCaptor eventCaptor;

  private ProcessInstance instance;

  @Before
  public void init() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("eventing")
      .singleResult();
    assertThat(processDefinition).isNotNull();

    eventCaptor.historyEvents.clear();
    instance = runtime.startProcessInstanceByKey("eventing");
  }

  @After
  public void stop() {
    if (instance != null) {
      // update stale instance
      instance = runtime.createProcessInstanceQuery().processInstanceId(instance.getProcessInstanceId()).active().singleResult();
      if (instance != null) {
        runtime.deleteProcessInstance(instance.getProcessInstanceId(), "eventing shutdown");
      }
    }
  }

  @Test
  public final void shouldEventTaskCreation() {

    Task task = taskService.createTaskQuery().active().singleResult();
    taskService.complete(task.getId());

    assertThat(eventCaptor.taskEvents).isEmpty();
    assertThat(eventCaptor.executionEvents).isEmpty();
    assertThat(eventCaptor.historyEvents).isEmpty();

  }

}
