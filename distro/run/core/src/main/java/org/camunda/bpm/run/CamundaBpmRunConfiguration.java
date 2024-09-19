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
package org.camunda.bpm.run;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.plugin.AdministratorAuthorizationPlugin;
import org.camunda.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin;
import org.camunda.bpm.run.property.CamundaBpmRunAdministratorAuthorizationProperties;
import org.camunda.bpm.run.property.CamundaBpmRunLdapProperties;
import org.camunda.bpm.run.property.CamundaBpmRunProperties;
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@EnableConfigurationProperties(CamundaBpmRunProperties.class)
@Configuration
@AutoConfigureAfter({ CamundaBpmAutoConfiguration.class })
public class CamundaBpmRunConfiguration {

  @Bean
  @ConditionalOnProperty(name = "enabled", havingValue = "true", prefix = CamundaBpmRunLdapProperties.PREFIX)
  public LdapIdentityProviderPlugin ldapIdentityProviderPlugin(CamundaBpmRunProperties properties) {
    return properties.getLdap();
  }

  @Bean
  @ConditionalOnProperty(name = "enabled", havingValue = "true", prefix = CamundaBpmRunAdministratorAuthorizationProperties.PREFIX)
  public AdministratorAuthorizationPlugin administratorAuthorizationPlugin(CamundaBpmRunProperties properties) {
    return properties.getAdminAuth();
  }

  @Bean
  public ProcessEngineConfigurationImpl processEngineConfigurationImpl(List<ProcessEnginePlugin> processEnginePluginsFromContext,
                                                                       CamundaBpmRunProperties properties,
                                                                       CamundaBpmRunDeploymentConfiguration deploymentConfig) {
    String normalizedDeploymentDir = deploymentConfig.getNormalizedDeploymentDir();
    boolean deployChangedOnly = properties.getDeployment().isDeployChangedOnly();
    var processEnginePluginsFromYaml = properties.getProcessEnginePlugins();

    return new CamundaBpmRunProcessEngineConfiguration(normalizedDeploymentDir, deployChangedOnly,
        processEnginePluginsFromContext, processEnginePluginsFromYaml);
  }

  @Bean
  public CamundaBpmRunDeploymentConfiguration camundaDeploymentConfiguration(@Value("${camunda.deploymentDir:#{null}}") String deploymentDir) {
    return new CamundaBpmRunDeploymentConfiguration(deploymentDir);
  }

}
