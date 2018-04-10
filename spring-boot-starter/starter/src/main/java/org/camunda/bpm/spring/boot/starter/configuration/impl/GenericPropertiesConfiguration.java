package org.camunda.bpm.spring.boot.starter.configuration.impl;

import java.util.Map;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.camunda.bpm.spring.boot.starter.property.GenericProperties;
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEngineLogger;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;

@Order(Ordering.DEFAULT_ORDER - 1)
public class GenericPropertiesConfiguration extends AbstractCamundaConfiguration {

  protected static final SpringBootProcessEngineLogger LOG = SpringBootProcessEngineLogger.LOG;

  @Override
  public void preInit(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
    GenericProperties genericProperties = camundaBpmProperties.getGenericProperties();
    final Map<String, Object> properties = genericProperties.getProperties();

    if (!CollectionUtils.isEmpty(properties)) {
      ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
      Binder binder = new Binder(source);
      try {
        if (genericProperties.isIgnoreUnknownFields()) {
          binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(springProcessEngineConfiguration));
        } else {
          binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(springProcessEngineConfiguration), new NoUnboundElementsBindHandler(BindHandler.DEFAULT));
        }
      } catch (Exception e) {
        throw LOG.exceptionDuringBinding(e.getMessage());
      }
      logger.debug("properties bound to configuration: {}", genericProperties);
    }
  }

}
