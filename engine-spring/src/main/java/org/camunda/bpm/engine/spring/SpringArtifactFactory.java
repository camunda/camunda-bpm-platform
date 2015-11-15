package org.camunda.bpm.engine.spring;

import org.camunda.bpm.engine.ArtifactFactory;
import org.camunda.bpm.engine.impl.DefaultArtifactFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * Uses applicationContext to get an instance of the clazz requested. If the bean can not be initialized, it falls back to
 * the DefaultArtifactFactory.
 */
public class SpringArtifactFactory implements ArtifactFactory {

  private final DefaultArtifactFactory defaultArtifactFactory = new DefaultArtifactFactory();

  private final ApplicationContext applicationContext;

  public SpringArtifactFactory(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public <T> T getArtifact(Class<T> clazz) {
    T artifact = null;

    try {
      artifact = applicationContext.getBean(clazz);
    } catch (NoSuchBeanDefinitionException e) {
      // ignore, see default factory
    }

    if (artifact == null) {
      artifact = defaultArtifactFactory.getArtifact(clazz);
    }

    return artifact;
  }


}
