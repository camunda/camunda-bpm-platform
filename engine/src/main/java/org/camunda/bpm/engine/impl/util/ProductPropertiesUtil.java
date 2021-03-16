package org.camunda.bpm.engine.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 * Provides product information data loaded from a *.properties file.
 */
public class ProductPropertiesUtil {

  protected static final EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;
  protected static final String PROPERTIES_FILE_PATH = "/org/camunda/bpm/engine/product-info.properties";
  protected static final String VERSION_PROPERTY = "camunda.version";
  protected static final Properties INSTANCE = getProperties();

  protected static Properties getProperties() {
    Properties productProperties = new Properties();
    try (InputStream inputStream = ProductPropertiesUtil.class.getResourceAsStream(PROPERTIES_FILE_PATH)) {
      productProperties.load(inputStream);
    } catch (IOException e) {
      LOG.logMissingProductInformationPropertiesFile();
    }

    return productProperties;
  }

  /**
   * @return the current version of the product (e.g. <code>7.15.0-SNAPSHOT</code>)
   */
  public static String getProductVersion() {
    return INSTANCE.getProperty(VERSION_PROPERTY);
  }

}
