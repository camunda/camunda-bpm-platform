package org.camunda.bpm.spring.boot.starter.webapp;

import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.webapp.filter.LazyDelegateFilter.InitHook;
import org.camunda.bpm.spring.boot.starter.webapp.filter.LazyInitRegistration;
import org.camunda.bpm.spring.boot.starter.webapp.filter.ResourceLoaderDependingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter(CamundaBpmAutoConfiguration.class)
public class CamundaBpmWebappAutoConfiguration implements WebMvcConfigurer {

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private CamundaBpmProperties properties;


  @Bean
  public CamundaBpmWebappInitializer camundaBpmWebappInitializer() {
    return new CamundaBpmWebappInitializer(properties);
  }

  @Bean(name = "resourceLoaderDependingInitHook")
  public InitHook<ResourceLoaderDependingFilter> resourceLoaderDependingInitHook() {
    return filter -> {
      filter.setResourceLoader(resourceLoader);
      filter.setWebappProperty(properties.getWebapp());
    };
  }

  @Bean
  public LazyInitRegistration lazyInitRegistration() {
    return new LazyInitRegistration();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    final String classpath = "classpath:" + properties.getWebapp().getWebjarClasspath();
    registry.addResourceHandler("/lib/**").addResourceLocations(classpath + "/lib/");
    registry.addResourceHandler("/api/**").addResourceLocations("classpath:/api/");
    registry.addResourceHandler("/app/**").addResourceLocations(classpath + "/app/");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    if (properties.getWebapp().isIndexRedirectEnabled()) {
      registry.addRedirectViewController("/", "/app/");
    }
  }

}
