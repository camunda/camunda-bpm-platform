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
package org.camunda.bpm.run.test.config.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin;
import org.camunda.bpm.run.CamundaBpmRun;
import org.camunda.bpm.run.property.CamundaBpmRunLdapProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { CamundaBpmRun.class })
@ActiveProfiles(profiles = { "test-auth-disabled" , "test-ldap-enabled" })
public class LdapConfigurationTest {

  @Autowired
  CamundaBpmRunLdapProperties props;

  @Autowired
  LdapIdentityProviderPlugin plugin;

  @Test
  public void shouldPickUpConfiguration() {
    assertThat(props.isEnabled()).isEqualTo(true);
    assertThat(props.getServerUrl()).isEqualTo(plugin.getServerUrl());
    assertThat(props.getManagerDn()).isEqualTo(plugin.getManagerDn());
    assertThat(props.getManagerPassword()).isEqualTo(plugin.getManagerPassword());
    assertThat(props.getBaseDn()).isEqualTo(plugin.getBaseDn());
    assertThat(props.getUserSearchBase()).isEqualTo(plugin.getUserSearchBase());
    assertThat(props.getUserIdAttribute()).isEqualTo(plugin.getUserIdAttribute());
    assertThat(props.getUserFirstnameAttribute()).isEqualTo(plugin.getUserFirstnameAttribute());
    assertThat(props.getUserLastnameAttribute()).isEqualTo(plugin.getUserLastnameAttribute());
    assertThat(props.getUserEmailAttribute()).isEqualTo(plugin.getUserEmailAttribute());
    assertThat(props.getUserPasswordAttribute()).isEqualTo(plugin.getUserPasswordAttribute());
    assertThat(props.getGroupSearchBase()).isEqualTo(plugin.getGroupSearchBase());
    assertThat(props.getGroupSearchFilter()).isEqualTo(plugin.getGroupSearchFilter());
    assertThat(props.getGroupIdAttribute()).isEqualTo(plugin.getGroupIdAttribute());
    assertThat(props.getGroupNameAttribute()).isEqualTo(plugin.getGroupNameAttribute());
    assertThat(props.getGroupTypeAttribute()).isEqualTo(plugin.getGroupTypeAttribute());
    assertThat(props.getGroupMemberAttribute()).isEqualTo(plugin.getGroupMemberAttribute());
    assertThat(props.isSortControlSupported()).isEqualTo(plugin.isSortControlSupported());
    assertThat(props.isUseSsl()).isEqualTo(plugin.isUseSsl());
    assertThat(props.isUsePosixGroups()).isEqualTo(plugin.isUsePosixGroups());
    assertThat(props.isAllowAnonymousLogin()).isEqualTo(plugin.isAllowAnonymousLogin());
    assertThat(props.isAuthorizationCheckEnabled()).isEqualTo(plugin.isAuthorizationCheckEnabled());
    assertThat(props.isAcceptUntrustedCertificates()).isEqualTo(plugin.isAcceptUntrustedCertificates());
    assertThat(props.getInitialContextFactory()).isEqualTo(plugin.getInitialContextFactory());
    assertThat(props.getSecurityAuthentication()).isEqualTo(plugin.getSecurityAuthentication());
  }

  @Test
  public void shouldNotIncludeSensitiveConnectionPropertiesInToString() {
    assertThat(props.toString()).doesNotContain(
        "http://foo.bar",
        "managerdn",
        "managerpw");
  }
}