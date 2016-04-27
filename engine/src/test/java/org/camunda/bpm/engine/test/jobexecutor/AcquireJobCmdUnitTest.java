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

package org.camunda.bpm.engine.test.jobexecutor;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.junit.Before;
import org.junit.Test;

public class AcquireJobCmdUnitTest {

  protected final static String PROCESS_INSTANCE_ID_1 = "pi_1";
  protected final static String PROCESS_INSTANCE_ID_2 = "pi_2";

  protected static final String JOB_ID_1 = "job_1";
  protected static final String JOB_ID_2 = "job_2";
  protected static final String JOB_ID_3 = "job_3";

  protected AcquireJobsCmd acquireJobsCmd;
  protected JobManager jobManager;
  protected CommandContext commandContext;

  @Before
  public void initCommand() {
    JobExecutor jobExecutor = mock(JobExecutor.class);
    when(jobExecutor.getMaxJobsPerAcquisition()).thenReturn(3);
    when(jobExecutor.getLockOwner()).thenReturn("test");
    when(jobExecutor.getLockTimeInMillis()).thenReturn(5 * 60 * 1000);

    acquireJobsCmd = new AcquireJobsCmd(jobExecutor);

    commandContext = mock(CommandContext.class);

    DbEntityManager dbEntityManager = mock(DbEntityManager.class);
    when(commandContext.getDbEntityManager()).thenReturn(dbEntityManager);

    jobManager = mock(JobManager.class);
    when(commandContext.getJobManager()).thenReturn(jobManager);
  }

  @Test
  public void nonExclusiveJobs() {
    JobEntity job1 = createNonExclusiveJob(JOB_ID_1, PROCESS_INSTANCE_ID_1);
    JobEntity job2 = createNonExclusiveJob(JOB_ID_2, PROCESS_INSTANCE_ID_1);

    when(jobManager.findNextJobsToExecute(any(Page.class))).thenReturn(Arrays.asList(job1, job2));
    when(jobManager.findExclusiveJobsToExecute(PROCESS_INSTANCE_ID_1)).thenReturn(Collections.<JobEntity> emptyList());

    AcquiredJobs acquiredJobs = acquireJobsCmd.execute(commandContext);

    List<List<String>> jobIdBatches = acquiredJobs.getJobIdBatches();
    assertThat(jobIdBatches.size(), is(2));
    assertThat(jobIdBatches.get(0).size(), is(1));
    assertThat(jobIdBatches.get(0), hasItem(JOB_ID_1));
    assertThat(jobIdBatches.get(1).size(), is(1));
    assertThat(jobIdBatches.get(1), hasItem(JOB_ID_2));
  }

  @Test
  public void exclusiveJobs() {
    JobEntity job1 = createExclusiveJob(JOB_ID_1, PROCESS_INSTANCE_ID_1);
    JobEntity job2 = createExclusiveJob(JOB_ID_2, PROCESS_INSTANCE_ID_1);
    List<JobEntity> jobs = Arrays.asList(job1, job2);

    when(jobManager.findNextJobsToExecute(any(Page.class))).thenReturn(jobs);
    when(jobManager.findExclusiveJobsToExecute(PROCESS_INSTANCE_ID_1)).thenReturn(jobs);

    AcquiredJobs acquiredJobs = acquireJobsCmd.execute(commandContext);

    List<List<String>> jobIdBatches = acquiredJobs.getJobIdBatches();
    assertThat(jobIdBatches.size(), is(1));
    assertThat(jobIdBatches.get(0).size(), is(2));
    assertThat(jobIdBatches.get(0), hasItems(JOB_ID_1, JOB_ID_2));
  }

  @Test
  public void exclusiveJobsConcurrentLockSameInstance() {
    // given: two exclusive jobs for the same process instance and two job executors
    JobEntity job1 = createExclusiveJob(JOB_ID_1, PROCESS_INSTANCE_ID_1);
    JobEntity job2 = createExclusiveJob(JOB_ID_2, PROCESS_INSTANCE_ID_1);

    // when the job executor acquire new jobs
    when(jobManager.findNextJobsToExecute(any(Page.class))).thenReturn(Arrays.asList(job1, job2));
    // and job2 is locked by the other job executor concurrently
    when(jobManager.findExclusiveJobsToExecute(PROCESS_INSTANCE_ID_1)).thenReturn(Collections.singletonList(job1));
    // - note that job1 was not locked by the other job executor because it was locked before. The job execution failed
    // and the job was unlocked before this job executor starts to acquire jobs.

    // then the job executor should acquire job1
    AcquiredJobs acquiredJobs = acquireJobsCmd.execute(commandContext);

    List<List<String>> jobIdBatches = acquiredJobs.getJobIdBatches();
    assertThat(jobIdBatches.size(), is(1));
    assertThat(jobIdBatches.get(0).size(), is(1));
    assertThat(jobIdBatches.get(0), hasItem(JOB_ID_1));
  }

  @Test
  public void exclusiveJobsConcurrentLockDifferentInstance() {
    // given: two exclusive jobs for the same process instance and two job executors
    JobEntity job1 = createExclusiveJob(JOB_ID_1, PROCESS_INSTANCE_ID_1);
    JobEntity job2 = createExclusiveJob(JOB_ID_2, PROCESS_INSTANCE_ID_2);

    // when the job executor acquire new jobs
    when(jobManager.findNextJobsToExecute(any(Page.class))).thenReturn(Arrays.asList(job1, job2));
    when(jobManager.findExclusiveJobsToExecute(PROCESS_INSTANCE_ID_1)).thenReturn(Collections.singletonList(job1));
    // job2 is locked by the other job executor concurrently
    // and a new job is created which belongs to the same instance as job2
    JobEntity job3 = createExclusiveJob(JOB_ID_3, PROCESS_INSTANCE_ID_2);
    when(jobManager.findExclusiveJobsToExecute(PROCESS_INSTANCE_ID_2)).thenReturn(Collections.singletonList(job3));

    // then the job executor should only acquire job1
    AcquiredJobs acquiredJobs = acquireJobsCmd.execute(commandContext);

    List<List<String>> jobIdBatches = acquiredJobs.getJobIdBatches();
    assertThat(jobIdBatches.size(), is(1));
    assertThat(jobIdBatches.get(0).size(), is(1));
    assertThat(jobIdBatches.get(0), hasItem(JOB_ID_1));
  }

  protected JobEntity createExclusiveJob(String id, String processInstanceId) {
    JobEntity job = createNonExclusiveJob(id, processInstanceId);
    when(job.isExclusive()).thenReturn(true);
    return job;
  }

  protected JobEntity createNonExclusiveJob(String id, String processInstanceId) {
    JobEntity job = mock(JobEntity.class);
    when(job.getId()).thenReturn(id);
    when(job.getProcessInstanceId()).thenReturn(processInstanceId);
    return job;
  }

}
