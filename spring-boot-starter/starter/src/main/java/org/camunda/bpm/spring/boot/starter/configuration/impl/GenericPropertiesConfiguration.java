package org.camunda.bpm.spring.boot.starter.configuration.impl;

import java.util.Map;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.property.GenericProperties;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;

@Order(Ordering.DEFAULT_ORDER - 1)
public class GenericPropertiesConfiguration extends AbstractCamundaConfiguration {

  @Override
  public void preInit(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
    GenericProperties genericProperties = camundaBpmProperties.getGenericProperties();
    final Map<String, Object> properties = genericProperties.getProperties();
    if (!CollectionUtils.isEmpty(properties)) {
      RelaxedDataBinder relaxedDataBinder = new RelaxedDataBinder(springProcessEngineConfiguration);
      relaxedDataBinder.setIgnoreInvalidFields(genericProperties.isIgnoreInvalidFields());
      relaxedDataBinder.setIgnoreUnknownFields(genericProperties.isIgnoreUnknownFields());
      relaxedDataBinder.bind(getPropertyValues(properties));
      logger.debug("properties bound to configuration: {}", genericProperties);
    }
  }

  protected PropertyValues getPropertyValues(Map<String, Object> genericProperties) {
    return new MutablePropertyValues(genericProperties);
  }

}
