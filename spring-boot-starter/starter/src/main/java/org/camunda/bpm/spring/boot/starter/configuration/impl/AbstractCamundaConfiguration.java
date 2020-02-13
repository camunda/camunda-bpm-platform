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
