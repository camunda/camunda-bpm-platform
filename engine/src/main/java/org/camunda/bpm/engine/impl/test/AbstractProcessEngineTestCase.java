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

package org.camunda.bpm.engine.impl.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import junit.framework.AssertionFailedError;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.LogUtil.ThreadLogMode;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Assert;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractProcessEngineTestCase extends PvmTestCase {

  static {
    // this ensures that mybatis uses the jdk logging
//    LogFactory.useJdkLogging();
    // with an upgrade of mybatis, this might have to become org.mybatis.generator.logging.LogFactory.forceJavaLogging();
  }

  private static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
    "ACT_GE_PROPERTY"
  );

  protected ProcessEngine processEngine;

  protected ThreadLogMode threadRenderingMode = DEFAULT_THREAD_LOG_MODE;
  protected String deploymentId;
  protected Throwable exception;

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected FormService formService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  protected AuthorizationService authorizationService;

  protected abstract void initializeProcessEngine();

  // Default: do nothing
  protected void closeDownProcessEngine() {
  }

  @Override
  public void runBare() throws Throwable {
    initializeProcessEngine();
    if (repositoryService==null) {
      initializeServices();
    }

    log.severe(EMPTY_LINE);

    try {

      deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, getClass(), getName());

      super.runBare();

    }  catch (AssertionFailedError e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "ASSERTION FAILED: "+e, e);
      exception = e;
      throw e;

    } catch (Throwable e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "EXCEPTION: "+e, e);
      exception = e;
      throw e;

    } finally {
      TestHelper.annotationDeploymentTearDown(processEngine, deploymentId, getClass(), getName());
      identityService.setAuthenticatedUserId(null);
      assertAndEnsureCleanDb();
      ClockUtil.reset();

      // Can't do this in the teardown, as the teardown will be called as part of the super.runBare
      closeDownProcessEngine();
    }
  }

  /** Each test is assumed to clean up all DB content it entered.
   * After a test method executed, this method scans all tables to see if the DB is completely clean.
   * It throws AssertionFailed in case the DB is not clean.
   * If the DB is not clean, it is cleaned by performing a create a drop. */
  protected void assertAndEnsureCleanDb() throws Throwable {
    log.fine("verifying that db is clean after test");

    Map<String, Long> tableCounts = managementService.getTableCount();
    StringBuilder outputMessage = new StringBuilder();
    for (String tableName : tableCounts.keySet()) {
      String tableNameWithoutPrefix = tableName.replace(processEngineConfiguration.getDatabaseTablePrefix(), "");
      if (!TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(tableNameWithoutPrefix)) {
        Long count = tableCounts.get(tableName);
        if (count!=0L) {
          outputMessage.append("  "+tableName + ": " + count + " record(s) ");
        }
      }
    }
    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "DB NOT CLEAN: \n");
      log.severe(EMPTY_LINE);
      log.severe(outputMessage.toString());

      log.info("dropping and recreating db");

      CommandExecutor commandExecutor = ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration().getCommandExecutorTxRequired();
      commandExecutor.execute(new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          DbSqlSession session = commandContext.getSession(DbSqlSession.class);
          session.dbSchemaDrop();
          session.dbSchemaCreate();
          return null;
        }
      });

      if (exception!=null) {
        throw exception;
      } else {
        Assert.fail(outputMessage.toString());
      }
    } else {
      log.info("database was clean");
    }
  }


  protected void initializeServices() {
    processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    formService = processEngine.getFormService();
    historyService = processEngine.getHistoryService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
    authorizationService = processEngine.getAuthorizationService();
  }

  public void assertProcessEnded(final String processInstanceId) {
    ProcessInstance processInstance = processEngine
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();

    if (processInstance!=null) {
      throw new AssertionFailedError("Expected finished process instance '"+processInstanceId+"' but it was still in the db");
    }
  }

  @Deprecated
  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
    waitForJobExecutorToProcessAllJobs(maxMillisToWait);
  }

  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait) {
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();
    long intervalMillis = 1000;

    int jobExecutorWaitTime = jobExecutor.getWaitTimeInMillis() * 2;
    if(maxMillisToWait < jobExecutorWaitTime) {
      maxMillisToWait = jobExecutorWaitTime;
    }

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          try {
            areJobsAvailable = areJobsAvailable();
          } catch(Throwable t) {
            // Ignore, possible that exception occurs due to locking/updating of table on MSSQL when
            // isolation level doesn't allow READ of the table
          }
        }
      } catch (InterruptedException e) {
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new ProcessEngineException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  @Deprecated
  public void waitForJobExecutorOnCondition(long maxMillisToWait, long intervalMillis, Callable<Boolean> condition) {
    waitForJobExecutorOnCondition(maxMillisToWait, condition);
  }

  public void waitForJobExecutorOnCondition(long maxMillisToWait, Callable<Boolean> condition) {
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();
    long intervalMillis = 500;

    if(maxMillisToWait < (jobExecutor.getWaitTimeInMillis()*2)) {
      maxMillisToWait = (jobExecutor.getWaitTimeInMillis()*2);
    }

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean conditionIsViolated = true;
      try {
        while (conditionIsViolated && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          conditionIsViolated = !condition.call();
        }
      } catch (InterruptedException e) {
      } catch (Exception e) {
        throw new ProcessEngineException("Exception while waiting on condition: "+e.getMessage(), e);
      } finally {
        timer.cancel();
      }
      if (conditionIsViolated) {
        throw new ProcessEngineException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  public void executeAvailableJobs() {
    List<Job> jobs = managementService.createJobQuery().withRetriesLeft().list();

    if (jobs.isEmpty()) {
      return;
    }

    for (Job job : jobs) {
      try {
        managementService.executeJob(job.getId());
      } catch (Exception e) {};
    }

    executeAvailableJobs();
  }

  public boolean areJobsAvailable() {
    List<Job> list = managementService.createJobQuery().list();
    for (Job job : list) {
      if (!job.isSuspended() && job.getRetries() > 0 && (job.getDuedate() == null || ClockUtil.getCurrentTime().after(job.getDuedate()))) {
        return true;
      }
    }
    return false;
  }

  private static class InteruptTask extends TimerTask {
    protected boolean timeLimitExceeded = false;
    protected Thread thread;
    public InteruptTask(Thread thread) {
      this.thread = thread;
    }
    public boolean isTimeLimitExceeded() {
      return timeLimitExceeded;
    }
    public void run() {
      timeLimitExceeded = true;
      thread.interrupt();
    }
  }

  protected List<ActivityInstance> getInstancesForActivitiyId(ActivityInstance activityInstance, String activityId) {
    List<ActivityInstance> result = new ArrayList<ActivityInstance>();
    if(activityInstance.getActivityId().equals(activityId)) {
      result.add(activityInstance);
    }
    for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
      result.addAll(getInstancesForActivitiyId(childInstance,activityId));
    }
    return result;
  }

  protected void runAsUser(String userId, List<String> groupIds, Runnable r) {
    try {
      identityService.setAuthenticatedUserId(userId);
      processEngineConfiguration.setAuthorizationEnabled(true);

      r.run();

    } finally {
      identityService.setAuthenticatedUserId(null);
      processEngineConfiguration.setAuthorizationEnabled(false);
    }
  }

}
