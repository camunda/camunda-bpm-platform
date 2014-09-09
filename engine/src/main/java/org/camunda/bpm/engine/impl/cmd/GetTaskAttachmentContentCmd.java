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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;


/**
 * @author kristin.polenz@camunda.com
 */
public class GetTaskAttachmentContentCmd implements Command<InputStream>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String attachmentId;
  protected String taskId;

  public GetTaskAttachmentContentCmd(String taskId, String attachmentId) {
    this.attachmentId = attachmentId;
    this.taskId = taskId;
  }

  public InputStream execute(CommandContext commandContext) {
    AttachmentEntity attachment = (AttachmentEntity) commandContext
        .getAttachmentManager()
        .findAttachmentByTaskIdAndAttachmentId(taskId, attachmentId);

    if (attachment == null) {
      return null;
    }

    String contentId = attachment.getContentId();
    if (contentId==null) {
      return null;
    }

    ByteArrayEntity byteArray = commandContext
        .getDbEntityManager()
        .selectById(ByteArrayEntity.class, contentId);

    byte[] bytes = byteArray.getBytes();

    return new ByteArrayInputStream(bytes);
  }

}
