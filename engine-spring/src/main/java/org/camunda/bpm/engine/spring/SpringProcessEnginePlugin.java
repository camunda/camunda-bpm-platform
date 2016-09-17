package org.camunda.bpm.engine.spring;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.springframework.beans.factory.BeanNameAware;

/**
 * This is bean-name-aware extension of the {@link AbstractProcessEnginePlugin} allowing anonymous
 * classes get logged correctly when processed.
 */
public class SpringProcessEnginePlugin extends AbstractProcessEnginePlugin implements BeanNameAware {

  protected String beanName;

  @Override
  public void setBeanName(String beanName) {
    this.beanName = beanName;
  }

  @Override
  public String toString() {
    return beanName;
  }
}
