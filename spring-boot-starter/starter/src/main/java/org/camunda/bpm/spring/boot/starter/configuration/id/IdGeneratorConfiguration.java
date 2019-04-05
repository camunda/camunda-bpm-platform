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
