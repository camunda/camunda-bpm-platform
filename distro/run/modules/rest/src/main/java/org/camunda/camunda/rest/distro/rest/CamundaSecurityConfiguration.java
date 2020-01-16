package org.camunda.camunda.rest.distro.rest;


import javax.servlet.Filter;

import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(CamundaRestDistroProperties.class)
@Configuration
@AutoConfigureAfter({ CamundaBpmAutoConfiguration.class })
public class CamundaSecurityConfiguration {

  @Autowired
  CamundaRestDistroProperties camundaRestDistroProperties;

  @Bean
  public FilterRegistrationBean<Filter> processEngineAuthenticationFilter() {
    FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
    registration.setName("camunda-auth");
    registration.setFilter(getProcessEngineAuthenticationFilter());
    registration.addUrlPatterns("/rest/*");

    // if nothing is set, use Http Basic authentication
    if (camundaRestDistroProperties.getAuthentication() == null ||
        CamundaRestDistroProperties.DEFAULT_AUTH
            .equals(camundaRestDistroProperties.getAuthentication())) {

      registration.addInitParameter("authentication-provider",
                                    "org.camunda.bpm.engine.rest.security.auth.impl.HttpBasicAuthenticationProvider");
    }

    return registration;
  }

  @Bean
  public Filter getProcessEngineAuthenticationFilter() {
    return new ProcessEngineAuthenticationFilter();
  }
}
