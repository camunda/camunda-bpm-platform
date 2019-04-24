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
package org.camunda.bpm.qa.performance.engine.sqlstatementlog;

import org.apache.ibatis.session.SqlSessionFactory;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;

/**
 * <p>ProcessEnginePlugin activating statement logging.</p>
 *
 * <p>Wraps the MyBatis {@link SqlSessionFactory} used by the process engine using
 * the {@link StatementLogSqlSessionFactory} allowing us to intercept the sql statements
 * executed by the process engine and gain insight into the Database communication.</p>
 *
 * @author Daniel Meyer
 *
 */
public class StatementLogProcessEnginePlugin extends AbstractProcessEnginePlugin {

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    SqlSessionFactory sqlSessionFactory = processEngineConfiguration.getSqlSessionFactory();

    // wrap the SqlSessionFactory using a statement logger
    StatementLogSqlSessionFactory wrappedSessionFactory = new StatementLogSqlSessionFactory(sqlSessionFactory);
    processEngineConfiguration.setSqlSessionFactory(wrappedSessionFactory);

    // replace the sqlSessionFacorty used by the DbSqlSessionFactory as well
    DbSqlSessionFactory dbSqlSessionFactory = processEngineConfiguration.getDbSqlSessionFactory();
    dbSqlSessionFactory.setSqlSessionFactory(wrappedSessionFactory);
  }

}
