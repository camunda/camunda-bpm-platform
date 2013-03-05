package org.camunda.bpm.container.impl.jmx.kernel.util;

import org.camunda.bpm.container.impl.jmx.kernel.MBeanService;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;

public class TestService implements MBeanService<TestService>, TestServiceMBean {

  public void start(MBeanServiceContainer mBeanServiceContainer) {

  }

  public void stop(MBeanServiceContainer mBeanServiceContainer) {

  }

  public TestService getValue() {
    return this;
  }

}
