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

import org.camunda.bpm.engine.impl.Page;
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

import java.util.*;


/**
 * @author Nick Burch
 * @author Daniel Meyer
 */
public class AcquireJobsCmd implements Command<AcquiredJobs>, OptimisticLockingListener {

  private final JobExecutor jobExecutor;

  protected AcquiredJobs acquiredJobs;
  protected int numJobsToAcquire;

  public AcquireJobsCmd(JobExecutor jobExecutor) {
    this(jobExecutor, jobExecutor.getMaxJobsPerAcquisition());
  }

  public AcquireJobsCmd(JobExecutor jobExecutor, int numJobsToAcquire) {
    this.jobExecutor = jobExecutor;
    this.numJobsToAcquire = numJobsToAcquire;
  }

  public AcquiredJobs execute(CommandContext commandContext) {

    acquiredJobs = new AcquiredJobs(numJobsToAcquire);

    List<JobEntity> jobs = commandContext
      .getJobManager()
      .findNextJobsToExecute(new Page(0, numJobsToAcquire));

    Map<String, List<String>> exclusiveJobsByProcessInstance = new HashMap<String, List<String>>();

    for (JobEntity job : jobs) {

      lockJob(job);

      if(job.isExclusive()) {
        List<String> list = exclusiveJobsByProcessInstance.get(job.getProcessInstanceId());
        if (list == null) {
          list = new ArrayList<String>();
          exclusiveJobsByProcessInstance.put(job.getProcessInstanceId(), list);
        }
        list.add(job.getId());
      }
      else {
        acquiredJobs.addJobIdBatch(job.getId());
      }
    }

    for (List<String> jobIds : exclusiveJobsByProcessInstance.values()) {
      acquiredJobs.addJobIdBatch(jobIds);
    }

    // register an OptimisticLockingListener which is notified about jobs which cannot be acquired.
    // the listener removes them from the list of acquired jobs.
    commandContext
      .getDbEntityManager()
      .registerOptimisticLockingListener(this);


    return acquiredJobs;
  }

  protected void lockJob(JobEntity job) {
    String lockOwner = jobExecutor.getLockOwner();
    job.setLockOwner(lockOwner);

    int lockTimeInMillis = jobExecutor.getLockTimeInMillis();

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
