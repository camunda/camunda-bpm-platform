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
package org.camunda.bpm.spring.boot.starter.security.oauth2.impl;

import org.camunda.bpm.spring.boot.starter.security.oauth2.OAuth2Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class OAuth2GrantedAuthoritiesMapper implements GrantedAuthoritiesMapper {

  private static final Logger logger = LoggerFactory.getLogger(OAuth2GrantedAuthoritiesMapper.class);
  private final OAuth2Properties oAuth2Properties;

  public OAuth2GrantedAuthoritiesMapper(OAuth2Properties oAuth2Properties) {
    this.oAuth2Properties = oAuth2Properties;
  }

  @Override
  public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
    var identityProviderProperties = oAuth2Properties.getIdentityProvider();
    var groupNameAttribute = identityProviderProperties.getGroupNameAttribute();
    Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

    authorities.forEach(authority -> {
      if (authority instanceof OAuth2UserAuthority) {
        var oauth2UserAuthority = (OAuth2UserAuthority) authority;
        Object groupAttribute = oauth2UserAuthority.getAttributes().get(groupNameAttribute);

        if (groupAttribute == null) {
          logger.debug("Attribute {} is not available", groupNameAttribute);
          return;
        }

        if (groupAttribute instanceof Collection) {
          //noinspection unchecked
          Collection<String> groupsAttribute = (Collection<String>) groupAttribute;
          var grantedAuthorities = groupsAttribute.stream()
              .map(SimpleGrantedAuthority::new)
              .collect(Collectors.toSet());
          mappedAuthorities.addAll(grantedAuthorities);
        } else if (groupAttribute instanceof String) {
          String groupNameDelimiter = identityProviderProperties.getGroupNameDelimiter();
          String groupsAttribute = (String) groupAttribute;

          var grantedAuthorities = Arrays.stream(groupsAttribute.split(groupNameDelimiter))
              .map(SimpleGrantedAuthority::new)
              .collect(Collectors.toSet());
          mappedAuthorities.addAll(grantedAuthorities);
        } else {
          logger.error("Could not map granted authorities, unsupported group attribute type: {}", groupAttribute.getClass());
        }
      }
    });

    logger.debug("Authorities mapped from {} to {}", authorities, mappedAuthorities);
    return mappedAuthorities;
  }

}