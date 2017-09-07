package org.camunda.bpm.spring.boot.starter.configuration.id;

import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@SuppressWarnings("unused")
public class IdGeneratorConfiguration {

  public static final String PROPERTY_NAME = "id-generator";

  public static final String SIMPLE = "simple";
  public static final String STRONG = "strong";
  public static final String PREFIXED = "prefixed";

  @Bean
  @ConditionalOnMissingBean(IdGenerator.class)
  @ConditionalOnProperty(prefix = CamundaBpmProperties.PREFIX, name = PROPERTY_NAME, havingValue = STRONG, matchIfMissing = true)
  public IdGenerator strongUuidGenerator() {
    return new StrongUuidGenerator();
  }


  @Bean
  @ConditionalOnMissingBean(IdGenerator.class)
  @ConditionalOnProperty(prefix = CamundaBpmProperties.PREFIX, name = PROPERTY_NAME, havingValue = PREFIXED)
  public IdGenerator prefixedUuidGenerator(@Value("${spring.application.name}") String applicationName) {
    return new PrefixedUuidGenerator(applicationName);
  }

}
