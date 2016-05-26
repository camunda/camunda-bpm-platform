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

package org.camunda.bpm.engine.test.standalone.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSession;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.slf4j.Logger;


/**
 * @author Tom Baeyens
 */
public class MetaDataTest extends PluggableProcessEngineTestCase {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  public void testMetaData() {
    ((ProcessEngineImpl)processEngine)
      .getProcessEngineConfiguration()
      .getCommandExecutorTxRequired()
      .execute(new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          // PRINT THE TABLE NAMES TO CHECK IF WE CAN USE METADATA INSTEAD
          // THIS IS INTENDED FOR TEST THAT SHOULD RUN ON OUR QA INFRASTRUCTURE TO SEE IF METADATA
          // CAN BE USED INSTEAD OF PERFORMING A QUERY THAT MIGHT FAIL
          try {
            SqlSession sqlSession = commandContext.getSession(DbSqlSession.class).getSqlSession();
            ResultSet tables = sqlSession.getConnection().getMetaData().getTables(null, null, null, null);
            while (tables.next()) {
              ResultSetMetaData resultSetMetaData = tables.getMetaData();
              int columnCount = resultSetMetaData.getColumnCount();
              for (int i=1; i<=columnCount; i++) {
                LOG.info("result set column "+i+" | "+resultSetMetaData.getColumnName(i)+" | "+resultSetMetaData.getColumnLabel(i)+" | "+tables.getString(i));
              }
              LOG.info("-------------------------------------------------------");
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          return null;
        }
      });
  }

  public void testMariaDbDatabaseType() {
    if (isMariaDbConfigured()) {
      assertEquals("mariadb",  processEngineConfiguration.getDatabaseType());
    }
  }

  public boolean isMariaDbConfigured() {
    return processEngineConfiguration.getJdbcUrl().toLowerCase().contains("mariadb") ||
      processEngineConfiguration.getJdbcDriver().toLowerCase().contains("mariadb");
  }
}
