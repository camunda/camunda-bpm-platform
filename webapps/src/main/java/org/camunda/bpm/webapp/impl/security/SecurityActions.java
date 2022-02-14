/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.webapp.impl.security;

import javax.servlet.ServletException;
import java.io.IOException;
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

  public static <T> T runWithAuthentications(SecurityAction<T> action, Authentications authentications) throws IOException, ServletException {

    List<Authentication> currentAuthentications = authentications.getAuthentications();
    try {
      for (Authentication authentication : currentAuthentications) {
        authenticateProcessEngine(authentication);
      }

      return action.execute();

    } finally {
      for (Authentication authentication : currentAuthentications) {
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

  private static void authenticateProcessEngine(Authentication authentication) {

    ProcessEngine processEngine = Cockpit.getProcessEngine(authentication.getProcessEngineName());
    if (processEngine != null) {

      String userId = authentication.getIdentityId();
      List<String> groupIds = null;
      List<String> tenantIds = null;

      if (authentication instanceof UserAuthentication) {
        UserAuthentication userAuthentication = (UserAuthentication) authentication;
        groupIds = userAuthentication.getGroupIds();
        tenantIds = userAuthentication.getTenantIds();
      }

      processEngine.getIdentityService().setAuthentication(userId, groupIds, tenantIds);
    }
  }

  public static <T> T runWithoutAuthentication(SecurityAction<T> action, ProcessEngine processEngine) throws IOException, ServletException {

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
    public T execute() throws IOException, ServletException;
  }

}
