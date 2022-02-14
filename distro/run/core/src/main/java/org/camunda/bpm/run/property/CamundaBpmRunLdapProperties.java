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
package org.camunda.bpm.run.property;

import org.camunda.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin;

public class CamundaBpmRunLdapProperties extends LdapIdentityProviderPlugin {

  public static final String PREFIX = CamundaBpmRunProperties.PREFIX + ".ldap";

  boolean enabled = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return "CamundaBpmRunLdapProperty [enabled=" + enabled +
        ", initialContextFactory=" + initialContextFactory +
        ", securityAuthentication=" + securityAuthentication +
        ", contextProperties=" + contextProperties +
        ", serverUrl=******" + // sensitive for logging
        ", managerDn=******" + // sensitive for logging
        ", managerPassword=******" + // sensitive for logging
        ", baseDn=" + baseDn +
        ", userDnPattern=" + userDnPattern +
        ", userSearchBase=" + userSearchBase +
        ", userSearchFilter=" + userSearchFilter +
        ", groupSearchBase=" + groupSearchBase +
        ", groupSearchFilter=" + groupSearchFilter +
        ", userIdAttribute=" + userIdAttribute +
        ", userFirstnameAttribute=" + userFirstnameAttribute +
        ", userLastnameAttribute=" + userLastnameAttribute +
        ", userEmailAttribute=" + userEmailAttribute +
        ", userPasswordAttribute=" + userPasswordAttribute +
        ", groupIdAttribute=" + groupIdAttribute +
        ", groupNameAttribute=" + groupNameAttribute +
        ", groupTypeAttribute=" + groupTypeAttribute +
        ", groupMemberAttribute=" + groupMemberAttribute +
        ", sortControlSupported=" + sortControlSupported +
        ", useSsl=" + useSsl +
        ", usePosixGroups=" + usePosixGroups +
        ", allowAnonymousLogin=" + allowAnonymousLogin +
        ", authorizationCheckEnabled=" + authorizationCheckEnabled + "]";
  }
}
