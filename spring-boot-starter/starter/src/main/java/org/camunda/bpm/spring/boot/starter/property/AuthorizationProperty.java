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
package org.camunda.bpm.spring.boot.starter.property;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class AuthorizationProperty {

  /**
   * Enables authorization.
   */
  private boolean enabled = Defaults.INSTANCE.isAuthorizationEnabled();

  /**
   * Enables authorization for custom code.
   */
  private boolean enabledForCustomCode = Defaults.INSTANCE.isAuthorizationEnabledForCustomCode();

  private String authorizationCheckRevokes = Defaults.INSTANCE.getAuthorizationCheckRevokes();

  /**
   * If the value of this flag is set <code>true</code> then the process engine
   * performs tenant checks to ensure that an authenticated user can only access
   * data that belongs to one of his tenants.
   */
  private boolean tenantCheckEnabled = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabledForCustomCode() {
    return enabledForCustomCode;
  }

  public void setEnabledForCustomCode(boolean enabledForCustomCode) {
    this.enabledForCustomCode = enabledForCustomCode;
  }

  public String getAuthorizationCheckRevokes() {
    return authorizationCheckRevokes;
  }

  public void setAuthorizationCheckRevokes(String authorizationCheckRevokes) {
    this.authorizationCheckRevokes = authorizationCheckRevokes;
  }

  public boolean isTenantCheckEnabled() {
    return tenantCheckEnabled;
  }

  public void setTenantCheckEnabled(boolean tenantCheckEnabled) {
    this.tenantCheckEnabled = tenantCheckEnabled;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("enabled=" + enabled)
      .add("enabledForCustomCode=" + enabledForCustomCode)
      .add("authorizationCheckRevokes=" + authorizationCheckRevokes)
      .add("tenantCheckEnabled=" + tenantCheckEnabled)
      .toString();
  }

}
