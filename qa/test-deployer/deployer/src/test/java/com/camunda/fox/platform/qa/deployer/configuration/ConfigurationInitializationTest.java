package com.camunda.fox.platform.qa.deployer.configuration;

import com.camunda.fox.platform.qa.deployer.client.FoxConfigurationProducer;
import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Before;
import org.junit.Test;

public class ConfigurationInitializationTest extends AbstractTestTestBase {

  @Override
  protected void addExtensions(List<Class<?>> extensions) {
    extensions.add(FoxConfigurationProducer.class);
  }

  @Before
  public void initializeArquillianDescriptor() {
    bind(ApplicationScoped.class, ArquillianDescriptor.class, Descriptors.create(ArquillianDescriptor.class));
  }

  @Test
  public void shouldCreateConfigurationBeforeClassIsExecuted() throws Exception {
    // given
    getManager().getContext(SuiteContext.class).activate();

    // when
    fire(new BeforeSuite());
    FoxConfiguration foxConfiguration = getManager().getContext(ApplicationContext.class).getObjectStore().get(FoxConfiguration.class);

    // then
    assertThat(foxConfiguration).isNotNull();
  }
}
