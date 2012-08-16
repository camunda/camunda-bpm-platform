package com.camunda.fox.platform.qa.deployer.client;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

import com.camunda.fox.platform.qa.deployer.configuration.ConfigurationImporter;
import com.camunda.fox.platform.qa.deployer.configuration.FoxConfiguration;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class FoxConfigurationProducer {

  @Inject
  @ApplicationScoped
  private Instance<ArquillianDescriptor> descriptor;
  
  @Inject
  @ApplicationScoped
  private InstanceProducer<FoxConfiguration> configurationProducer;

  public void configure(@Observes BeforeSuite beforeSuiteEvent) {
    ConfigurationImporter<FoxConfiguration> extractor = new ConfigurationImporter(FoxConfiguration.class, FoxConfiguration.PROPERTY_PREFIX);
    configurationProducer.set(extractor.from(descriptor.get(), FoxConfiguration.EXTENSION_QUALIFIER));
  }
}
