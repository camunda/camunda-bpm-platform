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
package org.camunda.bpm.container.impl.jboss.extension.handler;

import org.camunda.bpm.container.impl.jboss.extension.Constants;
import org.camunda.bpm.container.impl.jboss.service.MscExecutorService;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.engine.ProcessEngineException;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.threads.*;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.THREAD_POOL_NAME;


/**
 * Installs the JobExecutor service into the container.
 *
 * @author Christian Lipphardt
 */
public class JobExecutorAdd extends AbstractAddStepHandler {

  public static final JobExecutorAdd INSTANCE = new JobExecutorAdd();

  private JobExecutorAdd() {
    super(Constants.JOB_EXECUTOR_ATTRIBUTES);
  }

  @Override
  protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
          ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
          throws OperationFailedException {

    if (!model.hasDefined(THREAD_POOL_NAME)) {
      throw new ProcessEngineException("Unable to configure threadpool for ContainerJobExecutorService, missing element '" + THREAD_POOL_NAME + "' in JobExecutor configuration.");
    }

    String jobExecutorThreadPoolName = Constants.THREAD_POOL_NAME.resolveModelAttribute(context, model).asString();
    ServiceName jobExecutorThreadPoolServiceName = ServiceNames.forManagedThreadPool(jobExecutorThreadPoolName);

    performRuntimeThreadPool(context, jobExecutorThreadPoolName, jobExecutorThreadPoolServiceName, verificationHandler, newControllers);

    MscExecutorService service = new MscExecutorService();
    ServiceController<MscExecutorService> serviceController = context.getServiceTarget().addService(ServiceNames.forMscExecutorService(), service)
        .addDependency(jobExecutorThreadPoolServiceName, ManagedQueueExecutorService.class, service.getManagedQueueInjector())
        .addListener(verificationHandler)
        .setInitialMode(Mode.ACTIVE)
        .install();

    newControllers.add(serviceController);

  }

  private void performRuntimeThreadPool(OperationContext context, String name, ServiceName jobExecutorThreadPoolServiceName,
      ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
      throws OperationFailedException {

    ServiceTarget serviceTarget = context.getServiceTarget();

    ThreadFactoryService threadFactory = new ThreadFactoryService();
    threadFactory.setThreadGroupName("Camunda BPM " + name);

    final ServiceName threadFactoryServiceName = ThreadsServices.threadFactoryName(name);

    ServiceBuilder<ThreadFactory> factoryBuilder = serviceTarget.addService(threadFactoryServiceName, threadFactory);
    if (verificationHandler != null) {
      factoryBuilder.addListener(verificationHandler);
    }
    if (newControllers != null) {
      newControllers.add(factoryBuilder.install());
    } else {
      factoryBuilder.install();
    }

    final BoundedQueueThreadPoolService service = new BoundedQueueThreadPoolService(3, 19, 3, false, new TimeSpec(TimeUnit.SECONDS, 10), true);

    ServiceBuilder<ManagedQueueExecutorService> builder = serviceTarget.addService(jobExecutorThreadPoolServiceName, service)
        .addDependency(threadFactoryServiceName, ThreadFactory.class, service.getThreadFactoryInjector())
        .setInitialMode(ServiceController.Mode.ACTIVE);
    if (verificationHandler != null) {
      builder.addListener(verificationHandler);
    }
    if (newControllers != null) {
      newControllers.add(builder.install());
    } else {
      builder.install();
    }
  }
}
