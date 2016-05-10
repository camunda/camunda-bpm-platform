package org.camunda.bpm.engine.rest.util.container;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.camunda.bpm.engine.rest.AbstractRestServiceTest;

public abstract class EmbeddedServerBootstrap {

  protected static final String PORT_PROPERTY = "rest.http.port";
  protected static final String ROOT_RESOURCE_PATH = "/rest-test";
  private static final String PROPERTIES_FILE = "/testconfig.properties";

  public abstract void start();

  public abstract void stop();

  protected Properties readProperties() {
    InputStream propStream = null;
    Properties properties = new Properties();

    try {
      propStream = AbstractRestServiceTest.class.getResourceAsStream(PROPERTIES_FILE);
      properties.load(propStream);
    } catch (IOException e) {
      throw new ServerBootstrapException(e);
    } finally {
      try {
        propStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return properties;
  }
}
