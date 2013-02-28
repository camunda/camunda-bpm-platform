package org.camunda.bpm.container.impl.jmx.kernel.util;

import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanService;

public class TestService extends MBeanService<TestService> implements TestServiceMBean {

  public void start(MBeanServiceContainer mBeanServiceContainer) {

  }

  public void stop(MBeanServiceContainer mBeanServiceContainer) {

  }

  public TestService getValue() {
    return this;
  }

}
