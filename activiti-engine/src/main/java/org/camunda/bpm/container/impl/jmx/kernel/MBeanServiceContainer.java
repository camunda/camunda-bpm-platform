/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jmx.kernel;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.activiti.engine.ActivitiException;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation.MBeanDeploymentOperationBuilder;

import com.camunda.fox.platform.FoxPlatformException;

/**
 * <p>A simple Service Container that delegates to the {@link MBeanServer}.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class MBeanServiceContainer {

  protected MBeanServer mBeanServer;
  
  protected Map<ObjectName, MbeanService<?>> servicesByName = new ConcurrentHashMap<ObjectName, MbeanService<?>>();

  /** set if the current thread is performing a composite deployment operation */
  protected ThreadLocal<MBeanDeploymentOperation> operationContext = new ThreadLocal<MBeanDeploymentOperation>();
  
  public synchronized <S> void startService(ServiceType serviceType, String localName, MbeanService<S> service) {
    
    ObjectName serviceName = serviceType.getServiceName(localName);
    startService(serviceName, service);
    
  }
  
  public synchronized <S> void startService(ObjectName serviceName, MbeanService<S> service) {

    if (getService(serviceName) != null) {
      throw new FoxPlatformException("Cannot register service " + serviceName + " with MBeans Container, service with same name already registered.");
    }

    final MBeanServer beanServer = getmBeanServer();
    // call the service-provided start behavior
    service.start(this);

    try {
      beanServer.registerMBean(service, serviceName);
      servicesByName.put(serviceName, service);

      MBeanDeploymentOperation currentOperationContext = operationContext.get();
      if (currentOperationContext != null) {
        currentOperationContext.serviceAdded(serviceName);
      }

    } catch (Exception e) {
      throw new FoxPlatformException("Could not register service " + serviceName + " with the MBean server", e);

    }

  }
  
  public synchronized void stopService(ServiceType serviceType, String localName) {
    
    ObjectName serviceName = serviceType.getServiceName(localName);   
    stopService(serviceName);

  }
  
  public synchronized void stopService(ObjectName serviceName) {
    
    final MBeanServer mBeanServer = getmBeanServer();        
    final MbeanService<Object> service = getService(serviceName);
    
    if(service == null) {
      throw new FoxPlatformException("Cannot stop service "+serviceName+": no such service registered.");      
    }
    
    try {
      // call the service-provided stop behavior
      service.stop(this);
    } finally {
      // always unregister, even if the stop method throws an exception.
      try {
        mBeanServer.unregisterMBean(serviceName);
        servicesByName.remove(serviceName);

      } catch (Throwable t) {
        throw new FoxPlatformException("Exception while unregistering " + serviceName.getCanonicalName() + " with the MBeanServer", t);
      }
    }
    
  }
  
  public MBeanDeploymentOperationBuilder createDeploymentOperation(String name) {
    return new MBeanDeploymentOperation.MBeanDeploymentOperationBuilder(this, name);
  }
  
  public MBeanDeploymentOperationBuilder createUndeploymentOperation(String name) {
    MBeanDeploymentOperationBuilder builder = new MBeanDeploymentOperation.MBeanDeploymentOperationBuilder(this, name);
    builder.setUndeploymentOperation();
    return builder;
  }
  
  protected void executeDeploymentOperation(MBeanDeploymentOperation operation) {

    if (operationContext.get() != null) {
      throw new ActivitiException("Cannot start deployment operation " + operation.name + "', already performing operation '" + operationContext.get().name);
    }

    try {
      operationContext.set(operation);
      // execute the operation
      operation.execute();
    } finally {
      operationContext.remove();
    }
  }
  
  /**
   * get a specific service by name or null if no such Service exists. 
   * 
   */
  public <S> MbeanService<S> getService(ServiceType type, String localName) {
    ObjectName serviceName = type.getServiceName(localName);
    return getService(serviceName);
  }
  
  /**
   * get a specific service by name or null if no such Service exists. 
   * 
   */
  @SuppressWarnings("unchecked")
  protected <S> MbeanService<S> getService(ObjectName name) {
    return (MbeanService<S>) servicesByName.get(name);
  }
  
  /**
   * get the service value for a specific service by name or null if no such
   * Service exists.
   * 
   */
  protected <S> S getServiceValue(ObjectName name) {
    MbeanService<S> service = getService(name);
    if(service != null) {
      return service.getValue();
    } else {
      return null;
    }
  }
  
  /**
   * get the service value for a specific service by name or null if no such
   * Service exists.
   * 
   */
  public <S> S getServiceValue(ServiceType type, String localName) {
    ObjectName serviceName = type.getServiceName(localName);
    return getServiceValue(serviceName);
  }
  
  /**
   * @return all services for a specific {@link ServiceType}
   */
  @SuppressWarnings("unchecked")
  public <S> List<MbeanService<S>> getServicesByType(ServiceType type) {
    
    // query the MBeanServer for all services of the given type
    Set<ObjectName> serviceNames = getServiceNames(type);
    
    List<MbeanService<S>> res = new ArrayList<MbeanService<S>>();
    for (ObjectName serviceName : serviceNames) {
      res.add((MbeanService<S>) servicesByName.get(serviceName));
    }
    
    return res;
  }

  /**
   * @return the service names ( {@link ObjectName} ) for all services for a given type
   */
  public Set<ObjectName> getServiceNames(ServiceType type) {
    return mBeanServer.queryNames(type.getTypeName(),null);
  }
    
  /**
   * @return the values of all services for a specific {@link ServiceType}
   */
  @SuppressWarnings("unchecked")
  public <S> List<S> getServiceValuesByType(ServiceType type) {

    // query the MBeanServer for all services of the given type
    Set<ObjectName> serviceNames = getServiceNames(type);

    List<S> res = new ArrayList<S>();
    for (ObjectName serviceName : serviceNames) {
      MbeanService<S> mbeanService = (MbeanService<S>) servicesByName.get(serviceName);
      if (mbeanService != null) {
        res.add(mbeanService.getValue());
      }
    }

    return res;
  }
  
  public MBeanServer getmBeanServer() {
    if (mBeanServer == null) {
      synchronized (this) {
        if (mBeanServer == null) {
          mBeanServer = createOrLookupMbeanServer();
        }
      }
    }
    return mBeanServer;
  }
  
  public void setmBeanServer(MBeanServer mBeanServer) {
    this.mBeanServer = mBeanServer;
  }
  
  protected MBeanServer createOrLookupMbeanServer() {    
    return ManagementFactory.getPlatformMBeanServer();
  }  
    
  /**
   * A ServiceType is a collection of services that share a common name prefix.
   */
  public interface ServiceType {
    
    /** get the full {@link ObjectName} for the service */
    public ObjectName getServiceName(String localName);
    
    /**
     * Returns a wildcard {@link ObjectName} name that allows to query the
     * {@link MBeanServer} for all services of the type represented by this
     * ServiceType.
     */
    public ObjectName getTypeName();
                
  }

}
