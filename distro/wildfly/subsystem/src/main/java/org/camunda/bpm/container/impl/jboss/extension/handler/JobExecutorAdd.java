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
package org.camunda.bpm.container.impl.jboss.extension.handler;

import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.impl.jboss.extension.SubsystemAttributeDefinitons;
import org.camunda.bpm.container.impl.jboss.service.MscExecutorService;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.threads.BoundedQueueThreadPoolService;
import org.jboss.as.threads.ManagedQueueExecutorService;
import org.jboss.as.threads.ThreadFactoryService;
import org.jboss.as.threads.TimeSpec;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * Installs the JobExecutor service into the container.
 *
 * @author Christian Lipphardt
 */
public class JobExecutorAdd extends AbstractAddStepHandler {

    public static final String THREAD_POOL_GRP_NAME = "Camunda BPM ";

    public static final JobExecutorAdd INSTANCE = new JobExecutorAdd();

    @Override
    protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model)
            throws OperationFailedException {

        String jobExecutorThreadPoolName =
                SubsystemAttributeDefinitons.THREAD_POOL_NAME.resolveModelAttribute(context, model).asString();
        ServiceName jobExecutorThreadPoolServiceName = ServiceNames.forManagedThreadPool(jobExecutorThreadPoolName);

        performRuntimeThreadPool(context, model, jobExecutorThreadPoolName, jobExecutorThreadPoolServiceName);

        ServiceBuilder<?> sb = context.getCapabilityServiceTarget().addService();
        Consumer<ExecutorService> provider = sb.provides(ServiceNames.forMscExecutorService());
        Supplier<ManagedQueueExecutorService> supplier = sb.requires(jobExecutorThreadPoolServiceName);
        MscExecutorService service = new MscExecutorService(supplier, provider);
        sb.setInitialMode(Mode.ACTIVE);
        sb.setInstance(service);
        sb.install();
    }

    protected void performRuntimeThreadPool(OperationContext context, ModelNode model, String name,
            ServiceName jobExecutorThreadPoolServiceName) throws OperationFailedException {

        ServiceTarget serviceTarget = context.getCapabilityServiceTarget();

        ThreadFactoryService threadFactory = new ThreadFactoryService();
        threadFactory.setThreadGroupName(THREAD_POOL_GRP_NAME + name);

        ServiceName threadFactoryServiceName = ServiceNames.forThreadFactoryService(name);

        // TODO document the WildFly classes use deprecated API
        serviceTarget.addService(threadFactoryServiceName, threadFactory).install();
//        ServiceBuilder<?> sb = serviceTarget.addService();
//        sb.provides(threadFactoryServiceName);
//        sb.setInstance(threadFactory);
//        sb.install();
        //CAN't cause
//        ERROR: MSC000012: Injection failed for service service org.camunda.bpm.platform.job-executor.job-executor-tp
//        java.lang.IllegalStateException: Service unavailable
//          at org.jboss.msc.service.WritableValueImpl.getValue(WritableValueImpl.java:41)
//          at org.jboss.msc.service.ServiceRegistrationImpl.getValue(ServiceRegistrationImpl.java:161)
//          at org.jboss.msc.service.ServiceControllerImpl.inject(ServiceControllerImpl.java:1397)
//          at org.jboss.msc.service.ServiceControllerImpl.inject(ServiceControllerImpl.java:1383)
//          at org.jboss.msc.service.ServiceControllerImpl$StartTask.execute(ServiceControllerImpl.java:1579)
//          at org.jboss.msc.service.ServiceControllerImpl$ControllerTask.run(ServiceControllerImpl.java:1438)
//          at org.jboss.threads.ContextClassLoaderSavingRunnable.run(ContextClassLoaderSavingRunnable.java:35)
//          at org.jboss.threads.EnhancedQueueExecutor.safeRun(EnhancedQueueExecutor.java:1990)
//          at org.jboss.threads.EnhancedQueueExecutor$ThreadBody.doRunTask(EnhancedQueueExecutor.java:1486)
//          at org.jboss.threads.EnhancedQueueExecutor$ThreadBody.run(EnhancedQueueExecutor.java:1377)
//          at java.base/java.lang.Thread.run(Thread.java:834)
//
//        Feb 08, 2024 3:43:28 PM org.jboss.msc.service.ServiceControllerImpl startFailed
//        ERROR: MSC000001: Failed to start service org.camunda.bpm.platform.job-executor.job-executor-tp
//        org.jboss.msc.service.StartException in service org.camunda.bpm.platform.job-executor.job-executor-tp: Failed to start service
//          at org.jboss.msc.service.ServiceControllerImpl$StartTask.execute(ServiceControllerImpl.java:1609)
//          at org.jboss.msc.service.ServiceControllerImpl$ControllerTask.run(ServiceControllerImpl.java:1438)
//          at org.jboss.threads.ContextClassLoaderSavingRunnable.run(ContextClassLoaderSavingRunnable.java:35)
//          at org.jboss.threads.EnhancedQueueExecutor.safeRun(EnhancedQueueExecutor.java:1990)
//          at org.jboss.threads.EnhancedQueueExecutor$ThreadBody.doRunTask(EnhancedQueueExecutor.java:1486)
//          at org.jboss.threads.EnhancedQueueExecutor$ThreadBody.run(EnhancedQueueExecutor.java:1377)
//          at java.base/java.lang.Thread.run(Thread.java:834)
//        Caused by: java.lang.IllegalStateException: Service unavailable
//          at org.jboss.msc.service.WritableValueImpl.getValue(WritableValueImpl.java:41)
//          at org.jboss.msc.service.ServiceRegistrationImpl.getValue(ServiceRegistrationImpl.java:161)
//          at org.jboss.msc.service.ServiceControllerImpl.inject(ServiceControllerImpl.java:1397)
//          at org.jboss.msc.service.ServiceControllerImpl.inject(ServiceControllerImpl.java:1383)
//          at org.jboss.msc.service.ServiceControllerImpl$StartTask.execute(ServiceControllerImpl.java:1579)
//          ... 6 more

        final BoundedQueueThreadPoolService threadPoolService = new BoundedQueueThreadPoolService(
                SubsystemAttributeDefinitons.CORE_THREADS.resolveModelAttribute(context, model).asInt(),
                SubsystemAttributeDefinitons.MAX_THREADS.resolveModelAttribute(context, model).asInt(),
                SubsystemAttributeDefinitons.QUEUE_LENGTH.resolveModelAttribute(context, model).asInt(), false,
                new TimeSpec(TimeUnit.SECONDS,
                        SubsystemAttributeDefinitons.KEEPALIVE_TIME.resolveModelAttribute(context, model).asInt()),
                SubsystemAttributeDefinitons.ALLOW_CORE_TIMEOUT.resolveModelAttribute(context, model).asBoolean());

        serviceTarget.addService(jobExecutorThreadPoolServiceName, threadPoolService)
                .addDependency(threadFactoryServiceName, ThreadFactory.class,
                        threadPoolService.getThreadFactoryInjector())
                .setInitialMode(ServiceController.Mode.ACTIVE).install();
//        ServiceBuilder<?> sb = serviceTarget.addService();
//        sb.provides(jobExecutorThreadPoolServiceName);
//        sb.requires(threadFactoryServiceName);
//        sb.setInitialMode(ServiceController.Mode.ACTIVE);
//        sb.setInstance(threadPoolService);
//        sb.install();
        //CAN'T cause
//        WFLYCTL0186:   Services which failed to start:      service org.camunda.bpm.platform.job-executor.job-executor-tp: Failed to start service
    }

}
