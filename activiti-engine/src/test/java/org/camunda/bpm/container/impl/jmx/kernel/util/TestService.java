package org.camunda.bpm.container.impl.jmx.kernel.util;

import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.kernel.MbeanService;

public class TestService extends MbeanService<TestService> implements TestServiceMBean {

  public void start(MBeanServiceContainer mBeanServiceContainer) {

  }

  public void stop(MBeanServiceContainer mBeanServiceContainer) {

  }

  public TestService getValue() {
    return this;
  }

}
