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
package org.camunda.bpm.container.impl.jmx;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperation.DeploymentOperationBuilder;
import org.camunda.bpm.container.impl.spi.PlatformService;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * <p>A simple Service Container that delegates to the JVM's {@link MBeanServer}.</p>
 *
 * @author Daniel Meyer
 *
 */
public class MBeanServiceContainer implements PlatformServiceContainer {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  protected MBeanServer mBeanServer;

  protected Map<ObjectName, PlatformService<?>> servicesByName = new ConcurrentHashMap<ObjectName, PlatformService<?>>();

  /** set if the current thread is performing a composite deployment operation */
  protected ThreadLocal<Stack<DeploymentOperation>> activeDeploymentOperations = new ThreadLocal<Stack<DeploymentOperation>>();

  public final static String SERVICE_NAME_EXECUTOR = "executor-service";

  public synchronized <S> void startService(ServiceType serviceType, String localName, PlatformService<S> service) {

    String serviceName = composeLocalName(serviceType, localName);
    startService(serviceName, service);

  }

  public synchronized <S> void startService(String name, PlatformService<S> service) {

    ObjectName serviceName = getObjectName(name);

    if (getService(serviceName) != null) {
      throw new ProcessEngineException("Cannot register service " + serviceName + " with MBeans Container, service with same name already registered.");
    }

    final MBeanServer beanServer = getmBeanServer();
    // call the service-provided start behavior
    service.start(this);

    try {
      beanServer.registerMBean(service, serviceName);
      servicesByName.put(serviceName, service);

      Stack<DeploymentOperation> currentOperationContext = activeDeploymentOperations.get();
      if (currentOperationContext != null) {
        currentOperationContext.peek().serviceAdded(name);
      }

    }
    catch (Exception e) {
      throw LOG.cannotRegisterService(serviceName, e);
    }
  }

  public static ObjectName getObjectName(String serviceName) {
    try {
      return new ObjectName(serviceName);
    }
    catch(Exception e) {
      throw LOG.cannotComposeNameFor(serviceName, e);
    }
  }

  public static String composeLocalName(ServiceType type, String localName) {
    return type.getTypeName() + ":type=" + localName;
  }

  public synchronized void stopService(ServiceType serviceType, String localName) {
    String globalName = composeLocalName(serviceType, localName);
    stopService(globalName);

  }

  public synchronized void stopService(String name) {

    final MBeanServer mBeanServer = getmBeanServer();

    ObjectName serviceName = getObjectName(name);

    final PlatformService<Object> service = getService(serviceName);

    ensureNotNull("Cannot stop service " + serviceName + ": no such service registered", "service", service);

    try {
      // call the service-provided stop behavior
      service.stop(this);
    } finally {
      // always unregister, even if the stop method throws an exception.
      try {
        mBeanServer.unregisterMBean(serviceName);
        servicesByName.remove(serviceName);
      }
      catch (Throwable t) {
        throw LOG.exceptionWhileUnregisteringService(serviceName.getCanonicalName(), t);
      }
    }

  }

  public DeploymentOperationBuilder createDeploymentOperation(String name) {
    return new DeploymentOperation.DeploymentOperationBuilder(this, name);
  }

  public DeploymentOperationBuilder createUndeploymentOperation(String name) {
    DeploymentOperationBuilder builder = new DeploymentOperation.DeploymentOperationBuilder(this, name);
    builder.setUndeploymentOperation();
    return builder;
  }

  public void executeDeploymentOperation(DeploymentOperation operation) {

    Stack<DeploymentOperation> currentOperationContext = activeDeploymentOperations.get();
    if(currentOperationContext == null) {
      currentOperationContext = new Stack<DeploymentOperation>();
      activeDeploymentOperations.set(currentOperationContext);
    }

    try {
      currentOperationContext.push(operation);
      // execute the operation
      operation.execute();
    }
    finally {
      currentOperationContext.pop();
      if(currentOperationContext.isEmpty()) {
        activeDeploymentOperations.remove();
      }
    }
  }

  /**
   * get a specific service by name or null if no such Service exists.
   *
   */
  public <S> S getService(ServiceType type, String localName) {
    String globalName = composeLocalName(type, localName);
    ObjectName serviceName = getObjectName(globalName);
    return getService(serviceName);
  }

  /**
   * get a specific service by name or null if no such Service exists.
   *
   */
  @SuppressWarnings("unchecked")
  public <S> S getService(ObjectName name) {
    return (S) servicesByName.get(name);
  }

  /**
   * get the service value for a specific service by name or null if no such
   * Service exists.
   *
   */
  public <S> S getServiceValue(ObjectName name) {
    PlatformService<S> service = getService(name);
    if(service != null) {
      return service.getValue();
    }
    else {
      return null;
    }
  }

  /**
   * get the service value for a specific service by name or null if no such
   * Service exists.
   *
   */
  public <S> S getServiceValue(ServiceType type, String localName) {
    String globalName = composeLocalName(type, localName);
    ObjectName serviceName = getObjectName(globalName);
    return getServiceValue(serviceName);
  }

  /**
   * @return all services for a specific {@link ServiceType}
   */
  @SuppressWarnings("unchecked")
  public <S> List<PlatformService<S>> getServicesByType(ServiceType type) {

    // query the MBeanServer for all services of the given type
    Set<String> serviceNames = getServiceNames(type);

    List<PlatformService<S>> res = new ArrayList<PlatformService<S>>();
    for (String serviceName : serviceNames) {
      res.add((PlatformService<S>) servicesByName.get(getObjectName(serviceName)));
    }

    return res;
  }

  /**
   * @return the service names ( {@link ObjectName} ) for all services for a given type
   */
  public Set<String> getServiceNames(ServiceType type) {
    String typeName = composeLocalName(type, "*");
    ObjectName typeObjectName = getObjectName(typeName);
    Set<ObjectName> resultNames = getmBeanServer().queryNames(typeObjectName, null);
    Set<String> result= new HashSet<String>();
    for (ObjectName objectName : resultNames) {
      result.add(objectName.toString());
    }
    return result;
  }

  /**
   * @return the values of all services for a specific {@link ServiceType}
   */
  @SuppressWarnings("unchecked")
  public <S> List<S> getServiceValuesByType(ServiceType type) {

    // query the MBeanServer for all services of the given type
    Set<String> serviceNames = getServiceNames(type);

    List<S> res = new ArrayList<S>();
    for (String serviceName : serviceNames) {
      PlatformService<S> BpmPlatformService = (PlatformService<S>) servicesByName.get(getObjectName(serviceName));
      if (BpmPlatformService != null) {
        res.add(BpmPlatformService.getValue());
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

}
