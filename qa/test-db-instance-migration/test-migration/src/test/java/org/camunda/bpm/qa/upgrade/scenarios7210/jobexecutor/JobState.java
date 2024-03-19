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

package org.camunda.bpm.qa.upgrade.scenarios7210.jobexecutor;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to preserve the state of a given set of jobs and restore them in case they've been locked.
 */
public class JobState {

    private final CommandExecutor commandExecutor;
    private final Map<String, Date> lockExpirationTimeByJobIdMap;

    private JobState(List<Job> jobs, CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
        this.lockExpirationTimeByJobIdMap = getLockExpirationTimeByJobIdMap(jobs);
    }

    /**
     * Creates a {@link JobState} from a given process engine
     *
     * @param engine the process engine
     * @return the job state
     */
    public static JobState ofAllProcessEngineJobs(ProcessEngine engine) {
        var managementService = engine.getManagementService();
        var engineConfig = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();
        var commandExecutor = engineConfig.getCommandExecutorTxRequired();

        var allJobs = managementService.createJobQuery()
                .list();

        return new JobState(allJobs, commandExecutor);
    }

    /**
     * Method that restores the locked jobs by using the original lockExpirationTime map to determine if the
     * current lockExpirationTime has changed. If so, the job will be unlocked and restored.
     * Same process will be repeated for all the job set this state is related to.
     */
    public void restoreLockedJobs() {
        for (Map.Entry<String, Date> entry : lockExpirationTimeByJobIdMap.entrySet()) {
            var jobId = entry.getKey();
            var lockExpirationTime = entry.getValue();

            updateJob(jobId, lockExpirationTime);
        }
    }

    private void updateJob(String jobId, Date originalLockExpirationTime) {
        commandExecutor.execute((Command<Void>) context -> {
            var jobManager = context.getJobManager();
            var job = jobManager.findJobById(jobId);

            if (jobHasBeenLocked(job, originalLockExpirationTime)) {
                job.unlock();
            }

            jobManager.updateJob(job);
            return null;
        });
    }

    private Map<String, Date> getLockExpirationTimeByJobIdMap(List<Job> jobs) {
        Map<String, Date> result = new HashMap<>();

        for (Job job : jobs) {
            var jobEntity = (JobEntity) job;
            result.put(jobEntity.getId(), jobEntity.getLockExpirationTime());
        }

        return result;
    }

    private boolean jobHasBeenLocked(JobEntity job, Date originalLockExpirationTime) {
        var currentLockExpirationTime = job.getLockExpirationTime();

        return currentLockExpirationTime != null && originalLockExpirationTime == null;
    }
}