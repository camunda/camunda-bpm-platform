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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AcquireJobCmdUnitTest {

  protected final static String PROCESS_INSTANCE_ID_1 = "pi_1";
  protected final static String PROCESS_INSTANCE_ID_2 = "pi_2";

  protected static final String JOB_ID_1 = "job_1";
  protected static final String JOB_ID_2 = "job_2";

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
  public void nonExclusiveJobsSameInstance() {
    // given: two non-exclusive jobs for a different process instance
    JobEntity job1 = createNonExclusiveJob(JOB_ID_1, PROCESS_INSTANCE_ID_1);
    JobEntity job2 = createNonExclusiveJob(JOB_ID_2, PROCESS_INSTANCE_ID_1);

    // when the job executor acquire new jobs
    when(jobManager.findNextJobsToExecute(any(Page.class))).thenReturn(Arrays.asList(job1, job2));

    // then the job executor should acquire job1 and job 2 in different batches
    checkThatAcquiredJobsInDifferentBatches();
  }

  @Test
  public void nonExclusiveDifferentInstance() {
    // given: two non-exclusive jobs for the same process instance
    JobEntity job1 = createNonExclusiveJob(JOB_ID_1, PROCESS_INSTANCE_ID_1);
    JobEntity job2 = createNonExclusiveJob(JOB_ID_2, PROCESS_INSTANCE_ID_2);

    // when the job executor acquire new jobs
    when(jobManager.findNextJobsToExecute(any(Page.class))).thenReturn(Arrays.asList(job1, job2));

    // then the job executor should acquire job1 and job 2 in different batches
    checkThatAcquiredJobsInDifferentBatches();
  }

  @Test
  public void exclusiveJobsSameInstance() {
    // given: two exclusive jobs for the same process instance
    JobEntity job1 = createExclusiveJob(JOB_ID_1, PROCESS_INSTANCE_ID_1);
    JobEntity job2 = createExclusiveJob(JOB_ID_2, PROCESS_INSTANCE_ID_1);

    // when the job executor acquire new jobs
    when(jobManager.findNextJobsToExecute(any(Page.class))).thenReturn(Arrays.asList(job1, job2));

    // then the job executor should acquire job1 and job 2 in one batch
    AcquiredJobs acquiredJobs = acquireJobsCmd.execute(commandContext);

    List<List<String>> jobIdBatches = acquiredJobs.getJobIdBatches();
    assertThat(jobIdBatches.size(), is(1));
    assertThat(jobIdBatches.get(0).size(), is(2));
    assertThat(jobIdBatches.get(0), hasItems(JOB_ID_1, JOB_ID_2));
  }

  @Test
  public void exclusiveJobsDifferentInstance() {
    // given: two exclusive jobs for a different process instance
    JobEntity job1 = createExclusiveJob(JOB_ID_1, PROCESS_INSTANCE_ID_1);
    JobEntity job2 = createExclusiveJob(JOB_ID_2, PROCESS_INSTANCE_ID_2);

    // when the job executor acquire new jobs
    when(jobManager.findNextJobsToExecute(any(Page.class))).thenReturn(Arrays.asList(job1, job2));

    // then the job executor should acquire job1 and job 2 in different batches
    checkThatAcquiredJobsInDifferentBatches();
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

  protected void checkThatAcquiredJobsInDifferentBatches() {
    AcquiredJobs acquiredJobs = acquireJobsCmd.execute(commandContext);

    List<List<String>> jobIdBatches = acquiredJobs.getJobIdBatches();
    assertThat(jobIdBatches.size(), is(2));
    assertThat(jobIdBatches.get(0).size(), is(1));
    assertThat(jobIdBatches.get(1).size(), is(1));
  }

}
