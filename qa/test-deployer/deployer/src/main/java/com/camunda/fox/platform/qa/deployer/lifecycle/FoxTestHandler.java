package com.camunda.fox.platform.qa.deployer.lifecycle;

import com.camunda.fox.platform.qa.deployer.configuration.FoxConfiguration;
import com.camunda.fox.platform.qa.deployer.event.AfterFoxTest;
import com.camunda.fox.platform.qa.deployer.event.BeforeFoxTest;
import com.camunda.fox.platform.qa.deployer.event.DeployProcessDefinitions;
import com.camunda.fox.platform.qa.deployer.event.UndeployProcessDefinitions;
import com.camunda.fox.platform.qa.deployer.metadata.MetadataExtractor;
import com.camunda.fox.platform.qa.deployer.metadata.MetadataProvider;
import com.camunda.fox.platform.qa.deployer.metadata.FoxExtensionEnabler;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class FoxTestHandler {

  @Inject
  @ClassScoped
  private InstanceProducer<MetadataExtractor> metadataExtractor;
  
  @Inject
  @ClassScoped
  private InstanceProducer<FoxExtensionEnabler> extensionEnabler;
  
  @Inject
  @TestScoped
  private InstanceProducer<MetadataProvider> metadataProvider;
  
  @Inject
  public Instance<FoxConfiguration> configuration;
  
  @Inject
  private Event<BeforeFoxTest> beforeFoxTestEvent;
  
  @Inject
  private Event<DeployProcessDefinitions> deployProcessDefinitionsEvent;
  
  @Inject
  private Event<UndeployProcessDefinitions> undeployProcessDefinitionsEvent;
  
  @Inject
  private Event<AfterFoxTest> afterFoxTestEvent;
  
  public void beforeSuite(@Observes(precedence=100) BeforeClass beforeClass) {
    metadataExtractor.set(new MetadataExtractor(beforeClass.getTestClass()));
    extensionEnabler.set(new FoxExtensionEnabler(metadataExtractor.get()));
  }

  public void beforeTest(@Observes(precedence=100) Before beforeTestEvent) {
    FoxConfiguration foxConfiguration = configuration.get();
    metadataProvider.set(new MetadataProvider(beforeTestEvent.getTestMethod(), metadataExtractor.get(), foxConfiguration));

    if (extensionEnabler.get().isExtensionRequired()) {
      beforeFoxTestEvent.fire(new BeforeFoxTest(beforeTestEvent));
      deployProcessDefinitionsEvent.fire(new DeployProcessDefinitions());
    }
  }

  public void afterTest(@Observes After afterTestEvent) {
    if (extensionEnabler.get().isExtensionRequired()) {
      undeployProcessDefinitionsEvent.fire(new UndeployProcessDefinitions());
      afterFoxTestEvent.fire(new AfterFoxTest(afterTestEvent));
    }
  }
}
