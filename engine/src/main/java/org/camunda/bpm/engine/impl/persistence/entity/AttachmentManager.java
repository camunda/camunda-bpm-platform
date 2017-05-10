/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
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
    CommandContext commandContext = Context.getCommandContext();
    commandContext
        .getDbEntityManager().deletePreserveOrder(ByteArrayEntity.class, "deleteAttachmentByteArraysByProcessInstanceIds", processInstanceIds);
    commandContext
        .getDbEntityManager().deletePreserveOrder(AttachmentEntity.class, "deleteAttachmentByProcessInstanceIds", processInstanceIds);
  }

  public void deleteAttachmentsByTaskProcessInstanceIds(List<String> processInstanceIds) {
    CommandContext commandContext = Context.getCommandContext();

    commandContext
        .getDbEntityManager().deletePreserveOrder(ByteArrayEntity.class, "deleteAttachmentByteArraysByTaskProcessInstanceIds", processInstanceIds);
    commandContext
        .getDbEntityManager().deletePreserveOrder(AttachmentEntity.class, "deleteAttachmentByTaskProcessInstanceIds", processInstanceIds);
  }

  public void deleteAttachmentsByTaskCaseInstanceIds(List<String> caseInstanceIds) {
    DbEntityManager entityManager = Context.getCommandContext().getDbEntityManager();

    entityManager.deletePreserveOrder(ByteArrayEntity.class, "deleteAttachmentByteArraysByTaskCaseInstanceIds", caseInstanceIds);
    entityManager.deletePreserveOrder(AttachmentEntity.class, "deleteAttachmentByTaskCaseInstanceIds", caseInstanceIds);
  }

  public Attachment findAttachmentByTaskIdAndAttachmentId(String taskId, String attachmentId) {
    checkHistoryEnabled();

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("taskId", taskId);
    parameters.put("id", attachmentId);

    return (AttachmentEntity) getDbEntityManager().selectOne("selectAttachmentByTaskIdAndAttachmentId", parameters);
  }

}

