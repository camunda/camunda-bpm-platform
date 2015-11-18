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

import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.task.IdentityLink;

import javax.ws.rs.core.Response.Status;

public class IdentityLinkDto {

  protected String userId;
  protected String groupId;
  protected String type;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public static IdentityLinkDto fromIdentityLink(IdentityLink identityLink) {
    IdentityLinkDto dto = new IdentityLinkDto();
    dto.userId = identityLink.getUserId();
    dto.groupId = identityLink.getGroupId();
    dto.type = identityLink.getType();

    return dto;
  }

  public void validate() {
    if (userId != null && groupId != null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Identity Link requires userId or groupId, but not both.");
    }

    if (userId == null && groupId == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Identity Link requires userId or groupId.");
    }
  }

}
