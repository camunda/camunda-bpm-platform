package com.camunda.fox.platform.qa.deployer.deployment;

import com.camunda.fox.platform.qa.deployer.client.FoxDeploymentConfigurationProducer;
import com.camunda.fox.platform.qa.deployer.configuration.FoxDeploymentConfiguration;
import com.camunda.fox.platform.qa.deployer.sample.WithMixedProcessDeployments;
import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Before;
import org.junit.Test;

public class FoxDynamicDependencyAppenderConfigurationPopulationTest extends AbstractTestTestBase {

  @Override
  protected void addExtensions(List<Class<?>> extensions) {
    extensions.add(FoxDeploymentConfigurationProducer.class);
    extensions.add(FoxDynamicDependencyAppender.class);
  }

  @Before
  public void initializeArquillianDescriptor() {
    bind(ApplicationScoped.class, ArquillianDescriptor.class, Descriptors.create(ArquillianDescriptor.class));
  }

  @Test
  public void shouldProperlyFillConfigurationWhenProcessingJar() throws Exception {
    // given
    getManager().getContext(SuiteContext.class).activate();
    fire(new BeforeSuite());
    FoxDynamicDependencyAppender appender = getManager().getExtension(FoxDynamicDependencyAppender.class);

    // when
    Archive<?> archive = ShrinkWrap.create(JavaArchive.class, "foo.jar");
    appender.process(archive, new TestClass(WithMixedProcessDeployments.class));

    // then
    FoxDeploymentConfiguration deploymentConfiguration = producedConfiguration();

    assertThat(deploymentConfiguration.getExtensionArchiveJndiPrefix()).isEqualTo("foo");
  }

  @Test
  public void shouldProperlyFillConfigurationWhenProcessingWar() throws Exception {
    // given
    getManager().getContext(SuiteContext.class).activate();
    fire(new BeforeSuite());
    FoxDynamicDependencyAppender appender = getManager().getExtension(FoxDynamicDependencyAppender.class);

    // when
    Archive<?> archive = ShrinkWrap.create(WebArchive.class, "foo.war");
    appender.process(archive, new TestClass(WithMixedProcessDeployments.class));

    // then
    FoxDeploymentConfiguration deploymentConfiguration = producedConfiguration();

    assertThat(deploymentConfiguration.getExtensionArchiveJndiPrefix()).isEqualTo("foo");
  }

  @Test
  public void shouldProperlyFillConfigurationWhenProcessingEar() throws Exception {
    // given
    getManager().getContext(SuiteContext.class).activate();
    fire(new BeforeSuite());
    FoxDynamicDependencyAppender appender = getManager().getExtension(FoxDynamicDependencyAppender.class);
    String expectedJndiPrefix = "foo/" + suffixRemoved(FoxDynamicDependencyAppender.APPLICATION_EXTENSION_ARCHIVE_NAME);

    // when
    Archive<?> archive = ShrinkWrap.create(EnterpriseArchive.class, "foo.ear");
    appender.process(archive, new TestClass(WithMixedProcessDeployments.class));

    // then
    FoxDeploymentConfiguration deploymentConfiguration = producedConfiguration();

    assertThat(deploymentConfiguration.getExtensionArchiveJndiPrefix()).isEqualTo(expectedJndiPrefix);
  }

  protected FoxDeploymentConfiguration producedConfiguration() {
    return getManager().getContext(SuiteContext.class).getObjectStore().get(FoxDeploymentConfiguration.class);
  }
  
  private String suffixRemoved(String name) {
    int indexOfDot = name.lastIndexOf(".");
    if (indexOfDot != -1) {
      return name.substring(0, indexOfDot);
    } else {
      return name;
    }
  }
}
