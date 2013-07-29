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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;

/**
 * @author Daniel Meyer
 *
 */
public class SaveAuthorizationCmd implements Command<Authorization> {
  
  protected AuthorizationEntity authorization;

  public SaveAuthorizationCmd(Authorization authorization) {
    this.authorization = (AuthorizationEntity) authorization;
    validate();
  }

  protected void validate() {
    if(authorization.getUserId() == null &&
        authorization.getGroupId() == null) {
      throw new ProcessEngineException("Authorization must either have a 'userId' or a 'groupId'.");
    }
    if(authorization.getUserId() != null && authorization.getGroupId() != null) {
      throw new ProcessEngineException("Authorization cannot define 'userId' or a 'groupId' at the same time.");
    }
    if(authorization.getResource() == null) {
      throw new ProcessEngineException("Authorization 'resourceType' cannot be null.");
    }
  }
  
  public Authorization execute(CommandContext commandContext) {
    
    final AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    
    if(authorization.getId() == null) {
      authorizationManager.insert(authorization);
      
    } else {
      authorizationManager.update(authorization);
      
    }
    
    return authorization;
  }

}
