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
package org.camunda.bpm.engine.cdi.test.impl;

import com.google.common.collect.Lists;
import org.camunda.bpm.engine.cdi.impl.ManagedJobExecutor;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.jobexecutor.AcquireJobsRunnable;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobsRunnable;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedThreadFactory;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class ManagedJobExecutorTest {

    @Test
    public void testUsesManagedExecutorService() {
        final ManagedExecutorService managedExecutorServiceMock = Mockito.mock(ManagedExecutorService.class);
        final ManagedThreadFactory managedThreadFactoryMock = Mockito.mock(ManagedThreadFactory.class);
        final ProcessEngineImpl processEngineImplMock = Mockito.mock(ProcessEngineImpl.class, Answers.RETURNS_DEEP_STUBS.get());
        final JobExecutor jobExecutor = new ManagedJobExecutor(managedExecutorServiceMock, managedThreadFactoryMock);

        when(processEngineImplMock.getProcessEngineConfiguration().getJobExecutor()).thenReturn(jobExecutor);

        jobExecutor.executeJobs(Lists.newArrayList(UUID.randomUUID().toString()), processEngineImplMock);
        verify(managedExecutorServiceMock, times(1)).execute(isA(ExecuteJobsRunnable.class));
    }

    @Test
    public void testUsesManagedThreadFactory() {
        final ManagedExecutorService managedExecutorServiceMock = Mockito.mock(ManagedExecutorService.class);
        final ManagedThreadFactory managedThreadFactoryMock = Mockito.mock(ManagedThreadFactory.class);
        final Thread managedThreadMock = mock(Thread.class);
        final JobExecutor jobExecutor = new ManagedJobExecutor(managedExecutorServiceMock, managedThreadFactoryMock);

        when(managedThreadFactoryMock.newThread(isA(AcquireJobsRunnable.class))).thenReturn(managedThreadMock);

        jobExecutor.start();
        verify(managedThreadFactoryMock, times(1)).newThread(isA(AcquireJobsRunnable.class));
    }

}
