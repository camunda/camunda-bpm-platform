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

import java.util.List;
import java.util.Set;

/**
 * @author: drobisch
 */
public class GroupInfoDto {

  private final List<GroupDto> groups;
  private Set<UserDto> groupUsers;

  public GroupInfoDto() {
    this.groups = null;
    this.groupUsers = null;
  }

  public GroupInfoDto(List<GroupDto> groups, Set<UserDto> groupUsers) {
    this.groupUsers = groupUsers;
    this.groups = groups;
  }

  public Set<UserDto> getGroupUsers() {
    return groupUsers;
  }

  public List<GroupDto> getGroups() {
    return groups;
  }
}
