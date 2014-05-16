package org.camunda.bpm.container.impl.jmx.kernel.util;

import org.camunda.bpm.container.impl.spi.PlatformService;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;

public class TestService implements PlatformService<TestService>, TestServiceMBean {

  public void start(PlatformServiceContainer mBeanServiceContainer) {

  }

  public void stop(PlatformServiceContainer mBeanServiceContainer) {

  }

  public TestService getValue() {
    return this;
  }

}
