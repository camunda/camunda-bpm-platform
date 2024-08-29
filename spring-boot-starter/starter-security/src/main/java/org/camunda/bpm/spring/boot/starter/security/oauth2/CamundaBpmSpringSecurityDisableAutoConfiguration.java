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

import org.camunda.bpm.spring.boot.starter.security.oauth2.impl.ClientsNotConfiguredCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Conditional(ClientsNotConfiguredCondition.class)
public class CamundaBpmSpringSecurityDisableAutoConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(CamundaBpmSpringSecurityDisableAutoConfiguration.class);

  @Bean
  public SecurityFilterChain filterChainPermitAll(HttpSecurity http) throws Exception {
    logger.info("Disabling Camunda Spring Security oauth2 integration");

    http.authorizeHttpRequests(customizer -> customizer.anyRequest().permitAll())
        .cors(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }

}
