package com.camunda.fox.cycle.web.jaxrs.ext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import com.camunda.fox.cycle.web.I18nResolver;


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
  public I18n i18n() {
    return I18nFactory.getI18n(getClass(), "i18n.Messages");
  }
  
  @Bean
  public TemplateResolver templateResolver() {
    ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
    resolver.setTemplateMode("LEGACYHTML5");
    resolver.setCharacterEncoding("UTF-8");
    resolver.setPrefix("/WEB-INF/views/");
    resolver.setSuffix(".html");
    return resolver;
  }

  @Bean
  public SpringTemplateEngine templateEngine() {
    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(templateResolver());
    engine.setCacheManager(null);
    engine.setMessageResolver(new I18nResolver(i18n()));
    return engine;
  }
}
