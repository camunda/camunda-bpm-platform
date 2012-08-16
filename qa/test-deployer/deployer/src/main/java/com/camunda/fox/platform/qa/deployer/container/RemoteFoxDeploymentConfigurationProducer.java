package com.camunda.fox.platform.qa.deployer.container;

import com.camunda.fox.platform.qa.deployer.configuration.ConfigurationImporter;
import com.camunda.fox.platform.qa.deployer.configuration.FoxDeploymentConfiguration;
import com.camunda.fox.platform.qa.deployer.event.BeforeFoxTest;
import com.camunda.fox.platform.qa.deployer.exception.InitializationException;
import com.camunda.fox.platform.qa.deployer.war.ApplicationArchiveContext;
import java.io.IOException;
import java.util.Properties;
import org.jboss.arquillian.core.api.Instance;

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.TestScoped;

/**
 * Triggers deployment configuration creation on the container side.
 *
 * @author nico.rehwaldt@camunda.com
 */
public class RemoteFoxDeploymentConfigurationProducer {

  @Inject
  private Instance<ApplicationArchiveContext> applicationArchiveContext;
  
  @Inject
  @TestScoped
  private InstanceProducer<FoxDeploymentConfiguration> configurationProducer;

  public void beforeTest(@Observes(precedence=200) BeforeFoxTest event) {
    ConfigurationImporter<FoxDeploymentConfiguration> configurationImporter = new ConfigurationImporter(FoxDeploymentConfiguration.class, FoxDeploymentConfiguration.PROPERTY_PREFIX);
    configurationProducer.set(configurationImporter.from(loadProperties()));
  }

  // Private methods
  private Properties loadProperties() {
    
    Properties properties = new Properties();
    try {
      properties.load(applicationArchiveContext.get().getClassLoader().getResourceAsStream(FoxDeploymentConfiguration.PROPERTIES_FILE));
    } catch (IOException e) {
      throw new InitializationException("Unable to load deployment properties in container.", e);
    }
    return properties;
  }
}
