package com.camunda.fox.platform.qa.deployer.configuration;

import java.io.*;
import static org.fest.assertions.Assertions.assertThat;

import java.util.Properties;
import org.junit.After;

import org.junit.Test;

public class ConfigurationRoundtripTest {

  static final String PROPERTIES_FILE = "fox-arq.properties";
  
  private File createdFile;
  
  @After
  public void deleteTemporaryFile() {
    if (createdFile != null && createdFile.exists()) {
      createdFile.delete();
    }
  }
  
  @Test
  public void shouldImportFoxDeploymentConfigurationAfterExport() throws Exception {
    // given
    FoxDeploymentConfiguration originalConfiguration = new FoxDeploymentConfiguration();
    originalConfiguration.setExtensionArchiveJndiPrefix("97ac15aa-fbcd-4831-8f9f-43ccb1481fae");
    
    ConfigurationExporter<FoxDeploymentConfiguration> exporter = new ConfigurationExporter<FoxDeploymentConfiguration>(originalConfiguration, FoxDeploymentConfiguration.PROPERTY_PREFIX);
    ConfigurationImporter<FoxDeploymentConfiguration> importer = new ConfigurationImporter(FoxDeploymentConfiguration.class, FoxDeploymentConfiguration.PROPERTY_PREFIX);
    
    // when
    createdFile = new File(PROPERTIES_FILE);
    
    FileOutputStream os = new FileOutputStream(createdFile);
    exporter.toProperties(os);
    os.close();
    
    Properties properties = new Properties();
    FileInputStream is = new FileInputStream(createdFile);
    properties.load(is);
    is.close();
    
    FoxDeploymentConfiguration readConfiguration = importer.from(properties);
    
    // then
    assertThat(readConfiguration.getExtensionArchiveJndiPrefix()).isEqualTo(readConfiguration.getExtensionArchiveJndiPrefix());
  }
}
