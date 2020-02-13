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
