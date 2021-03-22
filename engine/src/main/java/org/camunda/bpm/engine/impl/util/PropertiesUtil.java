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
package org.camunda.bpm.engine.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;

public class PropertiesUtil {

  protected static final EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;

  /**
   * Reads a <code>.properties</code> file from the classpath and provides a {@link Properties} object.
   */
  public static Properties getProperties(String propertiesFile) {
    Properties productProperties = new Properties();
    try (InputStream inputStream = ProductPropertiesUtil.class.getResourceAsStream(propertiesFile)) {
      productProperties.load(inputStream);
    } catch (IOException | NullPointerException e) {
      // if `propertiesFile` is null, the file is missing, or an error occurs during reading
      LOG.logMissingPropertiesFile(propertiesFile);
    }

    return productProperties;
  }
}
