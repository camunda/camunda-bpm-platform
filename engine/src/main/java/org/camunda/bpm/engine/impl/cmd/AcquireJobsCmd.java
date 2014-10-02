/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cmd;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingListener;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;


/**
 * @author Nick Burch
 * @author Daniel Meyer
 */
public class AcquireJobsCmd implements Command<AcquiredJobs>, OptimisticLockingListener {

  private final JobExecutor jobExecutor;

  protected AcquiredJobs acquiredJobs;

  public AcquireJobsCmd(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }

  public AcquiredJobs execute(CommandContext commandContext) {

    String lockOwner = jobExecutor.getLockOwner();
    int lockTimeInMillis = jobExecutor.getLockTimeInMillis();
    int maxNonExclusiveJobsPerAcquisition = jobExecutor.getMaxJobsPerAcquisition();

    acquiredJobs = new AcquiredJobs();
    List<JobEntity> jobs = commandContext
      .getJobManager()
      .findNextJobsToExecute(new Page(0, maxNonExclusiveJobsPerAcquisition));

    for (JobEntity job: jobs) {
      List<String> jobIds = new ArrayList<String>();

      if (job != null && !acquiredJobs.contains(job.getId())) {
        if (job.isExclusive() && job.getProcessInstanceId() != null) {
          // acquire all exclusive jobs in the same process instance
          // (includes the current job)
          List<JobEntity> exclusiveJobs = commandContext.getJobManager()
            .findExclusiveJobsToExecute(job.getProcessInstanceId());
          for (JobEntity exclusiveJob : exclusiveJobs) {
            if(exclusiveJob != null) {
              lockJob(exclusiveJob, lockOwner, lockTimeInMillis);
              jobIds.add(exclusiveJob.getId());
            }
          }
        } else {
          lockJob(job, lockOwner, lockTimeInMillis);
          jobIds.add(job.getId());
        }

      }

      acquiredJobs.addJobIdBatch(jobIds);
    }

    // register an OptimisticLockingListener which is notified about jobs which cannot be acquired.
    // the listener removes them from the list of acquired jobs.
    Context.getCommandContext()
      .getDbEntityManager()
      .registerOptimisticLockingListener(this);

    return acquiredJobs;
  }

  protected void lockJob(JobEntity job, String lockOwner, int lockTimeInMillis) {
    job.setLockOwner(lockOwner);
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(ClockUtil.getCurrentTime());
    gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
    job.setLockExpirationTime(gregorianCalendar.getTime());
  }

  public Class<? extends DbEntity> getEntityType() {
    return JobEntity.class;
  }

  public void failedOperation(DbOperation operation) {
    if (operation instanceof DbEntityOperation) {

      DbEntityOperation entityOperation = (DbEntityOperation) operation;
      if(JobEntity.class.isAssignableFrom(entityOperation.getEntityType())) {
        // could not lock the job -> remove it from list of acquired jobs
        acquiredJobs.removeJobId(entityOperation.getEntity().getId());
      }

    }
  }

}
