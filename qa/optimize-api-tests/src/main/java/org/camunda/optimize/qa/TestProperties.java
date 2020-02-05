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
package org.camunda.optimize.qa;

import org.camunda.bpm.engine.impl.util.IoUtil;

import java.io.InputStream;
import java.util.Properties;

public class TestProperties {

  public static final String PROPERTIES_FILE_NAME = "optimize-test-config.properties";
  private final Properties properties;

  public TestProperties() {
    this.properties = loadProperties();
  }

  public String getProperty(String key, String defaultValue) {
    String value = properties.getProperty(key);
    if (value == null || value.isEmpty() || value.startsWith("${")) {
      value = defaultValue;
    }
    return value;
  }

  private Properties loadProperties() {
    InputStream propertyInputStream = null;
    try {
      propertyInputStream = TestProcessEngine.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME);
      Properties properties = new Properties();
      properties.load(propertyInputStream);
      return properties;

    } catch (Exception e) {
      throw new RuntimeException("Cannot load properties from file " + PROPERTIES_FILE_NAME + ": " + e);
    } finally {
      IoUtil.closeSilently(propertyInputStream);
    }
  }
}
