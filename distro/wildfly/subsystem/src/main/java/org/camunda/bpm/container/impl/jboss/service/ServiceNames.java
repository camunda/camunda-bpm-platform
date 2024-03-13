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

import org.jboss.as.threads.ThreadsServices;
import org.jboss.msc.service.ServiceName;

/**
 * <p>All ServiceName references run through here.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ServiceNames {

  private final static ServiceName BPM_PLATFORM = ServiceName.of("org", "camunda", "bpm", "platform");

  private final static ServiceName PROCESS_ENGINE = BPM_PLATFORM.append("process-engine");
  private final static ServiceName JOB_EXECUTOR = BPM_PLATFORM.append("job-executor");
  private final static ServiceName DEFAULT_PROCESS_ENGINE = PROCESS_ENGINE.append("default");

  private final static ServiceName MSC_RUNTIME_CONTAINER_DELEGATE = BPM_PLATFORM.append("runtime-container");

  private final static ServiceName PROCESS_APPLICATION = BPM_PLATFORM.append("process-application");

  private final static ServiceName PROCESS_APPLICATION_MODULE = BPM_PLATFORM.append("process-application-module");

  private final static ServiceName BPM_PLATFORM_PLUGINS = BPM_PLATFORM.append("bpm-platform-plugins");

  /**
   * Returns the service name for a {@link MscManagedProcessEngine}.
   *
   * @param the
   *          name of the process engine
   * @return the composed service name
   */
  public static ServiceName forManagedProcessEngine(String processEngineName) {
    return PROCESS_ENGINE.append(processEngineName);
  }

  /**
   * @return the {@link ServiceName} for the default
   *         {@link MscManagedProcessEngine}. This is a constant name since
   *         there can only be one default process engine.
   */
  public static ServiceName forDefaultProcessEngine() {
    return DEFAULT_PROCESS_ENGINE;
  }

  /**
   * @return the {@link ServiceName} for the {@link MscRuntimeContainerDelegate}
   */
  public static ServiceName forMscRuntimeContainerDelegate() {
    return MSC_RUNTIME_CONTAINER_DELEGATE;
  }

  /**
   * @return the {@link ServiceName} that is the longest common prefix of all
   * ServiceNames used for {@link MscManagedProcessEngine}.
   */
  public static ServiceName forManagedProcessEngines() {
    return PROCESS_ENGINE;
  }

  /**
   * @return the {@link ServiceName} that is the longest common prefix of all
   * ServiceNames used for {@link MscManagedProcessApplication}.
   */
  public static ServiceName forManagedProcessApplications() {
    return PROCESS_APPLICATION;
  }

  /**
   * @param applicationName
   * @return the name to be used for an {@link MscManagedProcessApplication} service.
   */
  public static ServiceName forManagedProcessApplication(String applicationName) {
    return PROCESS_APPLICATION.append(applicationName);
  }

  public static ServiceName forProcessApplicationModuleService(String moduleName) {
    return PROCESS_APPLICATION_MODULE.append(moduleName);
  }

  /**
   * @param applicationName
   * @return the name to be used for an {@link MscManagedProcessApplication} service.
   */
  public static ServiceName forProcessApplicationStartService(String moduleName) {
    return PROCESS_APPLICATION_MODULE.append(moduleName).append("START");
  }

  /**
   * <p>Returns the name for a {@link ProcessApplicationDeploymentService} given
   * the name of the deployment unit and the name of the deployment.</p>
   *
   * @param processApplicationName
   * @param deploymentId
   */
  public static ServiceName forProcessApplicationDeploymentService(String moduleName, String deploymentName) {
    return PROCESS_APPLICATION_MODULE.append(moduleName).append("DEPLOY").append(deploymentName);
  }

  public static ServiceName forNoViewProcessApplicationStartService(String moduleName) {
    return PROCESS_APPLICATION_MODULE.append(moduleName).append("NO_VIEW");
  }

  /**
   * @return the {@link ServiceName} of the {@link MscExecutorService}.
   */
  public static ServiceName forMscExecutorService() {
    return BPM_PLATFORM.append("executor-service");
  }

  /**
   * @return the {@link ServiceName} of the {@link MscRuntimeContainerJobExecutor}
   */
  public static ServiceName forMscRuntimeContainerJobExecutorService(String jobExecutorName) {
    return JOB_EXECUTOR.append(jobExecutorName);
  }

  /**
   * @return the {@link ServiceName} of the {@link MscBpmPlatformPlugins}
   */
  public static ServiceName forBpmPlatformPlugins() {
    return BPM_PLATFORM_PLUGINS;
  }

  /**
   * @return the {@link ServiceName} of the {@link ProcessApplicationStopService}
   */
  public static ServiceName forProcessApplicationStopService(String moduleName) {
    return PROCESS_APPLICATION_MODULE.append(moduleName).append("STOP");
  }

  /**
   * @return the {@link ServiceName} of the {@link org.jboss.as.threads.BoundedQueueThreadPoolService}
   */
  public static ServiceName forManagedThreadPool(String threadPoolName) {
    return JOB_EXECUTOR.append(threadPoolName);
  }

  /**
   * @return the {@link ServiceName} of the {@link org.jboss.as.threads.ThreadFactoryService}
   */
  public static ServiceName forThreadFactoryService(String threadFactoryName) {
    return ThreadsServices.threadFactoryName(threadFactoryName);
  }

}
