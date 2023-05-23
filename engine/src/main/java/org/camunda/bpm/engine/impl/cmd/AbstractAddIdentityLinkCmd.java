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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.IdentityLinkType;

/**
 * Abstract class that modifies {@link AbstractSetTaskPropertyCmd} to customize validation & logging for
 * Add Identity Link related Commands.
 */
public abstract class AbstractAddIdentityLinkCmd extends AbstractSetTaskPropertyCmd<Integer> {

  protected final String userId;
  protected final String groupId;
  protected final String type;

  public AbstractAddIdentityLinkCmd(String taskId, String userId, String groupId, String type) {
    super(taskId, null, true);
    validateParameters(type, userId, groupId);

    this.userId = userId;
    this.groupId = groupId;
    this.type = type;
  }

  @Override
  protected void executeSetOperation(TaskEntity task, Integer value) {

    if (isAssignee(type)) {
      task.setAssignee(userId);
      return;
    }

    if (isOwner(type)) {
      task.setOwner(userId);
      return;
    }

    task.addIdentityLink(userId, groupId, type);
  }

  /**
   * Method to be overridden by concrete identity commands that wish to log an operation.
   *
   * @param context the command context
   * @param task    the task related entity
   */
  protected abstract void logOperation(CommandContext context, TaskEntity task);

  @Override
  protected String getUserOperationLogName() {
    return null; // Ignored for identity commands
  }

  protected void validateParameters(String type, String userId, String groupId) {

    if (isAssignee(type) && groupId != null) {
      throw new BadUserRequestException("Incompatible usage: cannot use ASSIGNEE together with a groupId");
    }

    if (!isAssignee(type) && hasNullIdentity(userId, groupId)) {
      throw new NullValueException("userId and groupId cannot both be null");
    }
  }

  protected boolean hasNullIdentity(String userId, String groupId) {
    return (userId == null) && (groupId == null);
  }

  protected boolean isAssignee(String type) {
    return IdentityLinkType.ASSIGNEE.equals(type);
  }

  protected boolean isOwner(String type) {
    return IdentityLinkType.OWNER.equals(type);
  }

}
