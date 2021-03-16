package org.camunda.bpm.engine.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestconfigProperties {

  protected static final String PROPERTIES_FILE_PATH = "/testconfig.properties";
  protected static final String VERSION_PROPERTY = "camunda.version";

  protected static Properties loadTestconfigProperties() throws IOException {
    Properties configProps = new Properties();
    try (InputStream propStream = TestconfigProperties.class.getResourceAsStream(PROPERTIES_FILE_PATH)) {
      configProps = new Properties();
      configProps.load(propStream);
    }

    return configProps;
  }

  public static String getEngineVersion() throws IOException {
    return loadTestconfigProperties().getProperty(VERSION_PROPERTY);
  }

}
