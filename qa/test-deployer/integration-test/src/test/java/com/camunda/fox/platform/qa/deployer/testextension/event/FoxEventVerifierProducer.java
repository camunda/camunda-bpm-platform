package com.camunda.fox.platform.qa.deployer.testextension.event;

import java.lang.annotation.Annotation;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class FoxEventVerifierProducer implements ResourceProvider {

  @Inject
  private Instance<FoxEventVerifier> foxEventVerifier;
  
  public boolean canProvide(Class<?> type) {
    return FoxEventVerifier.class.isAssignableFrom(type);
  }

  public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
    return foxEventVerifier.get();
  }
}
