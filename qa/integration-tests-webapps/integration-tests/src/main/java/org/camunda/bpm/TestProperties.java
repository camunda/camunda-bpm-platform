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
package org.camunda.bpm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author drobisch
 */
public class TestProperties {

  public static final String TESTCONFIG_PROPERTIES_FILE = "/testconfig.properties";

  private final Properties properties;
  private final int defaultPort;

  public TestProperties() throws IOException {
    this(8080);
  }

  public TestProperties(int defaultPort) throws IOException {

    this.defaultPort = defaultPort;

    properties = getTestProperties();
  }

  public Properties getProps() {
    return properties;
  }

  public String getApplicationPath(String contextPath) {
    return "http://" + getHttpHost() + ":" + getHttpPort() + contextPath;
  }

  public int getHttpPort() {

    try {
      return Integer.parseInt(properties.getProperty("http.port"));
    } catch (RuntimeException e) {
      return defaultPort;
    }
  }
  
  public String getStringProperty(String propName, String defaultValue) {
    return properties.getProperty(propName, defaultValue);    
  }

  public String getHttpHost() {
    return properties.getProperty("http.host", "localhost");
  }

  public static Properties getTestProperties() throws IOException {
    Properties properties = new Properties();

    InputStream propertiesStream = null;
    try {
      propertiesStream = TestProperties.class.getResourceAsStream(TESTCONFIG_PROPERTIES_FILE);
      properties.load(propertiesStream);
    } finally {
      try {
        if (propertiesStream != null) {
          propertiesStream.close();
        }
      } catch(Exception e) {
        // nop
      }
    }

    return properties;
  }
}
