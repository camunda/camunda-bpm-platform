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

}

