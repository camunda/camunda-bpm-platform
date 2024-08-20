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
package org.camunda.bpm.container.impl.ejb;

import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.container.impl.RuntimeContainerDelegateImpl;
import org.camunda.bpm.container.impl.deployment.DiscoverBpmPlatformPluginsStep;
import org.camunda.bpm.container.impl.deployment.PlatformXmlStartProcessEnginesStep;
import org.camunda.bpm.container.impl.deployment.StopProcessApplicationsStep;
import org.camunda.bpm.container.impl.deployment.StopProcessEnginesStep;
import org.camunda.bpm.container.impl.deployment.UnregisterBpmPlatformPluginsStep;
import org.camunda.bpm.container.impl.deployment.jobexecutor.StartJobExecutorStep;
import org.camunda.bpm.container.impl.deployment.jobexecutor.StopJobExecutorStep;
import org.camunda.bpm.container.impl.ejb.deployment.EjbJarParsePlatformXmlStep;
import org.camunda.bpm.container.impl.ejb.deployment.StartJcaExecutorServiceStep;
import org.camunda.bpm.container.impl.ejb.deployment.StopJcaExecutorServiceStep;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p>Bootstrap for the Camunda Platform using a singleton EJB</p>
 *
 * @author Daniel Meyer
 */
@Startup
@Singleton(name="BpmPlatformBootstrap")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class EjbBpmPlatformBootstrap {

  final private static Logger LOGGER = Logger.getLogger(EjbBpmPlatformBootstrap.class.getName());

  @EJB
  protected ExecutorService executorServiceBean;

  protected ProcessEngineService processEngineService;
  protected ProcessApplicationService processApplicationService;

  @PostConstruct
  protected void start() {

    final RuntimeContainerDelegateImpl containerDelegate = getContainerDelegate();

    containerDelegate.getServiceContainer().createDeploymentOperation("deploying Camunda Platform")
      .addStep(new EjbJarParsePlatformXmlStep())
      .addStep(new DiscoverBpmPlatformPluginsStep())
      .addStep(new StartJcaExecutorServiceStep(executorServiceBean))
      .addStep(new StartJobExecutorStep())
      .addStep(new PlatformXmlStartProcessEnginesStep())
      .execute();

    processEngineService = containerDelegate.getProcessEngineService();
    processApplicationService = containerDelegate.getProcessApplicationService();

    LOGGER.log(Level.INFO, "Camunda Platform started successfully.");
  }

  @PreDestroy
  protected void stop() {

    final RuntimeContainerDelegateImpl containerDelegate = getContainerDelegate();

    containerDelegate.getServiceContainer().createUndeploymentOperation("undeploying Camunda Platform")
      .addStep(new StopProcessApplicationsStep())
      .addStep(new StopProcessEnginesStep())
      .addStep(new StopJobExecutorStep())
      .addStep(new StopJcaExecutorServiceStep())
      .addStep(new UnregisterBpmPlatformPluginsStep())
      .execute();

    LOGGER.log(Level.INFO, "Camunda Platform stopped.");

  }

  protected RuntimeContainerDelegateImpl getContainerDelegate() {
    return (RuntimeContainerDelegateImpl) RuntimeContainerDelegate.INSTANCE.get();
  }

  // getters //////////////////////////////////////////////

  public ProcessEngineService getProcessEngineService() {
    return processEngineService;
  }

  public ProcessApplicationService getProcessApplicationService() {
    return processApplicationService;
  }

}
