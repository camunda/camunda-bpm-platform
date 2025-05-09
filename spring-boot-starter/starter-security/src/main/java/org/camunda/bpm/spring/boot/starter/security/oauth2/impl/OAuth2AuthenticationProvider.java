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

import jakarta.servlet.http.HttpServletRequest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;
import org.camunda.bpm.engine.rest.security.auth.impl.ContainerBasedAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public class OAuth2AuthenticationProvider extends ContainerBasedAuthenticationProvider {

  private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationProvider.class);

  @Override
  public AuthenticationResult extractAuthenticatedUser(HttpServletRequest request, ProcessEngine engine) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      logger.debug("Authentication is null");
      return AuthenticationResult.unsuccessful();
    }

    if (!(authentication instanceof OAuth2AuthenticationToken)) {
      logger.debug("Authentication is not OAuth2, it is {}", authentication.getClass());
      return AuthenticationResult.unsuccessful();
    }
    var oauth2 = (OAuth2AuthenticationToken) authentication;
    String camundaUserId = oauth2.getName();
    if (camundaUserId == null || camundaUserId.isEmpty()) {
      logger.debug("UserId is empty");
      return AuthenticationResult.unsuccessful();
    }

    logger.debug("Authenticated user '{}'", camundaUserId);
    return AuthenticationResult.successful(camundaUserId);
  }
}
