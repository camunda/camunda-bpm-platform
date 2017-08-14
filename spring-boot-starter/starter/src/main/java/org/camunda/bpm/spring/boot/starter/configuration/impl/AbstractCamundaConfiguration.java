package org.camunda.bpm.spring.boot.starter.configuration.impl;

import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEngineLogger;
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEnginePlugin;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

@Order(Ordering.DEFAULT_ORDER)
public abstract class AbstractCamundaConfiguration extends SpringBootProcessEnginePlugin {

  protected static final SpringBootProcessEngineLogger LOG = SpringBootProcessEngineLogger.LOG;

  protected static Supplier<IllegalStateException> fail(String message) {
    return () -> new IllegalStateException(message);
  }

  protected String createToString(Map<String, Object> attributes) {
    StringJoiner joiner = new StringJoiner(", ", getClass().getSimpleName() + "[", "]");
    attributes.entrySet().forEach(e -> joiner.add(e.getKey() + "=" + e.getValue()));

    return joiner.toString();
  }

  /**
   * @deprecated use {@link SpringBootProcessEngineLogger}
   */
  @Deprecated
  protected final Logger logger = getLogger(this.getClass());

  @Autowired
  protected CamundaBpmProperties camundaBpmProperties;


}
