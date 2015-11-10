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
package org.camunda.bpm.container.impl.deployment.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationDeploymentInfo;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.container.impl.deployment.Attachments;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessApplication;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.ServiceTypes;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 * @author Daniel Meyer
 *
 */
public class InjectionUtil {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  public static Method detectAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotationType) {

    Method[] methods = clazz.getMethods();
    for (Method method : methods) {
      for (Annotation annotaiton : method.getAnnotations()) {
        if(annotationType.equals(annotaiton.annotationType())) {
          return method;
        }
      }
    }

    return null;

  }

  public static Object[] resolveInjections(DeploymentOperation operationContext, Method lifecycleMethod) {

    final Type[] parameterTypes = lifecycleMethod.getGenericParameterTypes();
    final List<Object> parameters = new ArrayList<Object>();

    for (Type parameterType : parameterTypes) {

      boolean injectionResolved = false;

      if(parameterType instanceof Class) {

        Class<?> parameterClass = (Class<?>)parameterType;

        // support injection of the default process engine
        if(ProcessEngine.class.isAssignableFrom(parameterClass)) {
          parameters.add(getDefaultProcessEngine(operationContext));
          injectionResolved = true;
        }

        // support injection of the ProcessApplicationInfo
        else if(ProcessApplicationInfo.class.isAssignableFrom(parameterClass)) {
          parameters.add(getProcessApplicationInfo(operationContext));
          injectionResolved = true;
        }

      } else if(parameterType instanceof ParameterizedType) {

        ParameterizedType parameterizedType = (ParameterizedType) parameterType;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        // support injection of List<ProcessEngine>
        if(actualTypeArguments.length==1 && ProcessEngine.class.isAssignableFrom((Class<?>) actualTypeArguments[0])) {
          parameters.add(getProcessEngines(operationContext));
          injectionResolved = true;
        }
      }

      if(!injectionResolved) {
        throw LOG.unsuppoertedParameterType(parameterType);
      }

    }

    return parameters.toArray();
  }

  public static ProcessApplicationInfo getProcessApplicationInfo(DeploymentOperation operationContext) {

    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();
    final AbstractProcessApplication processApplication = operationContext.getAttachment(Attachments.PROCESS_APPLICATION);

    JmxManagedProcessApplication managedPa = serviceContainer.getServiceValue(ServiceTypes.PROCESS_APPLICATION, processApplication.getName());
    return managedPa.getProcessApplicationInfo();
  }

  public static List<ProcessEngine> getProcessEngines(DeploymentOperation operationContext) {

    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();
    final ProcessApplicationInfo processApplicationInfo = getProcessApplicationInfo(operationContext);

    List<ProcessEngine> processEngines = new ArrayList<ProcessEngine>();
    for (ProcessApplicationDeploymentInfo deploymentInfo : processApplicationInfo.getDeploymentInfo()) {
      String processEngineName = deploymentInfo.getProcessEngineName();
      processEngines.add((ProcessEngine) serviceContainer.getServiceValue(ServiceTypes.PROCESS_ENGINE, processEngineName));
    }

    return processEngines;
  }

  public static ProcessEngine getDefaultProcessEngine(DeploymentOperation operationContext) {
    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();
    return serviceContainer.getServiceValue(ServiceTypes.PROCESS_ENGINE, "default");
  }

}
