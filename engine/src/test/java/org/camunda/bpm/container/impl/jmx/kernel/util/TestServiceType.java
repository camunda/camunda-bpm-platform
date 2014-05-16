package org.camunda.bpm.container.impl.jmx.kernel.util;

import org.camunda.bpm.container.impl.spi.PlatformServiceContainer.ServiceType;

public enum TestServiceType implements ServiceType {

    TYPE1("test.type1"),
    TYPE2("test.type2");

    protected String serviceRealm;

    private TestServiceType(String serviceRealm) {
      this.serviceRealm = serviceRealm;
    }

    public String getTypeName() {
      return serviceRealm;
    }

}
