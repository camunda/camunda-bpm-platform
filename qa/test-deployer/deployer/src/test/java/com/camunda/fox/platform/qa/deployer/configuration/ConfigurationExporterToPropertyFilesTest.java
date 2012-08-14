package com.camunda.fox.platform.qa.deployer.configuration;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.After;
import org.junit.Test;

public class ConfigurationExporterToPropertyFilesTest {

  private static final String ARQ_PROPERTY_FILE = "target/fox.properties";
  private File createdFile;
  
  @After
  public void deleteTemporaryFile() {
    if (createdFile != null && createdFile.exists()) {
      createdFile.delete();
    }
  }

  @Test
  public void shouldExportFoxDeploymentConfigurationToPropertyFile() throws Exception {
    // given
    Properties expectedProperties = expectedProperties("properties/empty.fox.arquillian.properties");

    FoxDeploymentConfiguration configuration = new FoxDeploymentConfiguration();
    ConfigurationExporter exporter = new ConfigurationExporter(configuration, FoxDeploymentConfiguration.PROPERTY_PREFIX);
    
    // when
    exporter.toProperties(new FileOutputStream(ARQ_PROPERTY_FILE));
    
    // then
    Properties createdProperties = createdProperties();
    assertThat(createdProperties).isEqualTo(expectedProperties);
  }

  @Test
  public void shouldExportFoxDeploymentConfigurationWithPropertiesSetToPropertyFile() throws Exception {
    // given
    Properties expectedProperties = expectedProperties("properties/sample.fox.arquillian.properties");

    FoxDeploymentConfiguration configuration = new FoxDeploymentConfiguration();
    configuration.setExtensionArchiveJndiPrefix("foo/bar");
    configuration.setBundleActivitiCdi(false);
    
    ConfigurationExporter exporter = new ConfigurationExporter(configuration, FoxDeploymentConfiguration.PROPERTY_PREFIX);
    
    // when
    exporter.toProperties(new FileOutputStream(ARQ_PROPERTY_FILE));
    
    // then
    Properties createdProperties = createdProperties();
    assertThat(createdProperties).isEqualTo(expectedProperties);
  }

  // Utility methods
  private Properties createdProperties() throws IOException, FileNotFoundException {
    createdFile = new File(ARQ_PROPERTY_FILE);
    final Properties actualProperties = new Properties();
    actualProperties.load(new FileInputStream(createdFile));
    return actualProperties;
  }

  private Properties expectedProperties(String expectedPropertiesFileName) throws IOException,
          FileNotFoundException, URISyntaxException {
    final Properties expectedProperties = new Properties();
    final URI expectedPropertiesUri = Thread.currentThread().getContextClassLoader().getResource(expectedPropertiesFileName).toURI();
    expectedProperties.load(new FileInputStream(new File(expectedPropertiesUri)));
    return expectedProperties;
  }
}
