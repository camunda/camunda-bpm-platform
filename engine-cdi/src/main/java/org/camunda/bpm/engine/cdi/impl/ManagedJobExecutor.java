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
package org.camunda.bpm.engine.cdi.impl;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.ThreadPoolJobExecutor;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * {@link JobExecutor} implementation that utilises an application server's managed thread pool to acquire and execute jobs.
 */
@ApplicationScoped
public class ManagedJobExecutor extends ThreadPoolJobExecutor {

    @Resource
    private ManagedExecutorService managedExecutorService;

    @Resource
    private ManagedThreadFactory managedThreadFactory;

    /**
     * Constructs a new ManagedJobExecutor.
     */
    public ManagedJobExecutor() {
    }

    /**
     * Constructs a new ManagedJobExecutor with the provided {@link ManagedExecutorService} and {@link ManagedThreadFactory}.
     */
    public ManagedJobExecutor(final ManagedExecutorService managedExecutorService, final ManagedThreadFactory managedThreadFactory) {
        this.managedExecutorService = managedExecutorService;
        this.managedThreadFactory = managedThreadFactory;
    }

    @Override
    public void executeJobs(List<String> jobIds, ProcessEngineImpl processEngine) {
        try {
            managedExecutorService.execute(getExecuteJobsRunnable(jobIds, processEngine));
        } catch (RejectedExecutionException e) {
            logRejectedExecution(processEngine, jobIds.size());
            rejectedJobsHandler.jobsRejected(jobIds, processEngine, this);
        }
    }

    @Override
    protected void startJobAcquisitionThread() {
        if (jobAcquisitionThread == null) {
            jobAcquisitionThread = managedThreadFactory.newThread(acquireJobsRunnable);
            jobAcquisitionThread.start();
        }
    }
}
