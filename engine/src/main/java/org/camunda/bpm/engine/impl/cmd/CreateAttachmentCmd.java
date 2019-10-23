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

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.ResourceTypes;
import org.camunda.bpm.engine.task.Attachment;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tom Baeyens
 */
// Not Serializable
public class CreateAttachmentCmd implements Command<Attachment> {

  protected String taskId;
  protected String attachmentType;
  protected String processInstanceId;
  protected String attachmentName;
  protected String attachmentDescription;
  protected InputStream content;
  protected String url;
  private TaskEntity task;
  protected ExecutionEntity processInstance;

  public CreateAttachmentCmd(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content, String url) {
    this.attachmentType = attachmentType;
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    this.attachmentName = attachmentName;
    this.attachmentDescription = attachmentDescription;
    this.content = content;
    this.url = url;
  }

  @Override
  public Attachment execute(CommandContext commandContext) {
    if (taskId != null) {
      task = commandContext
          .getTaskManager()
          .findTaskById(taskId);
    } else {
      ensureNotNull("taskId or processInstanceId has to be provided", this.processInstanceId);
      List<ExecutionEntity> executionsByProcessInstanceId = commandContext.getExecutionManager().findExecutionsByProcessInstanceId(processInstanceId);
      processInstance = executionsByProcessInstanceId.get(0);
    }

    AttachmentEntity attachment = new AttachmentEntity();
    attachment.setName(attachmentName);
    attachment.setDescription(attachmentDescription);
    attachment.setType(attachmentType);
    attachment.setTaskId(taskId);
    attachment.setProcessInstanceId(processInstanceId);
    attachment.setUrl(url);
    attachment.setCreateTime(ClockUtil.getCurrentTime());

    if (task != null) {
      ExecutionEntity execution = task.getExecution();
      if (execution != null) {
        attachment.setRootProcessInstanceId(execution.getRootProcessInstanceId());
      }
    } else if (processInstance != null) {
      attachment.setRootProcessInstanceId(processInstance.getRootProcessInstanceId());
    }

    if (isHistoryRemovalTimeStrategyStart()) {
      provideRemovalTime(attachment);
    }

    DbEntityManager dbEntityManger = commandContext.getDbEntityManager();
    dbEntityManger.insert(attachment);

    if (content != null) {
      byte[] bytes = IoUtil.readInputStream(content, attachmentName);
      ByteArrayEntity byteArray = new ByteArrayEntity(bytes, ResourceTypes.HISTORY);

      byteArray.setRootProcessInstanceId(attachment.getRootProcessInstanceId());
      byteArray.setRemovalTime(attachment.getRemovalTime());

      commandContext.getByteArrayManager().insertByteArray(byteArray);
      attachment.setContentId(byteArray.getId());
    }

    PropertyChange propertyChange = new PropertyChange("name", null, attachmentName);

    if (task != null) {
      commandContext.getOperationLogManager()
          .logAttachmentOperation(UserOperationLogEntry.OPERATION_TYPE_ADD_ATTACHMENT, task, propertyChange);
      task.triggerUpdateEvent();
    } else if (processInstance != null) {
      commandContext.getOperationLogManager()
          .logAttachmentOperation(UserOperationLogEntry.OPERATION_TYPE_ADD_ATTACHMENT, processInstance, propertyChange);
    }

    return attachment;
  }

  protected boolean isHistoryRemovalTimeStrategyStart() {
    return HISTORY_REMOVAL_TIME_STRATEGY_START.equals(getHistoryRemovalTimeStrategy());
  }

  protected String getHistoryRemovalTimeStrategy() {
    return Context.getProcessEngineConfiguration()
      .getHistoryRemovalTimeStrategy();
  }

  protected HistoricProcessInstanceEventEntity getHistoricRootProcessInstance(String rootProcessInstanceId) {
    return Context.getCommandContext().getDbEntityManager()
      .selectById(HistoricProcessInstanceEventEntity.class, rootProcessInstanceId);
  }

  protected void provideRemovalTime(AttachmentEntity attachment) {
    String rootProcessInstanceId = attachment.getRootProcessInstanceId();
    if (rootProcessInstanceId != null) {
      HistoricProcessInstanceEventEntity historicRootProcessInstance =
        getHistoricRootProcessInstance(rootProcessInstanceId);

      if (historicRootProcessInstance != null) {
        Date removalTime = historicRootProcessInstance.getRemovalTime();
        attachment.setRemovalTime(removalTime);
      }
    }
  }

}
