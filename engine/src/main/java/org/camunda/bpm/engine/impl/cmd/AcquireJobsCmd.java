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
package org.camunda.bpm.engine.impl.cmd;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingListener;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingResult;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.AcquirableJobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;


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

    List<AcquirableJobEntity> jobs = commandContext
      .getJobManager()
      .findNextJobsToExecute(new Page(0, numJobsToAcquire));

    Map<String, List<String>> exclusiveJobsByProcessInstance = new HashMap<String, List<String>>();

    for (AcquirableJobEntity job : jobs) {

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

  /**
   * When CockroachDB is used, this command may be retried multiple times until
   * it is successful, or the retries are exhausted. CockroachDB uses a stricter,
   * SERIALIZABLE transaction isolation which ensures a serialized manner
   * of transaction execution. A concurrent transaction that attempts to modify
   * the same data as another transaction is required to abort, rollback and retry.
   * This also makes our use-case of pessimistic locks redundant since we only use
   * them as synchronization barriers, and not to lock actual data which would
   * protect it from concurrent modifications.
   *
   * The AcquireJobs command only executes internal code, so we are certain that
   * a retry of failed job locks will not impact user data, and may be performed
   * multiple times.
   */
  @Override
  public boolean isRetryable() {
    return true;
  }

  protected void lockJob(AcquirableJobEntity job) {
    String lockOwner = jobExecutor.getLockOwner();
    job.setLockOwner(lockOwner);

    int lockTimeInMillis = jobExecutor.getLockTimeInMillis();

    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(ClockUtil.getCurrentTime());
    gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
    job.setLockExpirationTime(gregorianCalendar.getTime());
  }

  @Override
  public Class<? extends DbEntity> getEntityType() {
    return AcquirableJobEntity.class;
  }

  @Override
  public OptimisticLockingResult failedOperation(DbOperation operation) {

    if (operation instanceof DbEntityOperation) {

      DbEntityOperation entityOperation = (DbEntityOperation) operation;
      
      // could not lock the job -> remove it from list of acquired jobs
      acquiredJobs.removeJobId(entityOperation.getEntity().getId());

      // When the job that failed the lock with an OLE is removed,
      // we suppress the OLE.
      return OptimisticLockingResult.IGNORE;
    }

    // If none of the conditions are satisfied, this might indicate a bug,
    // so we throw the OLE.
    return OptimisticLockingResult.THROW;
  }

}
