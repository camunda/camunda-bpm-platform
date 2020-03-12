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
package org.camunda.bpm.spring.boot.starter.util;

import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.PREFIX;

/**
 * Return the full version string of the present Camunda codebase, or
 * {@code null} if it cannot be determined.
 * <p/>
 * return the version of Camunda or {@code null}
 *
 * @see Package#getImplementationVersion()
 */
public class CamundaBpmVersion implements Supplier<String> {

  private static final String VERSION_FORMAT = "(v%s)";
  public static final String VERSION = "version";
  public static final String IS_ENTERPRISE = "is-enterprise";
  public static final String FORMATTED_VERSION = "formatted-version";

  public static String key(String name) {
    return PREFIX + "." + name;
  }

  private final String version;
  private final boolean isEnterprise;
  private final String formattedVersion;

  public CamundaBpmVersion() {
    this(ProcessEngine.class.getPackage());
  }

  CamundaBpmVersion(final Package pkg) {
    this.version = Optional.ofNullable(pkg.getImplementationVersion())
      .map(String::trim)
      .orElse("");
    this.isEnterprise = version.endsWith("-ee");
    this.formattedVersion = String.format(VERSION_FORMAT, version);
  }

  @Override
  public String get() {
    return version;
  }

  public boolean isEnterprise() {
    return isEnterprise;
  }

  public PropertiesPropertySource getPropertiesPropertySource() {
    final Properties props = new Properties();
    props.put(key(VERSION), version);
    props.put(key(IS_ENTERPRISE), isEnterprise);
    props.put(key(FORMATTED_VERSION), formattedVersion);

    return new PropertiesPropertySource(this.getClass().getSimpleName(), props);
  }


}
