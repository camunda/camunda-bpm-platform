package com.camunda.fox.platform.qa.deployer.container;

import com.camunda.fox.platform.qa.deployer.configuration.ConfigurationImporter;
import com.camunda.fox.platform.qa.deployer.configuration.FoxConfiguration;
import com.camunda.fox.platform.qa.deployer.exception.InitializationException;
import java.io.IOException;
import java.util.Properties;

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 * Triggers configuration creation on the container side.
 *
 * @author nico.rehwaldt@camunda.com
 */
public class RemoteFoxConfigurationProducer {
  
  @Inject
  @ApplicationScoped
  private InstanceProducer<FoxConfiguration> configurationProducer;

  public void configure(@Observes(precedence=400) BeforeSuite beforeSuiteEvent) {
    ConfigurationImporter<FoxConfiguration> configurationImporter = new ConfigurationImporter(FoxConfiguration.class, FoxConfiguration.PROPERTY_PREFIX);
    configurationProducer.set(configurationImporter.from(loadProperties()));
  }

  // Private methods
  private Properties loadProperties() {
    
    Properties properties = new Properties();
    try {
      properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(FoxConfiguration.PROPERTIES_FILE));
    } catch (IOException e) {
      throw new InitializationException("Unable to load arquillian properties in container.", e);
    }
    return properties;
  }
}
