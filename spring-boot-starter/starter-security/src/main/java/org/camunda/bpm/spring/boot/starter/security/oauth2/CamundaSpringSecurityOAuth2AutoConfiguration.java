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

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.camunda.bpm.engine.spring.SpringProcessEngineServicesConfiguration;
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.property.WebappProperty;
import org.camunda.bpm.spring.boot.starter.security.oauth2.impl.OIDCAuthenticationProvider;
import org.camunda.bpm.webapp.impl.security.auth.ContainerBasedAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition;
import org.springframework.boot.autoconfigure.web.servlet.JerseyApplicationPath;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

@Configuration
//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE) // FIXME rethink order
@AutoConfigureAfter({ CamundaBpmAutoConfiguration.class, SpringProcessEngineServicesConfiguration.class })
@ConditionalOnBean(CamundaBpmProperties.class)
@Conditional(ClientsConfiguredCondition.class)
public class CamundaSpringSecurityOAuth2AutoConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(CamundaSpringSecurityOAuth2AutoConfiguration.class);
  private final String webappPath;
  private final String restApiPath;

  public CamundaSpringSecurityOAuth2AutoConfiguration(CamundaBpmProperties properties,
                                                      JerseyApplicationPath applicationPath) {

    WebappProperty webapp = properties.getWebapp();
    this.webappPath = webapp.getApplicationPath();
    this.restApiPath = applicationPath.getUrlMapping();
    logger.debug("webappPath = {} restApiPath {}", webappPath, restApiPath);
  }

  @Bean
  public FilterRegistrationBean<?> webappAuthenticationFilter() {
    FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<>();
    filterRegistration.setName("Container Based Authentication Filter");
    filterRegistration.setFilter(new ContainerBasedAuthenticationFilter());
    filterRegistration.setInitParameters(Map.of(
        ProcessEngineAuthenticationFilter.AUTHENTICATION_PROVIDER_PARAM, OIDCAuthenticationProvider.class.getName()));
    // make sure the filter is registered after the Spring Security Filter Chain
    filterRegistration.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER + 1);
    filterRegistration.addUrlPatterns(webappPath + "/app/*");
    filterRegistration.setDispatcherTypes(DispatcherType.REQUEST);
    return filterRegistration;
  }
  
  @Bean // FIXME not needed
  public FilterRegistrationBean<ProcessEngineAuthenticationFilter> restAuthenticationFilter() {
    FilterRegistrationBean<ProcessEngineAuthenticationFilter> filterRegistration = new FilterRegistrationBean<>();
    filterRegistration.setName("camunda-auth");
    filterRegistration.setFilter(new ProcessEngineAuthenticationFilter());
    filterRegistration.setInitParameters(Map.of(
        ProcessEngineAuthenticationFilter.AUTHENTICATION_PROVIDER_PARAM, OIDCAuthenticationProvider.class.getName(),
        ProcessEngineAuthenticationFilter .SERVLET_PATH_PREFIX, restApiPath));
    // make sure the filter is registered after the Spring Security Filter Chain
    filterRegistration.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER + 1);
    filterRegistration.addUrlPatterns(restApiPath);
    filterRegistration.setDispatcherTypes(DispatcherType.REQUEST);
    return filterRegistration;
  }
  
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    logger.info("Enabling Camunda Spring Security oauth2 integration");
    
    http.sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
        .authorizeHttpRequests(c -> c
            .requestMatchers(webappPath + "/app/**").authenticated()
            .requestMatchers(webappPath + "/api/**").authenticated()
            .requestMatchers(restApiPath + "*").authenticated() // FIXME permitAll
            .requestMatchers("/swaggerui/**").permitAll()
            .anyRequest().permitAll()
        )
        .oauth2Login(Customizer.withDefaults())
        //.oauth2Login(c -> c
        //    .defaultSuccessUrl(webappPath + "/app/welcome/default/#!/welcome", true))
        .oauth2Login(Customizer.withDefaults())
        .oidcLogout(Customizer.withDefaults())
        .oauth2Client(Customizer.withDefaults())
        .cors(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable);

    return http.build();
  }

}
