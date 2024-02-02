/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jboss.service;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.application.ProcessApplicationDeploymentInfo;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.impl.ProcessApplicationDeploymentInfoImpl;
import org.camunda.bpm.application.impl.ProcessApplicationInfoImpl;
import org.camunda.bpm.container.impl.deployment.util.InjectionUtil;
import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugin;
import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugins;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.jboss.as.ee.component.ComponentView;
import org.jboss.as.naming.ManagedReference;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.MethodInfo;
import org.jboss.modules.Module;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * <p>This service is responsible for starting the {@link MscManagedProcessApplication} service.</p>
 *
 * <p>We need this as an extra step since we need a declarative dependency on the
 * ProcessApplicationComponent in order to call the getName() method on the ProcessApplication.
 * The name of the process application is subsequently used for composing the name of the
 * {@link MscManagedProcessApplication} service which means that it must be available when
 * registering the service.</p>
 *
 * <p>This service depends on all {@link ProcessApplicationDeploymentService} instances started for the
 * process application. Thus, when this service is started, it is guaranteed that all process application
 * deployments have completed successfully.</p>
 *
 * <p>This service creates the {@link ProcessApplicationInfo} object and passes it to the
 * {@link MscManagedProcessApplication} service.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationStartService implements Service<ProcessApplicationStartService> {

  private final static Logger LOGGER = Logger.getLogger(ProcessApplicationStartService.class.getName());

  /** the names of the deployment services we depend on; those must be added as
   * declarative dependencies when the service is installed. */
  protected final Collection<ServiceName> deploymentServiceNames;

  // for view-exposing ProcessApplicationComponents
  protected InjectedValue<ComponentView> paComponentViewInjector = new InjectedValue<ComponentView>();
  protected InjectedValue<ProcessApplicationInterface> noViewProcessApplication = new InjectedValue<ProcessApplicationInterface>();

  /** injector for the default process engine */
  protected InjectedValue<ProcessEngine> defaultProcessEngineInjector = new InjectedValue<ProcessEngine>();

  protected InjectedValue<BpmPlatformPlugins> platformPluginsInjector = new InjectedValue<BpmPlatformPlugins>();

  protected AnnotationInstance preUndeployDescription;
  protected AnnotationInstance postDeployDescription;

  protected ProcessApplicationInfoImpl processApplicationInfo;
  protected HashSet<ProcessEngine> referencedProcessEngines;

  protected Module paModule;

  public ProcessApplicationStartService(Collection<ServiceName> deploymentServiceNames, AnnotationInstance postDeployDescription, AnnotationInstance preUndeployDescription, Module paModule) {
    this.deploymentServiceNames = deploymentServiceNames;
    this.postDeployDescription = postDeployDescription;
    this.preUndeployDescription = preUndeployDescription;
    this.paModule = paModule;
  }

  @Override
  public void start(StartContext context) throws StartException {

    ManagedReference reference = null;
    try {

      // get the process application component
      ProcessApplicationInterface processApplication = null;
      ComponentView componentView = paComponentViewInjector.getOptionalValue();
      if(componentView != null) {
        reference = componentView.createInstance();
        processApplication = (ProcessApplicationInterface) reference.getInstance();
      } else {
        processApplication = noViewProcessApplication.getValue();
      }

      // create & populate the process application info object
      processApplicationInfo = new ProcessApplicationInfoImpl();
      processApplicationInfo.setName(processApplication.getName());
      processApplicationInfo.setProperties(processApplication.getProperties());

      referencedProcessEngines = new HashSet<ProcessEngine>();
      List<ProcessApplicationDeploymentInfo> deploymentInfos = new ArrayList<ProcessApplicationDeploymentInfo>();

      for (ServiceName deploymentServiceName : deploymentServiceNames) {

        ProcessApplicationDeploymentService value = getDeploymentService(context, deploymentServiceName);
        referencedProcessEngines.add(value.getProcessEngineInjector().getValue());

        ProcessApplicationDeployment deployment = value.getDeployment();
        if(deployment != null) {
          for (String deploymentId : deployment.getProcessApplicationRegistration().getDeploymentIds()) {
            ProcessApplicationDeploymentInfoImpl deploymentInfo = new ProcessApplicationDeploymentInfoImpl();
            deploymentInfo.setDeploymentId(deploymentId);
            deploymentInfo.setProcessEngineName(value.getProcessEngineName());
            deploymentInfos.add(deploymentInfo);
          }
        }

      }
      processApplicationInfo.setDeploymentInfo(deploymentInfos);

      notifyBpmPlatformPlugins(platformPluginsInjector.getValue(), processApplication);

      if(postDeployDescription != null) {
        invokePostDeploy(processApplication);
      }

      // install the ManagedProcessApplication Service as a child to this service
      // if this service stops (at undeployment) the ManagedProcessApplication service is removed as well.
      ServiceName serviceName = ServiceNames.forManagedProcessApplication(processApplicationInfo.getName());
      MscManagedProcessApplication managedProcessApplication = new MscManagedProcessApplication(processApplicationInfo, processApplication.getReference());
      context.getChildTarget().addService(serviceName, managedProcessApplication).install();

    } catch (StartException e) {
      throw e;

    } catch (Exception e) {
      throw new StartException(e);

    } finally {
      if(reference != null) {
        reference.release();
      }
    }
  }

  protected void notifyBpmPlatformPlugins(BpmPlatformPlugins value, ProcessApplicationInterface processApplication) {
    for (BpmPlatformPlugin plugin : value.getPlugins()) {
      plugin.postProcessApplicationDeploy(processApplication);
    }
  }

  @Override
  public void stop(StopContext context) {

    ManagedReference reference = null;
    try {

      // get the process application component
      ProcessApplicationInterface processApplication = null;
      ComponentView componentView = paComponentViewInjector.getOptionalValue();
      if(componentView != null) {
        reference = componentView.createInstance();
        processApplication = (ProcessApplicationInterface) reference.getInstance();
      } else {
        processApplication = noViewProcessApplication.getValue();
      }

      invokePreUndeploy(processApplication);

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Exception while stopping process application", e);

    } finally {
      if(reference != null) {
        reference.release();
      }
    }

  }

  protected void invokePostDeploy(final ProcessApplicationInterface processApplication) throws ClassNotFoundException, StartException {
    Class<?> paClass = getPaClass(postDeployDescription);
    final Method postDeployMethod = InjectionUtil.detectAnnotatedMethod(paClass, PostDeploy.class);

    if(postDeployMethod != null) {
      try {
        processApplication.execute(new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            postDeployMethod.invoke(processApplication.getRawObject(), getInjections(postDeployMethod));
            return null;
          }
        });
      }catch(Exception e) {
        throw new StartException("Exception while invoking the @PostDeploy method ", e);
      }
    }

  }

  protected void invokePreUndeploy(final ProcessApplicationInterface processApplication) throws ClassNotFoundException {
    if(preUndeployDescription != null) {
      Class<?> paClass = getPaClass(preUndeployDescription);
      final Method preUndeployMethod = InjectionUtil.detectAnnotatedMethod(paClass, PreUndeploy.class);

      if(preUndeployMethod != null) {
        try {
          processApplication.execute(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
              preUndeployMethod.invoke(processApplication.getRawObject(), getInjections(preUndeployMethod));
              return null;
            }
          });
        } catch(Exception e) {
          throw new RuntimeException("Exception while invoking the @PreUndeploy method ", e);
        }
      }
    }

  }

  protected Object[] getInjections(Method lifecycleMethod) {
    final Type[] parameterTypes = lifecycleMethod.getGenericParameterTypes();
    final List<Object> parameters = new ArrayList<Object>();

    for (Type parameterType : parameterTypes) {

      boolean injectionResolved = false;

      if(parameterType instanceof Class) {

        Class<?> parameterClass = (Class<?>)parameterType;

        // support injection of the default process engine, if present
        if(ProcessEngine.class.isAssignableFrom(parameterClass)) {
          parameters.add(defaultProcessEngineInjector.getOptionalValue());
          injectionResolved = true;
        }

        // support injection of the ProcessApplicationInfo
        else if(ProcessApplicationInfo.class.isAssignableFrom(parameterClass)) {
          parameters.add(processApplicationInfo);
          injectionResolved = true;
        }

      } else if(parameterType instanceof ParameterizedType) {

        ParameterizedType parameterizedType = (ParameterizedType) parameterType;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        // support injection of List<ProcessEngine>
        if(actualTypeArguments.length==1 && ProcessEngine.class.isAssignableFrom((Class<?>) actualTypeArguments[0])) {
          parameters.add(new ArrayList<ProcessEngine>(referencedProcessEngines));
          injectionResolved = true;
        }
      }

      if(!injectionResolved) {
        throw new ProcessEngineException("Unsupported parametertype "+parameterType);
      }

    }

    return parameters.toArray();
  }

  protected Class<?> getPaClass(AnnotationInstance annotation) throws ClassNotFoundException {
    String paClassName = ((MethodInfo)annotation.target()).declaringClass().name().toString();
    Class<?> paClass = paModule.getClassLoader().loadClass(paClassName);
    return paClass;
  }

  @SuppressWarnings("unchecked")
  private ProcessApplicationDeploymentService getDeploymentService(StartContext context, ServiceName deploymentServiceName) {
    final ServiceContainer serviceContainer = context.getController().getServiceContainer();
    ServiceController<ProcessApplicationDeploymentService> deploymentService = (ServiceController<ProcessApplicationDeploymentService>) serviceContainer.getRequiredService(deploymentServiceName);
    return deploymentService.getValue();
  }

  public InjectedValue<ProcessApplicationInterface> getNoViewProcessApplication() {
    return noViewProcessApplication;
  }

  public InjectedValue<ComponentView> getPaComponentViewInjector() {
    return paComponentViewInjector;
  }

  @Override
  public ProcessApplicationStartService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  public InjectedValue<ProcessEngine> getDefaultProcessEngineInjector() {
    return defaultProcessEngineInjector;
  }

  public InjectedValue<BpmPlatformPlugins> getPlatformPluginsInjector() {
    return platformPluginsInjector;
  }

}
