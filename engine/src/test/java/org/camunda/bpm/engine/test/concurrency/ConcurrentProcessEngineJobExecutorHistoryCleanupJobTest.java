package org.camunda.bpm.engine.test.concurrency;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineBootstrapCommand;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.BootstrapEngineCommand;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * <p>Tests a concurrent attempt of a bootstrapping Process Engine to reconfigure
 * the HistoryCleanupJob while the JobExecutor tries to execute it.</p>
 *
 * @author Nikola Koevski
 */
public class ConcurrentProcessEngineJobExecutorHistoryCleanupJobTest extends ConcurrencyTestCase {

  private static final String PROCESS_ENGINE_NAME = "historyCleanupJobEngine";

  @Override
  public void setUp() throws Exception {

    // Ensure that current time is outside batch window
    Calendar timeOfDay = Calendar.getInstance();
    timeOfDay.set(Calendar.HOUR_OF_DAY, 17);
    ClockUtil.setCurrentTime(timeOfDay.getTime());

    super.setUp();
  }

  @Override
  protected void closeDownProcessEngine() {
    super.closeDownProcessEngine();
    final ProcessEngine otherProcessEngine = ProcessEngines.getProcessEngine(PROCESS_ENGINE_NAME);
    if (otherProcessEngine != null) {

      ((ProcessEngineConfigurationImpl)otherProcessEngine.getProcessEngineConfiguration()).getCommandExecutorTxRequired().execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {

          List<Job> jobs = otherProcessEngine.getManagementService().createJobQuery().list();
          if (jobs.size() > 0) {
            assertEquals(1, jobs.size());
            String jobId = jobs.get(0).getId();
            commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
            commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
          }

          return null;
        }
      });

      otherProcessEngine.close();
      ProcessEngines.unregister(otherProcessEngine);
    }
  }

  @Override
  public void tearDown() throws Exception {
    ((ProcessEngineConfigurationImpl)processEngine.getProcessEngineConfiguration()).getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<Job> jobs = processEngine.getManagementService().createJobQuery().list();
        if (jobs.size() > 0) {
          assertEquals(1, jobs.size());
          String jobId = jobs.get(0).getId();
          commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
        }

        return null;
      }
    });
    ClockUtil.setCurrentTime(new Date());
    super.tearDown();
  }

  public void testConcurrentHistoryCleanupJobReconfigurationExecution() throws InterruptedException {

    getProcessEngine().getHistoryService().cleanUpHistoryAsync(true);

    ThreadControl thread1 = executeControllableCommand(new ControllableJobExecutionCommand());
    thread1.reportInterrupts();
    thread1.waitForSync();

    ThreadControl thread2 = executeControllableCommand(new ControllableProcessEngineBootstrapCommand());
    thread2.reportInterrupts();
    thread2.waitForSync();

    thread1.makeContinue();
    thread1.waitForSync();

    thread2.makeContinue();

    Thread.sleep(2000);

    thread1.waitUntilDone();

    thread2.waitForSync();
    thread2.waitUntilDone(true);

    assertNull(thread1.getException());
    assertNull(thread2.getException());

    assertNotNull(ProcessEngines.getProcessEngines().get(PROCESS_ENGINE_NAME));
  }

  protected static class ControllableProcessEngineBootstrapCommand extends ControllableCommand<Void> {

    @Override
    public Void execute(CommandContext commandContext) {

      ProcessEngineBootstrapCommand bootstrapCommand = new ControllableBootstrapEngineCommand(this.monitor);

      ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/concurrency/historycleanup.camunda.cfg.xml");


      processEngineConfiguration.setProcessEngineBootstrapCommand(bootstrapCommand);

      processEngineConfiguration.setProcessEngineName(PROCESS_ENGINE_NAME);
      processEngineConfiguration.buildProcessEngine();

      return null;
    }
  }

  protected static class ControllableJobExecutionCommand extends ControllableCommand<Void> {

    @Override
    public Void execute(CommandContext commandContext) {

      monitor.sync();

      List<Job> historyCleanupJobs = commandContext.getProcessEngineConfiguration().getHistoryService().findHistoryCleanupJobs();

      for (Job job : historyCleanupJobs) {
        commandContext.getProcessEngineConfiguration().getManagementService().executeJob(job.getId());
      }

      monitor.sync();

      return null;
    }
  }

  protected static class ControllableBootstrapEngineCommand extends BootstrapEngineCommand implements Command<Void> {

    protected final ThreadControl monitor;

    public ControllableBootstrapEngineCommand(ThreadControl threadControl) {
      this.monitor = threadControl;
    }

    @Override
    protected void createHistoryCleanupJob() {

      monitor.sync();

      super.createHistoryCleanupJob();

      monitor.sync();
    }
  }
}
