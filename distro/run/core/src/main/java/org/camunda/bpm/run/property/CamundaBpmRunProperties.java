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

import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(CamundaBpmRunProperties.PREFIX)
public class CamundaBpmRunProperties {

  public static final String PREFIX = CamundaBpmProperties.PREFIX + ".run";

  @NestedConfigurationProperty
  private CamundaBpmRunAuthenticationProperties auth = new CamundaBpmRunAuthenticationProperties();

  @NestedConfigurationProperty
  private CamundaBpmRunCorsProperty cors = new CamundaBpmRunCorsProperty();

  @NestedConfigurationProperty
  private CamundaBpmRunLdapProperties ldap = new CamundaBpmRunLdapProperties();

  public CamundaBpmRunAuthenticationProperties getAuth() {
    return auth;
  }

  public void setAuth(CamundaBpmRunAuthenticationProperties auth) {
    this.auth = auth;
  }

  public CamundaBpmRunCorsProperty getCors() {
    return cors;
  }

  public void setCors(CamundaBpmRunCorsProperty cors) {
    this.cors = cors;
  }

  public CamundaBpmRunLdapProperties getLdap() {
    return ldap;
  }

  public void setLdap(CamundaBpmRunLdapProperties ldap) {
    this.ldap = ldap;
  }

  @Override
  public String toString() {
    return "CamundaBpmRunProperties [auth=" + auth + ", cors=" + cors + ", ldap=" + ldap + "]";
  }
}
