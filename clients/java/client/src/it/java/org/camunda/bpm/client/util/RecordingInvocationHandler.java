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
package org.camunda.bpm.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;

public class RecordingInvocationHandler implements ExternalTaskHandler {

  protected List<RecordedInvocation> handledInvocations = Collections.synchronizedList(new ArrayList<>());
  protected int nextTaskHandler = 0;
  protected final ExternalTaskHandler[] taskHandlers;

  public RecordingInvocationHandler() {
      this((c, t) -> {
        // do nothing
      });
    }

  public RecordingInvocationHandler(ExternalTaskHandler... taskHandlers) {
      this.taskHandlers = taskHandlers;
    }

  @Override
  public void execute(ExternalTask task, ExternalTaskService externalTaskService) {
    final ExternalTaskHandler handler = taskHandlers[nextTaskHandler];
    nextTaskHandler = Math.min(nextTaskHandler + 1, taskHandlers.length - 1);

    try {
      handler.execute(task, externalTaskService);
    } finally {
      handledInvocations.add(new RecordedInvocation(task, externalTaskService));
    }
  }

  public List<RecordedInvocation> getInvocations() {
    return handledInvocations;
  }

  public int numInvocations() {
    return handledInvocations.size();
  }

  public void clear() {
    handledInvocations.clear();
  }

  public static class RecordedInvocation {

    private ExternalTaskService externalTaskService;
    private ExternalTask externalTask;

    public RecordedInvocation(ExternalTask externalTask, ExternalTaskService externalTaskService) {
      this.externalTask = externalTask;
      this.externalTaskService = externalTaskService;
    }

    public ExternalTask getExternalTask() {
      return externalTask;
    }

    public ExternalTaskService getExternalTaskService() {
      return externalTaskService;
    }

  }
}
