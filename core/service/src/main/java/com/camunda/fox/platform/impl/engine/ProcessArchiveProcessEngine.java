/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.impl.engine;

import java.lang.reflect.Proxy;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;

import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.impl.context.ProcessArchiveContextInvocationHandler;

/**
 * <p>Implementation of the activiti process engine interface which exposes proxies
 * of the activiti services.</p>
 * 
 * <p>Each process archive receives a different instance of this class</p>
 * 
 * @author Daniel Meyer
 */
public class ProcessArchiveProcessEngine implements ProcessEngine {

  private final ProcessArchiveContext processArchiveContext;

  // the actual engine
  private final ProcessEngine delegateEngine;
    
  // proxies
  private final RuntimeService runtimeServiceProxy;
  private final RepositoryService repositoryServiceProxy;
  private final FormService formServiceProxy;
  private final TaskService taskServiceProxy;
  private final HistoryService historyServiceProxy;
  private final IdentityService identityServiceProxy;
  private final ManagementService managementServiceProxy;

  public ProcessArchiveProcessEngine(ProcessArchiveContext processArchiveContext, ProcessEngine delegateEngine) {
    this.processArchiveContext = processArchiveContext;
    this.delegateEngine = delegateEngine;
    this.runtimeServiceProxy = generateProxy(RuntimeService.class, delegateEngine.getRuntimeService());
    this.repositoryServiceProxy = generateProxy(RepositoryService.class, delegateEngine.getRepositoryService());
    this.formServiceProxy = generateProxy(FormService.class, delegateEngine.getFormService());
    this.taskServiceProxy = generateProxy(TaskService.class, delegateEngine.getTaskService());
    this.historyServiceProxy = generateProxy(HistoryService.class, delegateEngine.getHistoryService());
    this.identityServiceProxy = generateProxy(IdentityService.class, delegateEngine.getIdentityService());
    this.managementServiceProxy = generateProxy(ManagementService.class, delegateEngine.getManagementService());

  }

  @SuppressWarnings("unchecked")
  protected <T> T generateProxy(Class<T> iface, T delegate) {
    // we use an "ordinary" java proxy:
    return (T) Proxy.newProxyInstance(
            ProcessArchiveProcessEngine.class.getClassLoader(), 
            new Class< ? >[] { iface }, 
            new ProcessArchiveContextInvocationHandler(processArchiveContext, delegate)
    );
  }

  @Override
  public String getName() {
    return delegateEngine.getName();
  }

  @Override
  public RepositoryService getRepositoryService() {
    return repositoryServiceProxy;
  }

  @Override
  public RuntimeService getRuntimeService() {
    return runtimeServiceProxy;
  }

  @Override
  public FormService getFormService() {
    return formServiceProxy;
  }

  @Override
  public TaskService getTaskService() {
    return taskServiceProxy;
  }

  @Override
  public HistoryService getHistoryService() {
    return historyServiceProxy;
  }

  @Override
  public IdentityService getIdentityService() {
    return identityServiceProxy;
  }

  @Override
  public ManagementService getManagementService() {
    return managementServiceProxy;
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException("The container-managed process engine cannot be closed from a process archive.");
  }

}
