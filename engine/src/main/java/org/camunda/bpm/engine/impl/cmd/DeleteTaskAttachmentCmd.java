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

package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author kristin.polenz@camunda.com
 */
public class DeleteTaskAttachmentCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String attachmentId;
  protected String taskId;

  public DeleteTaskAttachmentCmd(String taskId, String attachmentId) {
    this.attachmentId = attachmentId;
    this.taskId = taskId;
  }

  public Object execute(CommandContext commandContext) {
    AttachmentEntity attachment = (AttachmentEntity) commandContext
      .getAttachmentManager()
      .findAttachmentByTaskIdAndAttachmentId(taskId, attachmentId);

    ensureNotNull("No attachment exist for task id '" + taskId + " and attachmentId '" + attachmentId + "'.", "attachment", attachment);

    commandContext
      .getDbEntityManager()
      .delete(attachment);

    if (attachment.getContentId() != null) {
      commandContext
        .getByteArrayManager()
        .deleteByteArrayById(attachment.getContentId());
    }

    if (attachment.getTaskId() != null) {
      TaskEntity task = commandContext
        .getTaskManager()
        .findTaskById(attachment.getTaskId());

      PropertyChange propertyChange = new PropertyChange("name", null, attachment.getName());

      commandContext.getOperationLogManager()
        .logAttachmentOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE_ATTACHMENT, task, propertyChange);
    }

    return null;
  }

}
