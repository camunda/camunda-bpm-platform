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

import java.util.Properties;

/**
 * Provides product information data loaded from a *.properties file.
 */
public class ProductPropertiesUtil {

  protected static final String PROPERTIES_FILE_PATH = "/org/camunda/bpm/engine/product-info.properties";
  protected static final String VERSION_PROPERTY = "camunda.version";
  protected static final Properties INSTANCE = PropertiesUtil.getProperties(PROPERTIES_FILE_PATH);

  protected ProductPropertiesUtil() {
  }

  /**
   * @return the current version of the product (e.g. <code>7.15.0-SNAPSHOT</code>)
   */
  public static String getProductVersion() {
    // in case the `product-info.properties` file is missing,
    // try to get the product version from the manifest
    return INSTANCE.getProperty(VERSION_PROPERTY, ProductPropertiesUtil.class.getPackage().getImplementationVersion());
  }

}
