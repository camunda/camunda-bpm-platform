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
package org.camunda.bpm.engine.impl.cmd;

import java.util.Map;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;


/**
 * @author Tom Baeyens
 */
public class ResolveTaskCmd extends CompleteTaskCmd {

  private static final long serialVersionUID = 1L;

  public ResolveTaskCmd(String taskId, Map<String, Object> variables) {
    super(taskId, variables, false, false);
  }

  @Override
  protected void completeTask(TaskEntity task) {
    task.resolve();
    task.triggerUpdateEvent();
    task.logUserOperation(UserOperationLogEntry.OPERATION_TYPE_RESOLVE);
  }
}
