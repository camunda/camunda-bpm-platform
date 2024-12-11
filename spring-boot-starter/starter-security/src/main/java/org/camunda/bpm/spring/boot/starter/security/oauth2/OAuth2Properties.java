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
package org.camunda.bpm.spring.boot.starter.security.oauth2;

import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.security.oauth2.impl.OAuth2IdentityProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(OAuth2Properties.PREFIX)
public class OAuth2Properties {

  public static final String PREFIX = CamundaBpmProperties.PREFIX + ".oauth2";

  /**
   * OAuth2 SSO logout properties.
   */
  @NestedConfigurationProperty
  private OAuth2SSOLogoutProperties ssoLogout = new OAuth2SSOLogoutProperties();

  /**
   * OAuth2 identity provider properties.
   */
  @NestedConfigurationProperty
  private OAuth2IdentityProviderProperties identityProvider = new OAuth2IdentityProviderProperties();

  public static class OAuth2SSOLogoutProperties {
    /**
     * Enable SSO Logout. Default {@code false}.
     */
    private boolean enabled = false;

    /**
     * URI the user is redirected after SSO logout from the provider. Default {@code {baseUrl}}.
     */
    private String postLogoutRedirectUri = "{baseUrl}";

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getPostLogoutRedirectUri() {
      return postLogoutRedirectUri;
    }

    public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
      this.postLogoutRedirectUri = postLogoutRedirectUri;
    }
  }

  public static class OAuth2IdentityProviderProperties {
    /**
     * Enable {@link OAuth2IdentityProvider}. Default {@code true}.
     */
    private boolean enabled = true;

    /**
     * Name of the attribute (claim) that holds the groups.
     */
    private String groupNameAttribute;

    /**
     * Group name attribute delimiter. Only used if the {@link #groupNameAttribute} is a {@link String}.
     */
    private String groupNameDelimiter = ",";

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getGroupNameAttribute() {
      return groupNameAttribute;
    }

    public void setGroupNameAttribute(String groupNameAttribute) {
      this.groupNameAttribute = groupNameAttribute;
    }

    public String getGroupNameDelimiter() {
      return groupNameDelimiter;
    }

    public void setGroupNameDelimiter(String groupNameDelimiter) {
      this.groupNameDelimiter = groupNameDelimiter;
    }
  }

  public OAuth2SSOLogoutProperties getSsoLogout() {
    return ssoLogout;
  }

  public void setSsoLogout(OAuth2SSOLogoutProperties ssoLogout) {
    this.ssoLogout = ssoLogout;
  }

  public OAuth2IdentityProviderProperties getIdentityProvider() {
    return identityProvider;
  }

  public void setIdentityProvider(OAuth2IdentityProviderProperties identityProvider) {
    this.identityProvider = identityProvider;
  }
}