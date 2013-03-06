package org.camunda.bpm.container.impl.jmx.kernel.util;

import javax.management.ObjectName;

import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer.ServiceType;
import org.camunda.bpm.engine.ProcessEngineException;

public enum TestServiceType implements ServiceType {
    
    TYPE1("test.type1"),
    TYPE2("test.type2");

    protected String serviceRealm;

    private TestServiceType(String serviceRealm) {
      this.serviceRealm = serviceRealm;
    }
    
    public ObjectName getServiceName(String localName) {
      try {
        return new ObjectName(serviceRealm+":type=" + localName);
      } catch (Exception e) {
        throw new ProcessEngineException("Could not compose name", e);
      }
    }
    
    public ObjectName getTypeName() {
      try {
        return new ObjectName(serviceRealm + ":type=*");
      } catch (Exception e) {
        throw new ProcessEngineException("Could not compose name", e);
      }
    }
     
}
