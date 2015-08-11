package org.camunda.bpm.engine.test.jobexecutor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * <p>This testcase verifies that jobs without suspension state are correctly picked up by the job acquisition</p>
 *
 * @author Christian Lipphardt
 *
 */
public class JobAcquisitionSuspensionStateTest extends PluggableProcessEngineTestCase {

  public void testJobAcquisitionForJobsWithoutSuspensionStateSet() {
    final String processInstanceId = "1";
    final String myCustomTimerEntity = "myCustomTimerEntity";

    final TimerEntity timer = new TimerEntity();
    timer.setRetries(3);
    timer.setDuedate(null);
    timer.setLockOwner(null);
    timer.setLockExpirationTime(null);
    timer.setJobHandlerType(TimerStartEventJobHandler.TYPE);
    timer.setJobHandlerConfiguration(myCustomTimerEntity);
    timer.setProcessInstanceId(processInstanceId);

    final CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();

    try {
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
          Connection connection = null;
          Statement statement = null;
          ResultSet rs = null;

          try {
            SqlSession sqlSession = commandContext.getDbSqlSession().getSqlSession();
            connection = sqlSession.getConnection();
            statement = connection
                .createStatement();
            int updateResult = statement.executeUpdate("UPDATE ACT_RU_JOB " +
                "SET SUSPENSION_STATE_ = NULL, REV_ = 2" +
                " WHERE SUSPENSION_STATE_ = 1");
            statement.close();
            assertEquals(1, updateResult);

            statement = connection
                .createStatement();
            rs = statement.executeQuery("SELECT * FROM ACT_RU_JOB WHERE SUSPENSION_STATE_ IS NULL");

            int rowNum = 0;
            while (rs.next()) {
              rowNum = rs.getRow();
            }
            assertEquals(1, rowNum);
          } catch (SQLException e) {
            throw new RuntimeException(e);
          } finally {
            try {
              if (statement != null) {
                statement.close();
              }
              if (rs != null) {
                rs.close();
              }
              if (connection != null) {
                connection.close();
              }
            } catch (SQLException e) {
              throw new RuntimeException(e);
            }
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

    } finally {
      // cleanup
      commandExecutor.execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          final JobEntity newTimer = commandContext.getJobManager().findJobById(timer.getId());
          newTimer.delete();
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(newTimer.getId());
          return null;
        }
      });
    }

  }

}
