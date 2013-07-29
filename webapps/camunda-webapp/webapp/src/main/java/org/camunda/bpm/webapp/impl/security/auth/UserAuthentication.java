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
package org.camunda.bpm.webapp.impl.security.auth;

import java.util.List;


/**
 * <p>An authentication for a user</p>
 * 
 * @author Daniel Meyer
 *
 */
public class UserAuthentication extends Authentication {

  private static final long serialVersionUID = 1L;
  
  protected List<String> groupIds;
  
  protected boolean tasklistAuthorized;
  
  protected boolean cockpitAuthorized;

  /**
   * @param userId the id of the user
   * @param groupIds
   * @param processEngineName the name of the process engine
   * @param cockpitAuthorized 
   * @param tasklistAuthorized 
   */
  public UserAuthentication(String userId, List<String> groupIds, String processEngineName, boolean tasklistAuthorized, boolean cockpitAuthorized) {
    super(userId, processEngineName);
    this.groupIds = groupIds;
    this.tasklistAuthorized = tasklistAuthorized;
    this.cockpitAuthorized = cockpitAuthorized;
  }
  
  public List<String> getGroupIds() {
    return groupIds;
  }
  
  public void setGroupIds(List<String> groupIds) {
    this.groupIds = groupIds;
  }

  public boolean isTasklistAuthorized() {
    return tasklistAuthorized;
  }

  public void setTasklistAuthorized(boolean tasklistAuthorized) {
    this.tasklistAuthorized = tasklistAuthorized;
  }

  public boolean isCockpitAuthorized() {
    return cockpitAuthorized;
  }

  public void setCockpitAuthorized(boolean cockpitAuthorized) {
    this.cockpitAuthorized = cockpitAuthorized;
  }
  
  
}
