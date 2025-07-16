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
package org.camunda.bpm.engine.test.bpmn.tasklistener.util;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.task.IdentityLinkType;

public class TaskCreateListenerAddAndDelete  implements TaskListener {

  @Override
  public void notify(DelegateTask delegateTask) {
    delegateTask.setDescription("TaskCreateListener is listening!");

    var taskService = delegateTask.getProcessEngineServices().getTaskService();
    taskService.addCandidateUser(delegateTask.getId(), "aCandidateUserId");
    taskService.deleteCandidateUser(delegateTask.getId(), "aCandidateUserId");

    taskService.addCandidateGroup(delegateTask.getId(), "aCandidateGroupId");
    taskService.deleteCandidateGroup(delegateTask.getId(), "aCandidateGroupId");

    taskService.addUserIdentityLink(delegateTask.getId(), "anAssigneeUserId", IdentityLinkType.ASSIGNEE);
    taskService.deleteUserIdentityLink(delegateTask.getId(), "anAssigneeUserId", IdentityLinkType.ASSIGNEE);

    taskService.addGroupIdentityLink(delegateTask.getId(), "anotherCandidateGroupId", IdentityLinkType.CANDIDATE);
    taskService.deleteGroupIdentityLink(delegateTask.getId(), "anotherCandidateGroupId", IdentityLinkType.CANDIDATE);

    var runTimeService = delegateTask.getProcessEngineServices().getRuntimeService();
    runTimeService.setVariable(delegateTask.getExecutionId(), "aVarName", "aVarValue");
    runTimeService.removeVariable(delegateTask.getExecutionId(), "aVarName");

  }
}
