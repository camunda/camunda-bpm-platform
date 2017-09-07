package org.camunda.bpm.spring.boot.starter.configuration.impl.custom;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.camunda.bpm.spring.boot.starter.property.FilterProperty;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class CreateFilterConfiguration extends AbstractCamundaConfiguration {

  protected String filterName;

  @PostConstruct
  public void init() {
    filterName = Optional.ofNullable(camundaBpmProperties.getFilter())
      .map(FilterProperty::getCreate)
      .orElseThrow(fail("filter.create not configured!"));
  }

  @Override
  public void postProcessEngineBuild(final ProcessEngine processEngine) {
    Objects.requireNonNull(filterName);
    Filter filter = processEngine.getFilterService().createFilterQuery().filterName(filterName).singleResult();
    if (filter == null) {
      filter = processEngine.getFilterService().newTaskFilter(filterName);
      processEngine.getFilterService().saveFilter(filter);
      LOG.createInitialFilter(filter);
    }
  }

  @Override
  public String toString() {
    return createToString(Collections.singletonMap("filterName", filterName));
  }
}
