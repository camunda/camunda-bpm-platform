package org.camunda.bpm.engine.test.jobexecutor;

import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * <p>This testcase verifies that jobs without suspension state are correctly picked up by the job acquisition</p>
 *
 * @author Christian Lipphardt
 *
 */
public class JobAcquisitionTest extends PluggableProcessEngineTestCase {

  public void testJobAcquisitionForJobsWithoutSuspensionStateSet() {
    final String processInstanceId = "1";
    final String myCustomTimerEntity = "myCustomTimerEntity";

    final TimerEntity timer = new TimerEntity();
    timer.setRetries(3);
    timer.setDuedate(null);
    timer.setLockOwner(null);
    timer.setLockExpirationTime(null);
    timer.setJobHandlerConfiguration(myCustomTimerEntity);
    timer.setProcessInstanceId(processInstanceId);
    
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();

    // we create a timer entity
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        commandContext.getJobManager().insert(timer);
        return null;
      }
    });

    // we change the suspension state to null
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        try {
          SqlSession sqlSession = commandContext.getDbSqlSession().getSqlSession();
          PreparedStatement preparedStatement = sqlSession.getConnection()
              .prepareStatement("UPDATE ACT_RU_JOB SET SUSPENSION_STATE_ = NULL");
          assertEquals(1, preparedStatement.executeUpdate());
        } catch (SQLException e) {
          e.printStackTrace();
        }
        return null;
      }
    });

    // it is picked up by the acquisition queries
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();

        List<JobEntity> executableJobs = jobManager.findNextJobsToExecute(new Page(0, 1));

        assertEquals(1, executableJobs.size());
        assertEquals(myCustomTimerEntity, executableJobs.get(0).getJobHandlerConfiguration());

        executableJobs = jobManager.findExclusiveJobsToExecute(processInstanceId);
        assertEquals(1, executableJobs.size());
        assertEquals(myCustomTimerEntity, executableJobs.get(0).getJobHandlerConfiguration());
        return null;
      }
    });

    // cleanup
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        commandContext.getJobManager().delete(timer);
        return null;
      }
    });
    
  }

}
