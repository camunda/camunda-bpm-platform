package org.camunda.bpm.engine.test.jobexecutor;

import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * <p>This testcase verifies that jobs inserted without suspension state are active by default</p>
 *
 * @author Christian Lipphardt
 *
 */
public class JobAcquisitionSuspensionStateTest extends PluggableProcessEngineTestCase {

  protected CommandExecutor commandExecutor;
  protected String jobId;

  protected void setUp() throws Exception {
    commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
  }

  protected void tearDown() throws Exception {
    if (jobId != null) {
      commandExecutor.execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          final JobEntity newTimer = commandContext.getJobManager().findJobById(jobId);
          newTimer.delete();
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(newTimer.getId());
          return null;
        }
      });
    }
  }

  public void testJobAcquisitionForJobsWithoutSuspensionStateSet() {
    final String processInstanceId = "1";
    final String myCustomTimerEntity = "myCustomTimerEntity";
    final String jobId = "2";

    // we insert a timer job without specifying a suspension state
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        String tablePrefix = commandContext.getProcessEngineConfiguration().getDatabaseTablePrefix();

        try {
          SqlSession sqlSession = commandContext.getDbSqlSession().getSqlSession();
          connection = sqlSession.getConnection();
          statement = connection
              .createStatement();
          String insertStatementString = "INSERT INTO " + tablePrefix + "ACT_RU_JOB(ID_, REV_, RETRIES_, PROCESS_INSTANCE_ID_, TYPE_, EXCLUSIVE_, HANDLER_TYPE_, HANDLER_CFG_) " +
              "VALUES (" +
              "'" + jobId + "'," +
              "1," +
              "3," +
              "'" + processInstanceId + "'," +
              "'timer'," +
              DbSqlSessionFactory.databaseSpecificTrueConstant.get(processEngineConfiguration.getDatabaseType()) + "," +
              "'" + TimerStartEventJobHandler.TYPE + "'," +
              "'" + myCustomTimerEntity + "'" +
              ")";

          int updateResult = statement.executeUpdate(insertStatementString);
          assertEquals(1, updateResult);
          connection.commit();

          JobAcquisitionSuspensionStateTest.this.jobId = jobId;
          statement.close();
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
        assertEquals(myCustomTimerEntity, executableJobs.get(0).getJobHandlerConfigurationRaw());
        assertEquals(SuspensionState.ACTIVE.getStateCode(), executableJobs.get(0).getSuspensionState());

        executableJobs = jobManager.findJobsByProcessInstanceId(processInstanceId);
        assertEquals(1, executableJobs.size());
        assertEquals(myCustomTimerEntity, executableJobs.get(0).getJobHandlerConfigurationRaw());
        assertEquals(SuspensionState.ACTIVE.getStateCode(), executableJobs.get(0).getSuspensionState());
        return null;
      }
    });


  }

}
