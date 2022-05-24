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
package org.camunda.bpm.engine.test.api.encoding;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ProcessEngineCharacterEncodingTest {

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected TaskService taskService;
  protected Charset defaultCharset;
  protected List<Task> tasks = new ArrayList<>();

  @Parameter(0)
  public Charset charset;

  @Parameters(name = "{index} - {0}")
  public static Collection<Object[]> scenarios() {
    return Arrays.asList(new Object[][] {
      { StandardCharsets.UTF_8 },
      { StandardCharsets.UTF_16 }
    });
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setDefaultCharset(defaultCharset);
    for (Task task : tasks) {
      taskService.deleteTask(task.getId(), true);
    }
  }

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    taskService = processEngineConfiguration.getTaskService();
    defaultCharset = processEngineConfiguration.getDefaultCharset();
    processEngineConfiguration.setDefaultCharset(charset);
  }

  protected Task newTask() {
    Task task = taskService.newTask();
    tasks.add(task);
    taskService.saveTask(task);
    return task;
  }

  protected Task newTaskWithComment(String message) {
    Task task = newTask();
    taskService.createComment(task.getId(), null, message);
    return task;
  }

  @Test
  public void shouldPreserveArabicTaskCommentMessageWithCharset() {
    // given
    String message = "این نمونه است";
    Task task = newTaskWithComment(message);

    // when
    List<Comment> taskComments = taskService.getTaskComments(task.getId());

    // then
    assertThat(taskComments).hasSize(1);
    assertThat(taskComments.get(0).getFullMessage()).isEqualTo(message);
  }

  @Test
  public void shouldPreserveLatinTaskCommentMessageWithCharset() {
    // given
    String message = "This is an example";
    Task task = newTaskWithComment(message);

    // when
    List<Comment> taskComments = taskService.getTaskComments(task.getId());

    // then
    assertThat(taskComments).hasSize(1);
    assertThat(taskComments.get(0).getFullMessage()).isEqualTo(message);
  }

}
