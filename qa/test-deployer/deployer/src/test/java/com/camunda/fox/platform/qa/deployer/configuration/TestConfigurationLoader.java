package com.camunda.fox.platform.qa.deployer.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

public class TestConfigurationLoader {

  private static final String DEFAULT_CONFIG_FILENAME = "xml/arquillian.xml";

  public static InputStream loadArquillianConfiguration(String fileName) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return classLoader.getResourceAsStream(fileName);
  }

  public static ArquillianDescriptor createArquillianDescriptor(String fileName) {
    return Descriptors.importAs(ArquillianDescriptor.class).from(
            TestConfigurationLoader.loadArquillianConfiguration(fileName));
  }

  public static ArquillianDescriptor createArquillianDescriptorFromDefaultConfigurationFile() {
    return createArquillianDescriptor(DEFAULT_CONFIG_FILENAME);
  }

  public static FoxConfiguration createDefaultConfiguration() {
    return createFoxConfigurationFrom(DEFAULT_CONFIG_FILENAME);
  }

  public static Properties createPropertiesFromCustomConfigurationFile() throws IOException {
    Properties properties = new Properties();
    properties.load(loadArquillianConfiguration("properties/custom.fox.arquillian.properties"));
    return properties;
  }

  public static FoxConfiguration createFoxConfigurationFrom(String fileName) {
    ArquillianDescriptor descriptor = createArquillianDescriptor(fileName);
    return new ConfigurationImporter<FoxConfiguration>(FoxConfiguration.class, FoxConfiguration.PROPERTY_PREFIX).from(descriptor, FoxConfiguration.EXTENSION_QUALIFIER);
  }
}
