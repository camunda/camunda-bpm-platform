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

package org.camunda.bpm.engine.test.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit 4 Rule that performs resources cleanup for methods that require post-method execution cleanup.
 * Currently, the rule supports only clean up of {@link Task}s but the rule can be extended for other resources that
 * might pollute sequential execution of other test methods.
 */
public class TaskCleanupRule extends TestWatcher {

  private static final Logger LOG = LoggerFactory.getLogger(TaskCleanupRule.class);

  private final ProcessEngine engine;

  public TaskCleanupRule(ProcessEngineTestRule engine) {
    this.engine = engine.processEngineRule.getProcessEngine();
  }

  @Override
  public Statement apply(Statement base, Description description) {
    boolean methodHasTaskCleanupAnnotation = getAnnotation(description, CleanupTask.class) != null;

    try {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          base.evaluate();

          if (methodHasTaskCleanupAnnotation) {
            deleteAllTasks();
          }
        }
      };
    } finally {
      LOG.debug("deleteTasks: {}", methodHasTaskCleanupAnnotation);
    }

  }

  private void deleteAllTasks() {
    try {
      TaskService taskService = engine.getTaskService();
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        LOG.debug("deleteTask with taskId: {}", task.getId());
        taskService.deleteTask(task.getId(), true);
      }
    } catch (Exception e) {
      throw new TaskCleanupRuleException(e);
    }
  }

  private <T extends Annotation> T getAnnotation(Description description, Class<T> annotation) {
    try {
      Class<?> testClass = description.getTestClass();
      String methodName = description.getMethodName();
      String methodWithoutParamsName = methodName.split("\\[")[0];

      Method method = testClass.getMethod(methodWithoutParamsName);
      return method.getAnnotation(annotation);
    } catch (NoSuchMethodException e) {
      throw new TaskCleanupRuleException(e);
    }
  }

  static class TaskCleanupRuleException extends RuntimeException {
    public TaskCleanupRuleException(Exception e) {
      super(e);
    }
  }

}