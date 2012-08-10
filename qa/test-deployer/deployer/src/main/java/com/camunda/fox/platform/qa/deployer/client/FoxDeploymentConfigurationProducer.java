package com.camunda.fox.platform.qa.deployer.client;

import com.camunda.fox.platform.qa.deployer.configuration.FoxDeploymentConfiguration;
import java.util.logging.Logger;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class FoxDeploymentConfigurationProducer {
  
  private static Logger log = Logger.getLogger(FoxDeploymentConfigurationProducer.class.getName());
  
  @Inject
  @SuiteScoped
  private InstanceProducer<FoxDeploymentConfiguration> deploymentConfigurationProducer;

  public void beforeSuite(@Observes BeforeSuite beforeClassEvent) {
    deploymentConfigurationProducer.set(new FoxDeploymentConfiguration());
  }
}
