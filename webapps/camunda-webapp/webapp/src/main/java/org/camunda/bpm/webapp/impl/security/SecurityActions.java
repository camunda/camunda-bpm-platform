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
package org.camunda.bpm.webapp.impl.security;

import java.util.List;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.webapp.impl.security.auth.Authentication;
import org.camunda.bpm.webapp.impl.security.auth.Authentications;
import org.camunda.bpm.webapp.impl.security.auth.UserAuthentication;

/**
 * @author Daniel Meyer
 *
 */
public class SecurityActions {
  
  public static <T> T runWithAuthentications(SecurityAction<T> action, Authentications authentications) {
        
    try {
      for (Authentication authentication : authentications.getAuthentications()) {
        authenticateProcessEngine(authentication);
      }    
      
      return action.execute();
      
    } finally {
      for (Authentication authentication : authentications.getAuthentications()) {
        clearAuthentication(authentication);
      }
    }
  }

  private static void clearAuthentication(Authentication authentication) {
    ProcessEngine processEngine = Cockpit.getProcessEngine(authentication.getProcessEngineName());
    if(processEngine != null) {
      processEngine.getIdentityService().clearAuthentication();
    }
  }

  private static void authenticateProcessEngine(org.camunda.bpm.webapp.impl.security.auth.Authentication authentication) {
    
    ProcessEngine processEngine = Cockpit.getProcessEngine(authentication.getProcessEngineName());    
    if(processEngine != null) {
      
      String userId = authentication.getIdentityId();
      List<String> groupIds = null;
      if (authentication instanceof UserAuthentication) {
        groupIds = ((UserAuthentication) authentication).getGroupIds();          
      }
      
      processEngine.getIdentityService().setAuthentication(userId, groupIds);        
    }
  }

  public static <T> T runWithoutAuthentication(SecurityAction<T> action, ProcessEngine processEngine) {
    
    final IdentityService identityService = processEngine.getIdentityService();
    org.camunda.bpm.engine.impl.identity.Authentication currentAuth = identityService.getCurrentAuthentication();
    
    try {
      identityService.clearAuthentication();      
      return action.execute();
      
    } finally {
      identityService.setAuthentication(currentAuth);
      
    }
    
  }
  
  public static interface SecurityAction<T> {
    public T execute();
  }

}
