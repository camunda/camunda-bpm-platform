package org.camunda.bpm.spring.boot.starter.configuration.id;

import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;

import static java.util.Objects.requireNonNull;

public class PrefixedUuidGenerator implements IdGenerator {

  private final StrongUuidGenerator strongUuidGenerator = new StrongUuidGenerator();

  private final String prefix;

  public PrefixedUuidGenerator(final String applicationName) {
    this.prefix = requireNonNull(StringUtils.trimToNull(applicationName), "prefix must not be null or blank! set the spring.application.name property!");
  }

  @Override
  public String getNextId() {
    return String.join("-", prefix, strongUuidGenerator.getNextId());
  }
}
