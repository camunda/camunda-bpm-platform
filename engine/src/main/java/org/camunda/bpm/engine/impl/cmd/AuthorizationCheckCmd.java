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

import java.util.List;

import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;

/**
 * <p>Command allowing to perform an authorization check</p>
 * 
 * @author Daniel Meyer
 *
 */
public class AuthorizationCheckCmd implements Command<Boolean> {

  protected String userId;
  protected List<String> groupIds;
  protected Permission permission;
  protected Resource resource;
  protected String resourceId;

  public AuthorizationCheckCmd(String userId, List<String> groupIds, Permission permission, Resource resource, String resourceId) {
    this.userId = userId;
    this.groupIds = groupIds;
    this.permission = permission;
    this.resource = resource;
    this.resourceId = resourceId;
  }

  public Boolean execute(CommandContext commandContext) {
    final AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();    
    return authorizationManager.isAuthorized(userId, groupIds, permission, resource, resourceId);
  }

}
