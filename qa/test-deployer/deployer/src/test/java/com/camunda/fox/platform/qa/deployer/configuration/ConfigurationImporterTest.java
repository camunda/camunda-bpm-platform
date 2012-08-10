package com.camunda.fox.platform.qa.deployer.configuration;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.Test;

public class ConfigurationImporterTest {

  @Test
  public void shouldImportFoxDeploymentConfigurationFromPropertyFile() throws Exception {
    // given
    Properties propertiesToImport = propertiesFromClassPath("properties/sample.fox.arquillian.properties");

    FoxDeploymentConfiguration expectedConfiguration = new FoxDeploymentConfiguration();
    expectedConfiguration.setExtensionArchiveJndiPrefix("foo/bar");
    expectedConfiguration.setProcessEngineName("default");
    
    ConfigurationImporter<FoxDeploymentConfiguration> importer = new ConfigurationImporter(FoxDeploymentConfiguration.class, FoxDeploymentConfiguration.PROPERTY_PREFIX);
    
    // when
    FoxDeploymentConfiguration configuration = importer.from(propertiesToImport);
    
    // then
    String importedExtensionJndiPrefix = configuration.getExtensionArchiveJndiPrefix();
    assertThat(importedExtensionJndiPrefix).isEqualTo(expectedConfiguration.getExtensionArchiveJndiPrefix());
  }

  private Properties propertiesFromClassPath(String propertyFileName) 
      throws IOException, FileNotFoundException, URISyntaxException {
    
    final Properties properties = new Properties();
    final URI propertiesUri = Thread.currentThread().getContextClassLoader().getResource(propertyFileName).toURI();
    properties.load(new FileInputStream(new File(propertiesUri)));
    return properties;
  }
}
