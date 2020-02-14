package org.camunda.bpm.run;

import javax.servlet.Filter;
import javax.servlet.ServletException;

import org.apache.catalina.filters.CorsFilter;
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.camunda.bpm.run.property.CamundaBpmRunAuthenticationProperties;
import org.camunda.bpm.run.property.CamundaBpmRunCorsProperty;
import org.camunda.bpm.run.property.CamundaBpmRunProperties;
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.camunda.bpm.spring.boot.starter.rest.CamundaBpmRestInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(CamundaBpmRunProperties.class)
@Configuration
@AutoConfigureAfter({ CamundaBpmAutoConfiguration.class })
public class CamundaBpmRunSecurityConfiguration {

  @Autowired
  CamundaBpmRunProperties camundaBpmRunProperties;

  @Bean
  @ConditionalOnClass(CamundaBpmRestInitializer.class)
  @ConditionalOnProperty(name = "enabled", havingValue = "true", prefix = CamundaBpmRunAuthenticationProperties.PREFIX)
  public FilterRegistrationBean<Filter> processEngineAuthenticationFilter() {
    FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
    registration.setName("camunda-auth");
    registration.setFilter(new ProcessEngineAuthenticationFilter());
    registration.addUrlPatterns("/rest/*");

    // if nothing is set, use Http Basic authentication
    CamundaBpmRunAuthenticationProperties properties = camundaBpmRunProperties.getAuth();
    if (properties.getAuthentication() == null || CamundaBpmRunAuthenticationProperties.DEFAULT_AUTH.equals(properties.getAuthentication())) {
      registration.addInitParameter("authentication-provider", "org.camunda.bpm.engine.rest.security.auth.impl.HttpBasicAuthenticationProvider");
    }

    return registration;
  }

  @Bean
  @ConditionalOnProperty(name = "enabled", havingValue = "true", prefix = CamundaBpmRunCorsProperty.PREFIX)
  public FilterRegistrationBean<Filter> corsFilter() throws ServletException {
    FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
    registration.setName("camunda-cors");
    CorsFilter corsFilter = new CorsFilter();
    registration.setFilter(corsFilter);
    registration.addUrlPatterns("/rest/*");
    registration.addInitParameter(CorsFilter.PARAM_CORS_ALLOWED_ORIGINS, camundaBpmRunProperties.getCors().getAllowedOrigins());
    return registration;
  }
}
