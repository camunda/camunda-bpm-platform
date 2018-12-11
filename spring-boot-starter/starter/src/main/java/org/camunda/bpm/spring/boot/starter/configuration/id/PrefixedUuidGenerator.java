/*
 * Copyright © 2015-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
