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
package org.camunda.bpm.engine.spring.components.jobexecutor;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 *
 * <p>
 * This is a spring based implementation of the {@link JobExecutor} using spring abstraction {@link TaskExecutor}
 * for performing background task execution.
 * </p>
 * <p>
 * The idea behind this implementation is to externalize the configuration of the task executor, so it can leverage to
 * Application servers controller thread pools, for example using the commonj API. The use of unmanaged thread in application servers
 * is discouraged by the Java EE spec.
 * </p>
 *
 * @author Pablo Ganga
 */
public class SpringJobExecutor extends JobExecutor {

	private TaskExecutor taskExecutor;

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	/**
	 * Required spring injected {@link TaskExecutor}} implementation that will be used to execute runnable jobs.
	 *
	 * @param taskExecutor
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public void executeJobs(List<String> jobIds, ProcessEngineImpl processEngine) {
	  try {
      taskExecutor.execute(getExecuteJobsRunnable(jobIds, processEngine));
    } catch (RejectedExecutionException e) {

      logRejectedExecution(processEngine, jobIds.size());
      rejectedJobsHandler.jobsRejected(jobIds, processEngine, this);
    }
	}

	@Override
	protected void startExecutingJobs() {
		startJobAcquisitionThread();
	}

	@Override
	protected void stopExecutingJobs() {
		stopJobAcquisitionThread();
	}
}
