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
package org.camunda.bpm.client.util;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyUtil {

  public static final String DEFAULT_PROPERTIES_PATH = "integration-rules.properties";
  public static final String CAMUNDA_ENGINE_REST = "camunda.engine.rest";
  public static final String CAMUNDA_ENGINE_NAME = "camunda.engine.name";

  private static Logger logger = LoggerFactory.getLogger(PropertyUtil.class);

  public static Properties loadProperties(String resource) {
    Properties properties = new Properties();
    try {
      properties.load(
          PropertyUtil.class
              .getClassLoader()
              .getResourceAsStream(resource)
      );
    } catch (IOException ex) {
      logger.error("Unable to load test properties!", ex);
    }
    return properties;
  }

  public static Properties loadProperties() {
    return PropertyUtil.loadProperties("service.properties");
  }

}
