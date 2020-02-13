/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
