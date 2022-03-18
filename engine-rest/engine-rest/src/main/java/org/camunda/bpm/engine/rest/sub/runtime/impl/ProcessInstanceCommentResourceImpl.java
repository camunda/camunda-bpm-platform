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
package org.camunda.bpm.engine.rest.sub.runtime.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.rest.dto.task.CommentDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.runtime.ProcessInstanceCommentResource;
import org.camunda.bpm.engine.task.Comment;

public class ProcessInstanceCommentResourceImpl implements ProcessInstanceCommentResource {

  private ProcessEngine engine;
  private String processInstanceId;

  public ProcessInstanceCommentResourceImpl(ProcessEngine engine, String processInstanceId) {
    this.engine = engine;
    this.processInstanceId = processInstanceId;
  }

  public List<CommentDto> getComments() {
    if (!isHistoryEnabled()) {
      return Collections.emptyList();
    }

    ensureProcessInstanceExists(Status.NOT_FOUND);

    List<Comment> processInstanceComments = engine.getTaskService().getProcessInstanceComments(processInstanceId);

    List<CommentDto> comments = new ArrayList<CommentDto>();
    for (Comment comment : processInstanceComments) {
      comments.add(CommentDto.fromComment(comment));
    }

    return comments;
  }

  private boolean isHistoryEnabled() {
    IdentityService identityService = engine.getIdentityService();
    Authentication currentAuthentication = identityService.getCurrentAuthentication();
    try {
      identityService.clearAuthentication();
      int historyLevel = engine.getManagementService().getHistoryLevel();
      return historyLevel > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE;
    } finally {
      identityService.setAuthentication(currentAuthentication);
    }
  }

  private void ensureProcessInstanceExists(Status status) {
    HistoricProcessInstance historicProcessInstance = engine.getHistoryService().createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();
    if (historicProcessInstance == null) {
      throw new InvalidRequestException(status, "No process instance found for id " + processInstanceId);
    }
  }

}