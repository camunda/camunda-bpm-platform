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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;


/**
 * @author Joram Barrez
 * @author Falko Menge
 */
public class GetIdentityLinksForTaskCmd implements Command<List<IdentityLink>>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String taskId;

  public GetIdentityLinksForTaskCmd(String taskId) {
    this.taskId = taskId;
  }

  public List<IdentityLink> execute(CommandContext commandContext) {
    ensureNotNull("taskId", taskId);

    TaskManager taskManager = commandContext.getTaskManager();
    TaskEntity task = taskManager.findTaskById(taskId);
    ensureNotNull("Cannot find task with id " + taskId, "task", task);

    checkGetIdentityLink(task, commandContext);

    List<IdentityLink> identityLinks = task.getIdentityLinks().stream().collect(Collectors.toList());

    // assignee is not part of identity links in the db.
    // so if there is one, we add it here.
    // @Tom: we discussed this long on skype and you agreed ;-)
    // an assignee *is* an identityLink, and so must it be reflected in the API
    //
    // Note: we cant move this code to the TaskEntity (which would be cleaner),
    // since the task.delete cascased to all associated identityLinks
    // and of course this leads to exception while trying to delete a non-existing identityLink
    if (task.getAssignee() != null) {
      IdentityLinkEntity identityLink = new IdentityLinkEntity();
      identityLink.setUserId(task.getAssignee());
      identityLink.setTask(task);
      identityLink.setType(IdentityLinkType.ASSIGNEE);
      identityLinks.add(identityLink);
    }
    if (task.getOwner() != null) {
      IdentityLinkEntity identityLink = new IdentityLinkEntity();
      identityLink.setUserId(task.getOwner());
      identityLink.setTask(task);
      identityLink.setType(IdentityLinkType.OWNER);
      identityLinks.add(identityLink);
    }

    return identityLinks;
  }

  protected void checkGetIdentityLink(TaskEntity task, CommandContext commandContext) {
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadTask(task);
    }
  }
}
