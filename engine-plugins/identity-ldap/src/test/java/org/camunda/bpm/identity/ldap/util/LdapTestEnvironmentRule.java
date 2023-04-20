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
package org.camunda.bpm.identity.ldap.util;

import org.junit.rules.ExternalResource;

public class LdapTestEnvironmentRule extends ExternalResource {

  LdapTestEnvironment ldapTestEnvironment;

  int additionalNumberOfUsers = 0;
  int additionnalNumberOfGroups = 0;
  int additionalNumberOfRoles = 0;
  boolean posix = false;

  @Override
  protected void before() throws Exception {
    if(posix) {
      setupPosix();
    } else {
      setupLdap();
    }
  }

  @Override
  protected void after() {
    if (ldapTestEnvironment != null) {
      ldapTestEnvironment.shutdown();
      ldapTestEnvironment = null;
    }
  }

  private void setupLdap() throws Exception {
    ldapTestEnvironment = new LdapTestEnvironment();
    ldapTestEnvironment.init(additionalNumberOfUsers, additionnalNumberOfGroups, additionalNumberOfRoles);
  }

  public void setupPosix() throws Exception {
    ldapTestEnvironment = new LdapPosixTestEnvironment();
    ldapTestEnvironment.init();
  }

  public LdapTestEnvironmentRule additionalNumberOfUsers(int additionalNumberOfUsers) {
    this.additionalNumberOfUsers = additionalNumberOfUsers;
    return this;
  }

  public LdapTestEnvironmentRule additionnalNumberOfGroups(int additionnalNumberOfGroups) {
    this.additionnalNumberOfGroups = additionnalNumberOfGroups;
    return this;
  }

  public LdapTestEnvironmentRule additionalNumberOfRoles(int additionalNumberOfRoles) {
    this.additionalNumberOfRoles = additionalNumberOfRoles;
    return this;
  }

  public LdapTestEnvironmentRule posix(boolean posix) {
    this.posix = posix;
    return this;
  }

  public LdapTestEnvironment getLdapTestEnvironment() {
    return ldapTestEnvironment;
  }
}
