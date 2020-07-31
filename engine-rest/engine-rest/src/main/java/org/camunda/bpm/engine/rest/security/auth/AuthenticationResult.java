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
package org.camunda.bpm.engine.rest.security.auth;

import java.util.List;

import org.camunda.bpm.engine.IdentityService;

/**
 * Serves as DTO to hold the result of an authentication request performed
 * through an {@link AuthenticationProvider}.
 * 
 * Note that when implementing a custom {@link AuthenticationProvider}, it is
 * not required to set groups or tenants for an AuthenticationResult, as they
 * will be resolved later via the {@link IdentityService} (e.g.
 * {@link ProcessEngineAuthenticationFilter#setAuthenticatedUser}).
 */
public class AuthenticationResult {

  protected boolean isAuthenticated;

  protected String authenticatedUser;
  protected List<String> groups;
  protected List<String> tenants;

  public AuthenticationResult(String authenticatedUser, boolean isAuthenticated) {
    this.authenticatedUser = authenticatedUser;
    this.isAuthenticated = isAuthenticated;
  }

  public String getAuthenticatedUser() {
    return authenticatedUser;
  }

  public void setAuthenticatedUser(String authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
  }

  public boolean isAuthenticated() {
    return isAuthenticated;
  }

  public void setAuthenticated(boolean isAuthenticated) {
    this.isAuthenticated = isAuthenticated;
  }

  public List<String> getGroups() {
    return groups;
  }

  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  public List<String> getTenants() {
    return tenants;
  }

  public void setTenants(List<String> tenants) {
    this.tenants = tenants;
  }

  public static AuthenticationResult successful(String userId) {
    return new AuthenticationResult(userId, true);
  }

  public static AuthenticationResult unsuccessful() {
    return new AuthenticationResult(null, false);
  }

  public static AuthenticationResult unsuccessful(String userId) {
    return new AuthenticationResult(userId, false);
  }
}
