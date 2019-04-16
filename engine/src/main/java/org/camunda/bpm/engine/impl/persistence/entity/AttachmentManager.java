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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;
import org.camunda.bpm.engine.task.Attachment;


/**
 * @author Tom Baeyens
 */
public class AttachmentManager extends AbstractHistoricManager {

  @SuppressWarnings("unchecked")
  public List<Attachment> findAttachmentsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    return getDbEntityManager().selectList("selectAttachmentsByProcessInstanceId", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<Attachment> findAttachmentsByTaskId(String taskId) {
    checkHistoryEnabled();
    return getDbEntityManager().selectList("selectAttachmentsByTaskId", taskId);
  }

  public void addRemovalTimeToAttachmentsByRootProcessInstanceId(String rootProcessInstanceId, Date removalTime) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("rootProcessInstanceId", rootProcessInstanceId);
    parameters.put("removalTime", removalTime);

    getDbEntityManager()
      .updatePreserveOrder(AttachmentEntity.class, "updateAttachmentsByRootProcessInstanceId", parameters);
  }

  public void addRemovalTimeToAttachmentsByProcessInstanceId(String processInstanceId, Date removalTime) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("removalTime", removalTime);

    getDbEntityManager()
      .updatePreserveOrder(AttachmentEntity.class, "updateAttachmentsByProcessInstanceId", parameters);

  }

  @SuppressWarnings("unchecked")
  public void deleteAttachmentsByTaskId(String taskId) {
    checkHistoryEnabled();
    List<AttachmentEntity> attachments = getDbEntityManager().selectList("selectAttachmentsByTaskId", taskId);
    for (AttachmentEntity attachment: attachments) {
      String contentId = attachment.getContentId();
      if (contentId!=null) {
        getByteArrayManager().deleteByteArrayById(contentId);
      }
      getDbEntityManager().delete(attachment);
    }
  }

  public void deleteAttachmentsByProcessInstanceIds(List<String> processInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processInstanceIds", processInstanceIds);
    deleteAttachments(parameters);
  }

  public void deleteAttachmentsByTaskProcessInstanceIds(List<String> processInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("taskProcessInstanceIds", processInstanceIds);
    deleteAttachments(parameters);
  }

  public void deleteAttachmentsByTaskCaseInstanceIds(List<String> caseInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("caseInstanceIds", caseInstanceIds);
    deleteAttachments(parameters);
  }

  protected void deleteAttachments(Map<String, Object> parameters) {
    getDbEntityManager().deletePreserveOrder(ByteArrayEntity.class, "deleteAttachmentByteArraysByIds", parameters);
    getDbEntityManager().deletePreserveOrder(AttachmentEntity.class, "deleteAttachmentByIds", parameters);
  }

  public Attachment findAttachmentByTaskIdAndAttachmentId(String taskId, String attachmentId) {
    checkHistoryEnabled();

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("taskId", taskId);
    parameters.put("id", attachmentId);

    return (AttachmentEntity) getDbEntityManager().selectOne("selectAttachmentByTaskIdAndAttachmentId", parameters);
  }

  public DbOperation deleteAttachmentsByRemovalTime(Date removalTime, int minuteFrom, int minuteTo, int batchSize) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("removalTime", removalTime);
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    parameters.put("batchSize", batchSize);

    return getDbEntityManager()
      .deletePreserveOrder(AttachmentEntity.class, "deleteAttachmentsByRemovalTime",
        new ListQueryParameterObject(parameters, 0, batchSize));
  }

}

