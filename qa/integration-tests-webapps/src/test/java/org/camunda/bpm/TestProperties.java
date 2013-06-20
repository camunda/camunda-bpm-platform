package org.camunda.bpm;

import org.camunda.bpm.cycle.test.TestCycleRoundtripIT;
import org.camunda.bpm.cycle.util.IoUtil;

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
      propertiesStream = TestCycleRoundtripIT.class.getResourceAsStream(TESTCONFIG_PROPERTIES_FILE);
      properties.load(propertiesStream);
      String httpPort = (String) properties.get("http.port");
    } finally {
      IoUtil.closeSilently(propertiesStream);
    }

    return properties;
  }
}
