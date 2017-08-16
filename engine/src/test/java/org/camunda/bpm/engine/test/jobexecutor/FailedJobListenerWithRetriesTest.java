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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.DefaultJobRetryCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultFailedJobCommandFactory;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(Parameterized.class)
public class FailedJobListenerWithRetriesTest {

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setFailedJobCommandFactory(new OLEFailedJobCommandFactory());
      configuration.setFailedJobListenerMaxRetries(5);
      return configuration;
    }
  };

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private RuntimeService runtimeService;

  @Parameterized.Parameter(0)
  public int failedRetriesNumber;

  @Parameterized.Parameter(1)
  public int jobRetries;

  @Parameterized.Parameter(2)
  public boolean jobLocked;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
  }

  @Parameterized.Parameters
  public static Collection<Object[]> scenarios() {
    return Arrays.asList(new Object[][] {
        { 4, 0, false },
        //all retries are depleted without success -> the job is still locked
        { 5, 1, true }
    });
  }

  @Test
  @org.camunda.bpm.engine.test.Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testFailedJobListenerRetries() {
    //given
    runtimeService.startProcessInstanceByKey("failingProcess");

    //when the job is run several times till the incident creation
    Job job = getJob();
    while (job.getRetries() > 0 && ((JobEntity)job).getLockOwner() == null ) {
      try {
        lockTheJob(job.getId());
        engineRule.getManagementService().executeJob(job.getId());
      } catch (Exception ex) {
      }
      job = getJob();
    }

    //then
    JobEntity jobFinalState = (JobEntity)engineRule.getManagementService().createJobQuery().jobId(job.getId()).list().get(0);
    assertEquals(jobRetries, jobFinalState.getRetries());
    if (jobLocked) {
      assertNotNull(jobFinalState.getLockOwner());
      assertNotNull(jobFinalState.getLockExpirationTime());
    } else {
      assertNull(jobFinalState.getLockOwner());
      assertNull(jobFinalState.getLockExpirationTime());
    }
  }

  void lockTheJob(final String jobId) {
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequiresNew().execute(new Command<Object>() {
      @Override
      public Object execute(CommandContext commandContext) {
        final JobEntity job = commandContext.getJobManager().findJobById(jobId);
        job.setLockOwner("someLockOwner");
        job.setLockExpirationTime(DateUtils.addHours(ClockUtil.getCurrentTime(), 1));
        return null;
      }
    });
  }

  private Job getJob() {
    List<Job> jobs = engineRule.getManagementService().createJobQuery().list();
    assertEquals(1, jobs.size());
    return jobs.get(0);
  }

  private class OLEFailedJobCommandFactory extends DefaultFailedJobCommandFactory {

    private Map<String, OLEFoxJobRetryCmd> oleFoxJobRetryCmds = new HashMap<String, OLEFoxJobRetryCmd>();

    public Command<Object> getCommand(String jobId, Throwable exception) {
      return getOleFoxJobRetryCmds(jobId, exception);
    }

    public OLEFoxJobRetryCmd getOleFoxJobRetryCmds(String jobId, Throwable exception) {
      if (!oleFoxJobRetryCmds.containsKey(jobId)) {
        oleFoxJobRetryCmds.put(jobId, new OLEFoxJobRetryCmd(jobId, exception));
      }
      return oleFoxJobRetryCmds.get(jobId);
    }
  }

  private class OLEFoxJobRetryCmd extends DefaultJobRetryCmd {

    private int countRuns = 0;

    public OLEFoxJobRetryCmd(String jobId, Throwable exception) {
      super(jobId, exception);
    }

    @Override
    public Object execute(CommandContext commandContext) {
      Job job = getJob();
      //on last attempt the incident will be created, we imitate OLE
      if (job.getRetries() == 1) {
        countRuns++;
        if (countRuns <= failedRetriesNumber) {
          super.execute(commandContext);
          throw new OptimisticLockingException("OLE");
        }
      }
      return super.execute(commandContext);
    }
  }
}
