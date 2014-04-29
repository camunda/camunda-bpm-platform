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
package org.camunda.bpm.engine.rest.dto.task;

import org.camunda.bpm.engine.rest.dto.LinkableDto;
import org.camunda.bpm.engine.task.Attachment;

public class AttachmentDto extends LinkableDto {

  private String id;
  private String name;
  private String description;
  private String taskId;
  private String type;
  private String url;

  public AttachmentDto() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public static AttachmentDto fromAttachment(Attachment attachment) {
    AttachmentDto dto = new AttachmentDto();
    dto.id = attachment.getId();
    dto.name = attachment.getName();
    dto.type = attachment.getType();
    dto.description = attachment.getDescription();
    dto.taskId = attachment.getTaskId();
    dto.url = attachment.getUrl();
    return dto;
  }
}
