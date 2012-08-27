package com.camunda.fox.cycle.web.jaxrs.ext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * Exposes the template resolving beans to the spring context. 
 * 
 * @author andreas.drobisch
 * @author nico.rehwaldt
 * 
 * @see TemplateMessageBodyWriter
 * @see TemplateExceptionMapper
 */
@Configuration
public class TemplateConfiguration {

  @Bean
  public TemplateResolver templateResolver() {
    ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
    resolver.setTemplateMode("LEGACYHTML5");
    resolver.setCharacterEncoding("UTF-8");
    resolver.setPrefix("WEB-INF/views/");
    resolver.setSuffix(".html");
    return resolver;
  }

  @Bean
  public SpringTemplateEngine templateEngine() {
    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(templateResolver());
    engine.setCacheManager(null);
    return engine;
  }
}
